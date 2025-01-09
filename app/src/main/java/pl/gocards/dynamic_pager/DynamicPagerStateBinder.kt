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
 * Source: https://github.com/GoCardsEdu/Dynamic-Pager/blob/main/dynamic-pager/src/main/java/pl/gocards/dynamic_pager/DynamicPagerStateBinder.kt
 * @author Grzegorz Ziemski
 * @version 1.0
 */

package pl.gocards.dynamic_pager

import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow


/**
 * Binds the state of a `PagerState` to a `DynamicPagerUIMediator`.
 *
 * It observes changes in the pager's state, such as the current page, and communicates them to the mediator.
 * Similarly, it listens for commands from the mediator, such as scroll requests or page change requests, and applies them to the `PagerState`.
 *
 * @author Grzegorz Ziemski
 * @version 1.0
 */
@Composable
fun <E> DynamicPagerStateBinder(
    pagerMediator: DynamicPagerUIMediator<E>,
    content: @Composable (PagerState) -> Unit,
) {
    val pagerState = rememberPagerState(
        initialPage = pagerMediator.settledPage.value ?: 0,
        pageCount = { pagerMediator.getSize() }
    )

    LaunchedEffect(pagerState.settledPage) {
        snapshotFlow { pagerState.settledPage }
            .collect {
                pagerMediator.setSettledPage(it)
            }
    }

    LaunchedEffect(pagerMediator.scrollToPage.value) {
        snapshotFlow { pagerMediator.scrollToPage.value }
            .collect {
                if (it != null) {
                    pagerState.animateScrollToPage(it)
                    pagerMediator.clearScrollToPage()
                }
            }
    }

    LaunchedEffect(pagerMediator.focusPage) {
        snapshotFlow { pagerMediator.focusPage.value }
            .collect {
                if (it != null) {
                    pagerState.scrollToPage(it)
                    pagerMediator.clearFocusPage()
                }
            }
    }

    content(pagerState)
}