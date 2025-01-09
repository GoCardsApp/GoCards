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
 * Source: https://github.com/GoCardsEdu/Dynamic-Pager/blob/main/dynamic-pager/src/main/java/pl/gocards/dynamic_pager/DynamicPagerUIMediator.kt
 * @author Grzegorz Ziemski
 * @version 1.0
 */

package pl.gocards.dynamic_pager

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf

/**
 * A mediator between the Compose view and the ViewModel.
 *
 * This class facilitates communication between the `PagerState` and the ViewModel.
 * It allows the ViewModel to send signals to the pager to change pages, either with sliding
 * animations or instantly without animation. Additionally, it ensures the ViewModel listens
 * to updates from the `PagerState`.
 *
 * @author Grzegorz Ziemski
 * @version 1.0
 */
data class DynamicPagerUIMediator<E>(
    val items: State<List<E>?>,

    val settledPage: State<Int?>,
    val setSettledPage: (Int) -> Unit,

    val scrollToPage: State<Int?> = mutableStateOf(null),
    val clearScrollToPage: () -> Unit,

    val focusPage: State<Int?> = mutableStateOf(null),
    val clearFocusPage: () -> Unit,

    val userScrollEnabled: MutableState<Boolean> = mutableStateOf(false)
) {

    fun getSize(): Int {
        return items.value?.size ?: 0
    }

    companion object {

        @Composable
        fun <E> create(viewModel: DynamicPagerViewModel<E>): DynamicPagerUIMediator<E> {
            return DynamicPagerUIMediator(
                items = viewModel.items,

                settledPage = viewModel.settledPage.collectAsState(),
                setSettledPage = { viewModel.setSettledPage(it) },

                scrollToPage = viewModel.scrollToPage.collectAsState(),
                clearScrollToPage = { viewModel.clearScrollToPage() },

                focusPage = viewModel.focusPage.collectAsState(null),
                clearFocusPage = { viewModel.clearFocusPage() },

                userScrollEnabled = viewModel.userScrollEnabled
            )
        }
    }
}