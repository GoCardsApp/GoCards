/*
 * MIT License
 *
 * Copyright (c) 2025 GoCards Grzegorz Ziemski
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

/**
 * Source: https://github.com/GoCardsEdu/Dynamic-Pager/blob/main/dynamic-pager/src/main/java/pl/gocards/dynamic_pager/DynamicPagerViewModel.kt
 * @author Grzegorz Ziemski
 * @version 1.0
 */

package pl.gocards.dynamic_pager

import android.app.Application
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * A ViewModel for managing the state and logic of the Pager.
 *
 * It provides methods to manipulate the pager's items, navigate between pages,
 * and perform operations such as adding or deleting items.
 *
 * @author Grzegorz Ziemski
 * @version 1.0
 */
open class DynamicPagerViewModel<E>(
    application: Application
) : AndroidViewModel(application) {

    private val mutex = Mutex()
    private var _currentItems = emptyList<E>()
    private val _items = mutableStateOf(_currentItems)
    val items: State<List<E>> = _items

    /**
     * Disable user scrolling while the page is being updated through ViewModel methods.
     */
    val userScrollEnabled: MutableState<Boolean> = mutableStateOf(true)

    private val _settledPage = MutableStateFlow<Int?>(null)
    val settledPage: StateFlow<Int?> = _settledPage.asStateFlow()

    private val _scrollToPage = MutableStateFlow<Int?>(null)
    val scrollToPage: StateFlow<Int?> = _scrollToPage.asStateFlow()

    private val _targetPage = MutableStateFlow<Int?>(null)
    private val targetPage: StateFlow<Int?> = _targetPage.asStateFlow()

    private val _focusPage = MutableStateFlow<Int?>(null)
    val focusPage: StateFlow<Int?> = _focusPage.asStateFlow()

    init {
        viewModelScope.launch {
            combine(settledPage, targetPage, focusPage) { settled, target, focus ->
                focus == null && settled != null && settled == target
            }.collect {
                if (it) {
                    onScrollCompleted()
                }
            }
        }
    }

    private suspend fun onScrollCompleted() {
        applyPendingDeletion()
        applyNextScrollToPage()
        _targetPage.value = null
    }

    fun isMutating() = isPendingInsertion() || isPendingDeletion()

    /* -----------------------------------------------------------------------------------------
     * Insert a new page before
     * ----------------------------------------------------------------------------------------- */

    private var nextScrollToPage: Int? = null

    fun appendPageAndScroll(item: E) {
        if (isMutating()) return

        viewModelScope.launch {
            withLocking {
                val items = _currentItems.toMutableList()
                items.apply { add(size, item) }
                setItemsWithoutLock(items)
                setScrollToPage(items.size)
            }
        }
    }

    fun insertPageBeforeAndScroll(targetPage: Int, item: E) {
        if (isMutating()) return

        viewModelScope.launch {
            withLocking {
                val items = _currentItems.toMutableList()
                items.apply { add(targetPage, item) }
                setItemsWithoutLock(items)

                nextScrollToPage = targetPage

                val refreshCurrentPage = targetPage + 1
                _targetPage.value = refreshCurrentPage
                _focusPage.value = refreshCurrentPage
                _settledPage.value = refreshCurrentPage
            }
        }
    }

    private fun applyNextScrollToPage() {
        nextScrollToPage?.let { page ->
            setScrollToPage(page)
        }
    }

    private fun isPendingInsertion() = nextScrollToPage != null

    /* -----------------------------------------------------------------------------------------
     * Insert a page after
     * ----------------------------------------------------------------------------------------- */

    fun insertPageAfter(page: Int, item: E) = viewModelScope.launch {
        if (isMutating()) return@launch

        withLocking {
            val updatedItems = _currentItems.toMutableList()
            if (updatedItems.isEmpty()) {
                updatedItems.add(0, item)
                setItemsWithoutLock(updatedItems)
                slideToFirst()
            } else {
                val nextPage = page + 1
                updatedItems.add(nextPage, item)
                setItemsWithoutLock(updatedItems)
                setScrollToPage(nextPage)
            }
        }
    }

    /* -----------------------------------------------------------------------------------------
     * Delete the page
     * ----------------------------------------------------------------------------------------- */

    private var pendingItemToDelete: E? = null

    fun deleteAndSlideToPrevious(page: Int, item: E) =
        deleteAndSlide(page, item) { slideToPrevious(it) }

    fun deleteAndSlideToNext(page: Int, item: E) =
        deleteAndSlide(page, item) { slideToNext(page) }

    private fun deleteAndSlide(page: Int, item: E, navigate: (Int) -> Unit) {
        if (isMutating()) return

        viewModelScope.launch {
            if (_currentItems.size == 1) {
                pendingItemToDelete = item
                applyPendingDeletion()
            } else {
                navigate(page)
                pendingItemToDelete = item
                userScrollEnabled.value = false
            }
        }
    }

    private fun isPendingDeletion() = pendingItemToDelete != null

    /**
     * Before removing an item, another page must be displayed to ensure a smooth transition.
     * Once the next page is loaded, the target item can safely be removed.
     *
     * This method is triggered after the sliding animation to the next or previous page is completed.
     */
    private suspend fun applyPendingDeletion() = withLocking {
        val itemToDelete = pendingItemToDelete
        if  (itemToDelete != null) {
            processDeletion(itemToDelete)
            this.pendingItemToDelete = null
        }
        userScrollEnabled.value = true
    }

    protected open suspend fun processDeletion(item: E) {
        val updatedItems = _currentItems.toMutableList()
        val deletedItemAt = updatedItems.indexOf(item)
        if (deletedItemAt > -1) {
            updatedItems.removeAt(deletedItemAt)
            setItemsWithoutLock(updatedItems)

            if (updatedItems.size == 0) {
                _settledPage.value = null
            } else {
                updateCurrentPageAfterDeletion(deletedItemAt, updatedItems.size)
            }
        }
    }

    /**
     * Adjusts the current page index if it is located after a deleted page.
     *
     * When a page is deleted, all subsequent pages shift left. This method ensures that
     * the current page index is updated correctly by decrementing it by 1 if necessary.
     */
    private fun updateCurrentPageAfterDeletion(deletedItemAt: Int, itemsSize: Int) {
        val currentPageAt = targetPage.value ?: return

        val isCurrentPageOnRight = currentPageAt > deletedItemAt
        val isDeletedPageNotLast = deletedItemAt < itemsSize

        if (isCurrentPageOnRight && isDeletedPageNotLast) {
            _focusPage.value = currentPageAt - 1
        }
    }

    /* -----------------------------------------------------------------------------------------
     * Page
     * ----------------------------------------------------------------------------------------- */

    fun slideToFirst() {
        setScrollToPage(0)
    }

    fun slideToPrevious() {
        val page = targetPage.value ?: return
        slideToPrevious(page)
    }

    fun slideToPrevious(page: Int) {
        val previousPage = previousPageIndex(page)
        setScrollToPage(previousPage)
    }

    fun slideToNext(page: Int): Int {
        val nextPage = nextPageIndex(page)
        setScrollToPage(nextPage)
        return nextPage
    }

    private fun nextPageIndex(page: Int): Int = if (isLastPage(page)) 0 else page + 1

    private fun previousPageIndex(page: Int): Int = if (isFirstPage(page)) _currentItems.size - 1 else page - 1

    private fun isFirstPage(page: Int): Boolean = page == 0

    private fun isLastPage(page: Int): Boolean = page >= lastPageIndex()

    private fun lastPageIndex(): Int = getItems().size.minus(1)

    /* -----------------------------------------------------------------------------------------
     * Gets/sets pages
     * ----------------------------------------------------------------------------------------- */

    fun getSettledPage(): Int? = settledPage.value

    fun setSettledPage(page: Int) {
        _settledPage.value = page
    }

    fun setScrollToPage(page: Int) {
        _scrollToPage.value = page
        _targetPage.value = page

        userScrollEnabled.value = false
    }

    fun clearScrollToPage() {
        _scrollToPage.value = null
        nextScrollToPage = null
        userScrollEnabled.value = true
    }

    fun setFocusPage(page: Int) {
        _focusPage.value = page
    }

    fun clearFocusPage() {
        _focusPage.value = null
    }

    /* -----------------------------------------------------------------------------------------
     * Gets/sets
     * ----------------------------------------------------------------------------------------- */

    fun getItem(page: Int): E = _currentItems[page]

    fun getItemOrNull(page: Int): E? = _currentItems.getOrNull(page)

    suspend fun updateItems(update: (items: MutableList<E>) -> List<E>): List<E> =
        mutex.withLock {
            val updatedItems = update(getItems().toMutableList())
            setItemsWithoutLock(updatedItems)
            return@withLock updatedItems
        }

    suspend fun setItems(items: List<E>) = withLocking { setItemsWithoutLock(items) }

    suspend fun withLocking(fn: suspend () -> Unit): Unit = mutex.withLock { fn() }

    private fun setItemsWithoutLock(items: List<E>) {
        _currentItems = items
        _items.value = items
    }

    fun getItems(): List<E> {
        return _currentItems
    }
}