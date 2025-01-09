package pl.gocards.ui.cards.slider.view

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import pl.gocards.ui.cards.slider.page.add.NewCardPage
import pl.gocards.ui.cards.slider.page.card.model.CardMode
import pl.gocards.ui.cards.slider.page.card.model.SliderCardUi
import pl.gocards.ui.cards.slider.page.edit.EditCardPage
import pl.gocards.ui.cards.slider.page.edit.model.EditCardUi
import pl.gocards.ui.cards.slider.page.study.StudyCardLayoutParams
import pl.gocards.ui.cards.slider.page.study.StudyCardPage
import pl.gocards.ui.cards.slider.page.study.model.StudyCardUi
import pl.gocards.ui.cards.slider.page.study.ui.definition.DefinitionButtonsActions

/**
 * @author Grzegorz Ziemski
 */
@Composable
fun CardSliderPager(
    pagerState: PagerState,
    sliderCards: List<SliderCardUi>,
    studyCards: State<Map<Int, StudyCardUi>>?,
    newCards: State<Map<Int, EditCardUi>>,
    editCards: State<Map<Int, EditCardUi>>,
    definitionButtonsActions: DefinitionButtonsActions?,
    studyCardLayoutParams: MutableState<StudyCardLayoutParams>,
    innerPadding: PaddingValues,
    userScrollEnabled: MutableState<Boolean>
) {
    HorizontalPager(
        state = pagerState,
        userScrollEnabled = userScrollEnabled.value,
        flingBehavior = PagerDefaults.flingBehavior(
            state = pagerState,
            snapAnimationSpec = spring(stiffness = Spring.StiffnessHigh),
        ),
    ) { page ->
        if (page >= sliderCards.size) return@HorizontalPager
        val sliderCard = sliderCards[page]
        val id = sliderCard.id
        val mode = sliderCard.cardMode.value

        if (mode == CardMode.STUDY && studyCards != null && definitionButtonsActions != null) {
            val studyCard = studyCards.value[id]
            if (studyCard != null) {
                StudyCardPage(
                    sliderCard = sliderCard,
                    studyCard = studyCard,
                    page = page,
                    pagerState = pagerState,
                    innerPadding = innerPadding,
                    studyCardLayoutParams = studyCardLayoutParams,
                    onScroll = { userScrollEnabled.value = !it },
                    definitionButtonsActions = definitionButtonsActions
                )
            } else {
                ErrorMessage("StudyCard not found", innerPadding)
            }
        } else if (mode == CardMode.NEW) {
            val newCard = newCards.value[id]
            if (newCard != null) {
                NewCardPage(
                    page = page,
                    newCard = newCard,
                    pagerState = pagerState,
                    innerPadding = innerPadding
                )
            } else {
                ErrorMessage("NewCard not found", innerPadding)
            }
        } else if (mode == CardMode.EDIT) {
            val editCard = editCards.value[id]
            if (editCard != null) {
                EditCardPage(
                    page = page,
                    editCard = editCard,
                    pagerState = pagerState,
                    innerPadding = innerPadding
                )
            } else {
                ErrorMessage("EditCard not found", innerPadding)
            }
        } else {
            ErrorMessage("Card mode not recognized.", innerPadding)
        }
    }
}

@Composable
private fun ErrorMessage(text: String, innerPadding: PaddingValues) {
    Box(modifier = Modifier
        .padding(innerPadding)
        .fillMaxSize()) {
        Text(text, Modifier.align(Alignment.Center))
    }
}