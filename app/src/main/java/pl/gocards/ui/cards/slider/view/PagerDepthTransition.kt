package pl.gocards.ui.cards.slider.view

import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.graphics.graphicsLayer
import kotlin.math.abs

/**
 * C_R_22 Swipe the cards left and right
 * @author Grzegorz Ziemski
 */
fun Modifier.pagerDepthTransition(
    page: Int,
    pagerState: PagerState,
    showBorder: MutableState<Boolean>
) = graphicsLayer {
    val pageOffset = pagerState.offsetForPage(page)

    if (pageOffset < 0) {
        rightCard(this, pageOffset, showBorder)
    } else if (pageOffset == 0f) {
        fullPage(showBorder)
    } else if (pageOffset <= 1) {
        leftCard(showBorder, pageOffset)
    }
}

private const val MIN_SCALE = 0.75f

@SuppressWarnings("unused")
private fun rightCard(
    graphics: GraphicsLayerScope,
    pageOffset: Float,
    showBorder: MutableState<Boolean>
) {
    showBorder.value = pageOffset < -0.01f

    // make the page not move
    graphics.translationX = graphics.size.width * pageOffset

    // Fade the page out.
    graphics.alpha = 1 + pageOffset

    // Scale the page down (between MIN_SCALE and 1)
    val scaleFactor: Float = MIN_SCALE + (1 - MIN_SCALE) * (1 - abs(pageOffset))
    graphics.scaleX = scaleFactor
    graphics.scaleY = scaleFactor
}

private fun fullPage(
    @SuppressWarnings("unused")
    showBorder: MutableState<Boolean>
) {
    showBorder.value = false
}

@SuppressWarnings("unused")
private fun leftCard(
    showBorder: MutableState<Boolean>,
    pageOffset: Float
) {
    showBorder.value = pageOffset > 0.01f
}

@SuppressWarnings("unused")
private fun PagerState.offsetForPage(page: Int) = (currentPage - page) + currentPageOffsetFraction