package pl.gocards.ui.cards.slider.page.study.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import pl.gocards.ui.cards.slider.page.study.model.StudyCardUi
import pl.gocards.ui.common.pxToDp

/**
 * @author Grzegorz Ziemski
 */
@Composable
fun TermBox(
    page: Int,
    pagerState: PagerState,
    studyCard: StudyCardUi,
    minSlideToY: Int,
    maxSlideToY: Int,
    sliderTouchSpace: Int,
    height: Int,
    darkMode: Boolean,
    onScroll: (enabled: Boolean) -> Unit
) {
    val termHeightPxVal = studyCard.termHeightPx.value ?: 0f
    var modifier = Modifier.height(termHeightPxVal.pxToDp())
    if (sliderTouchSpace != 0) {
        modifier = modifier.pointerInput(
            studyCard.id,
            termSlideDividerGesture(
                termHeightPx = studyCard.termHeightPx,
                minSlideToY = minSlideToY,
                maxSlideToY = maxSlideToY,
                sliderTouchSpace = sliderTouchSpace
            )
        )
    }

    StudyContentBox(
        page = page,
        pagerState = pagerState,
        content = studyCard.term,
        isSimpleHtml = studyCard.isTermSimpleHtml,
        isFullHtml = studyCard.isTermFullHtml,
        fontSize = studyCard.termFontSize,
        modifier = modifier,
        height = height,
        onScroll = onScroll,
        darkMode = darkMode
    )
}

/**
 * C_U_37 Adjust the term/definition ratio by scrolling the slider.
 */
@SuppressLint("ReturnFromAwaitPointerEventScope")
private fun termSlideDividerGesture(
    termHeightPx: MutableState<Float?>,
    minSlideToY: Int,
    maxSlideToY: Int,
    sliderTouchSpace: Int
): suspend PointerInputScope.() -> Unit {
    return {
        var enabled = false
        awaitPointerEventScope {
            while (true) {
                val termHeightPxVal = termHeightPx.value ?: 0f
                val event = awaitPointerEvent()
                val y = event.changes.first().position.y

                if (event.type == PointerEventType.Release) {
                    enabled = false
                } else if (enabled) {
                    val isInAllowedSpace = minSlideToY < y && y < maxSlideToY
                    val forbiddenTermHeightPx = termHeightPxVal < minSlideToY || maxSlideToY < termHeightPxVal
                    if (isInAllowedSpace || forbiddenTermHeightPx) {
                        event.changes.forEach { it.consume() }
                        termHeightPx.value = y
                    }
                } else if (event.type == PointerEventType.Press) {
                    val sliderTouchSpaceY = termHeightPxVal - sliderTouchSpace
                    if (sliderTouchSpaceY < y) {
                        enabled = true
                    }
                }
            }
        }
    }
}