package pl.gocards.ui.cards.slider.view

import androidx.compose.runtime.Composable
import pl.gocards.ui.cards.slider.page.add.NewCardPageMenu
import pl.gocards.ui.cards.slider.page.card.model.CardMode
import pl.gocards.ui.cards.slider.page.card.model.SliderCardUi
import pl.gocards.ui.cards.slider.page.edit.EditCardPageMenu
import pl.gocards.ui.cards.slider.page.study.StudyCardPageMenu

/**
 * @author Grzegorz Ziemski
 */
@Composable
fun CardSliderMenu(
    page: Int,
    sliderCards: List<SliderCardUi>,

    studyPage: StudyPage?,
    editPage: EditPage,
    newPage: NewPage,
) {
    if (sliderCards.size <= page) return
    val sliderCard = sliderCards[page]

    val mode = sliderCard.cardMode.value
    if (mode == CardMode.STUDY && studyPage != null) {
        val studyCard = studyPage.studyCards.value[sliderCard.id]
        if (studyCard != null) {
            StudyCardPageMenu(
                studyPage.editingLocked.value,
                { studyPage.onClickMenuEditCard(page) },
                { studyPage.onClickMenuNewCard(page) },
                { studyPage.onClickMenuDeleteCard(page, sliderCard) },
                { studyPage.onClickMenuResetView(studyCard) },
            )
        }
    } else if (mode == CardMode.NEW) {
        val newCard = newPage.newCards.value[sliderCard.id]
        if (newCard != null) {
            NewCardPageMenu(
                { newPage.onClickMenuSaveNewCard(page, newCard) },
                { newPage.onClickMenuDeleteCard(page, sliderCard) },
                { newPage.onClickMenuNewCard(page) },
            )
        }
    } else if (mode == CardMode.EDIT) {
        val editCard = editPage.editCards.value[sliderCard.id]
        if (editCard != null) {
            EditCardPageMenu(
                { editPage.onClickMenuSaveEditCard(page, editCard) },
                { editPage.onClickMenuDeleteCard(page, sliderCard) },
                { editPage.onClickMenuNewCard(page) },
            )
        }
    }
}