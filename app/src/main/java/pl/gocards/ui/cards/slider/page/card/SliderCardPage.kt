package pl.gocards.ui.cards.slider.page.card

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import pl.gocards.ui.cards.slider.view.pagerDepthTransition
import pl.gocards.ui.theme.ExtendedTheme

@Composable
fun SliderCardPage(
    page: Int,
    pagerState: PagerState,
    modifier: Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val showBorder: MutableState<Boolean> = remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .pagerDepthTransition(page, pagerState, showBorder)
            .zIndex(-page.toFloat())
            .fillMaxSize()
    ) {
        val cardModifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()

        Card(
            shape = if (showBorder.value) RoundedCornerShape(10.dp) else CardDefaults.shape,
            modifier = if (showBorder.value) cardModifier.border(
                1.dp,
                ExtendedTheme.colors.cardBorder,
                RoundedCornerShape(10.dp)
            ) else cardModifier,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.background
            ),
            content = content
        )
    }
}
