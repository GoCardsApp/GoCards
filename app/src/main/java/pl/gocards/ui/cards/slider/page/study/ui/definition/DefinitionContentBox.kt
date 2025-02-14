package pl.gocards.ui.cards.slider.page.study.ui.definition

import android.annotation.SuppressLint
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import pl.gocards.ui.cards.slider.page.study.model.StudyCardUi
import pl.gocards.ui.cards.slider.page.study.ui.StudyContentBox

/**
 * @author Grzegorz Ziemski
 */
@Composable
fun DefinitionContentBox(
    page: Int,
    pagerState: PagerState,
    modifier: Modifier,
    studyCard: StudyCardUi,
    minSlideToY: Int,
    maxSlideToY: Int,
    sliderTouchSpace: Int,
    height: Int,
    darkMode: Boolean,
    onScroll: (enabled: Boolean) -> Unit
) {
    var finalModifier = modifier
    if (sliderTouchSpace != 0) {
        finalModifier = modifier
            .pointerInput(
                studyCard.id,
                definitionSlideDividerGesture(
                    termHeightPx = studyCard.termHeightPx,
                    minSlideToY = minSlideToY,
                    maxSlideToY = maxSlideToY,
                    sliderTouchSpace = sliderTouchSpace
                )
            )
    }
    StudyContentBox(
        page = page,
        pagerState,
        content = studyCard.definition,
        isSimpleHtml = studyCard.isDefinitionSimpleHtml,
        isFullHtml = studyCard.isDefinitionFullHtml,
        fontSize = studyCard.defFontSize,
        modifier = finalModifier,
        height = height,
        darkMode = darkMode,
        onScroll = onScroll,
    )
}

/**
 * C_U_37 Adjust the term/definition ratio by scrolling the slider.
 */
@SuppressLint("ReturnFromAwaitPointerEventScope")
fun definitionSlideDividerGesture(
    termHeightPx: MutableState<Float?>,
    minSlideToY: Int,
    maxSlideToY: Int,
    sliderTouchSpace: Int
): suspend PointerInputScope.() -> Unit {
    return {
        var enabled = false
        awaitPointerEventScope {
            while (true) {
                val event = awaitPointerEvent()
                val termHeightPxVal = termHeightPx.value ?: 0f
                val y = event.changes.first().position.y + termHeightPxVal

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
                    val sliderTouchSpaceY = termHeightPxVal + sliderTouchSpace
                    if (y < sliderTouchSpaceY) {
                        enabled = true
                    }
                }
            }
        }
    }
}