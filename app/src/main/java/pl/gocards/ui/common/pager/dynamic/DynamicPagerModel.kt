package pl.gocards.ui.common.pager.dynamic

import android.app.Application
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Collections

/**
 * @author Grzegorz Ziemski
 */
open class DynamicPagerModel<E>(
    application: Application
): AndroidViewModel(application) {

    private val targetPage = MutableLiveData<Int?>(null)
    private val settledPage = MutableLiveData<Int?>(null)
    private val animateScrollToPage = MutableLiveData<Int?>(null)
    private val changePage = MutableLiveData<Int?>(null)

    init {
        viewModelScope.launch {
            settledPage.asFlow().collect {
                if (it != null && it == targetPage.value) {
                    onScrollCompleted()
                }
            }
        }
    }

    private fun onScrollCompleted() {
        deleteWaitingItem()
        animateScrollToPendingPage()
    }

    val items = mutableStateOf<List<E>>(emptyList())
    val scrollEnabled: MutableState<Boolean> = mutableStateOf(true)

    /* -----------------------------------------------------------------------------------------
     * Add a new page
     * ----------------------------------------------------------------------------------------- */

    fun addNewPage(page: Int, item: E, items: MutableList<E>) {
        val newItems = items
        val nextPage = page + 1
        newItems.add(nextPage, item)
        this.items.value = newItems
        viewModelScope.launch {
            trySlideToNextPage(page)
        }
    }

    /* -----------------------------------------------------------------------------------------
     * Delete page
     * ----------------------------------------------------------------------------------------- */

    private var waitingToDeleteItem: MutableList<E> = Collections.synchronizedList(mutableListOf<E>())

    fun deleteAndSlideToPreviousPage(page: Int, item: E) {
        synchronized (items) {
            synchronized(waitingToDeleteItem) {
                slideToPreviousPage(page)
                waitingToDeleteItem.add(item)
                scrollEnabled.value = false
            }
        }
    }

    fun deleteAndSlideToNextPage(page: Int, item: E) {
        synchronized (items) {
            synchronized(waitingToDeleteItem) {

                if (waitingToDeleteItem.contains(item)) return
                val items = this.items.value

                if (items.size == 1) {
                    waitingToDeleteItem.add(item)
                    deleteWaitingItem()
                } else {
                    viewModelScope.launch {
                        slideToNextPage(page)
                        waitingToDeleteItem.add(item)
                        scrollEnabled.value = false
                    }
                }
            }
        }
    }

    /**
     * When deleting an item, you must first show another card,
     * once loaded, the card can be removed.
     *
     * The method is executed after the sliding is completed.
     */
    protected fun deleteWaitingItem() {
        synchronized(items) {
            synchronized(waitingToDeleteItem) {
                while (true) {
                    if (this.waitingToDeleteItem.size == 0) break

                    val waitingToDeleteItem = this.waitingToDeleteItem.removeAt(0)
                    val items = items.value.toMutableList()
                    val deletedPage = items.indexOf(waitingToDeleteItem)
                    if (deletedPage > -1) {
                        items.removeAt(deletedPage)
                        this.items.value = items
                        if (items.size == 0) {
                            viewModelScope.launch {
                                targetPage.value = null
                                settledPage.value = null
                            }
                        } else {
                            /**
                             * If the current page is after a deleted page,
                             * the current page must be refreshed as page - 1
                             */
                            val currentPage = targetPage.value ?: return
                            val isCurrentPageOnRight = currentPage > deletedPage
                            val isDeletedPageNotLast = deletedPage < items.size
                            if (isCurrentPageOnRight && isDeletedPageNotLast) {
                                changePage.value = currentPage - 1
                            }
                        }
                    }
                }
                scrollEnabled.value = true
            }
        }
    }

    /* -----------------------------------------------------------------------------------------
     * Restore the page
     * ----------------------------------------------------------------------------------------- */

    private var pendingAnimateScrollToPage: Int? = null

    fun restorePage(deletedPage: Int, item: E) {
        val currentPage: Int = getNextPage(deletedPage)
        val wasRevertPageLast = currentPage == 0 && deletedPage != 0

        if (wasRevertPageLast) {
            restorePageAsLast(item)
        } else {
            restorePageAtMiddle(deletedPage, item)
        }
    }

    private fun restorePageAsLast(item: E) {
        val items = items.value.toMutableList()
        val lastPosition = items.size
        items.add(lastPosition, item)
        this.items.value = items

        viewModelScope.launch {
            targetPage.value = lastPosition
            animateScrollToPage.value = lastPosition
        }
    }

    private fun restorePageAtMiddle(deletedPage: Int, item: E) {
        val items = items.value.toMutableList()
        items.add(deletedPage, item)

        this.items.value = items
        pendingAnimateScrollToPage = deletedPage

        viewModelScope.launch {
            targetPage.value = deletedPage + 1
            changePage.value = deletedPage + 1
        }
    }

    private fun animateScrollToPendingPage() {
        val scrollToPage = pendingAnimateScrollToPage
        if (scrollToPage != null) {
            this.pendingAnimateScrollToPage = null
            animateScrollToPage.value = scrollToPage
        }
    }

    /* -----------------------------------------------------------------------------------------
     * Page
     * ----------------------------------------------------------------------------------------- */

    fun slideToNextPage(page: Int): Int {
        val nextPage = getNextPage(page)
        animateScrollToPage.value = nextPage
        return nextPage
    }

    fun slideToPreviousPage() {
        val page = targetPage.value ?: return
        slideToPreviousPage(page)
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun slideToPreviousPage(page: Int) {
        val previousPage = getPreviousPage(page)
        animateScrollToPage.value = previousPage
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun getNextPage(page: Int): Int {
        return if (isLastPage(page)) 0 else page + 1
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun getPreviousPage(page: Int): Int {
        val items = this.items.value
        return if (isFirstPage(page)) items.size else page - 1
    }

    @Suppress("SameReturnValue")
    fun slideToFirstPage(): Int {
        animateScrollToPage.value = 0
        return 0
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun trySlideToNextPage(page: Int) {
        animateScrollToPage.value = page + 1
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun isFirstPage(page: Int): Boolean {
        return page == 0
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun isLastPage(page: Int): Boolean {
        return page >= getLastPage()
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun getLastPage(): Int {
        return (items.value.size.minus(1))
    }

    /* -----------------------------------------------------------------------------------------
     * Gets/sets pages
     * ----------------------------------------------------------------------------------------- */

    fun getSettledPageLiveData(): LiveData<Int?> {
        return settledPage
    }

    fun getSettledPage(): Int? {
        return settledPage.value
    }

    fun setSettledPage(page: Int) {
        viewModelScope.launch {
            settledPage.value = page
            targetPage.value = page
        }
    }

    fun getAnimateScrollToPageLiveData(): LiveData<Int?> {
        return animateScrollToPage
    }

    suspend fun setAnimateScrollToPage(page: Int) {
        withContext(Dispatchers.Main) {
            animateScrollToPage.value = page
        }
    }

    fun clearAnimateScrollToPage() {
        animateScrollToPage.value = null
    }

    fun getChangePageLiveData(): LiveData<Int?> {
        return changePage
    }

    suspend fun setChangePagerPage(page: Int) {
        withContext(Dispatchers.Main) {
            changePage.value = page
        }
    }

    fun clearChangePage() {
        changePage.value = null
    }

    /* -----------------------------------------------------------------------------------------
     * Gets/sets
     * ----------------------------------------------------------------------------------------- */

    fun getItem(page: Int): E? {
        return items.value[page]
    }
}