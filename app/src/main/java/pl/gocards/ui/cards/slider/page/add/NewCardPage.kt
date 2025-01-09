package pl.gocards.ui.cards.slider.page.add

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import pl.gocards.R
import pl.gocards.ui.cards.slider.page.card.SliderCardPage
import pl.gocards.ui.cards.slider.page.edit.DisabledField
import pl.gocards.ui.cards.slider.page.edit.MultilineEditField
import pl.gocards.ui.cards.slider.page.edit.model.EditCardUi
import pl.gocards.ui.cards.slider.page.edit.testEditCardUi


@Preview(showBackground = true)
@Composable
fun PreviewNewCardPage() {
    NewCardPage(
        page = 0,
        newCard = testEditCardUi(),
        pagerState = rememberPagerState(pageCount = { 1 }),
        innerPadding = PaddingValues(),
    )
}

/**
 * C_C_23 Create a new card
 * @author Grzegorz Ziemski
 */
@Composable
fun NewCardPage(
    page: Int,
    newCard: EditCardUi,
    pagerState: PagerState,
    innerPadding: PaddingValues,
) {
    SliderCardPage(
        page = page,
        pagerState = pagerState,
        modifier = Modifier
            .padding(innerPadding)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            MultilineEditField(R.string.card_new_term_label, newCard.term)
            MultilineEditField(R.string.card_new_definition_label, newCard.definition, 15)
            DisabledField(newCard.disabled)
        }
    }
}