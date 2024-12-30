package pl.gocards.ui.cards.slider.model

import android.app.Application
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pl.gocards.db.room.DeckDatabase
import pl.gocards.ui.cards.slider.page.add.model.NewCardsModel
import pl.gocards.ui.cards.slider.page.edit.model.EditCardsModel
import pl.gocards.ui.cards.slider.page.study.model.StudyCardsModel
import pl.gocards.ui.cards.slider.slider.model.Mode
import pl.gocards.ui.cards.slider.slider.model.SliderCardUi
import pl.gocards.ui.cards.slider.slider.model.SliderCardsModel


/**
 * C_D_25 Delete the card
 * @author Grzegorz Ziemski
 */
open class DeleteSliderCardsViewModel(
    defaultMode: Mode,
    deckDb: DeckDatabase,
    sliderCardsModel: SliderCardsModel,
    studyCardsModel: StudyCardsModel,
    newCardsModel: NewCardsModel,
    editCardsModel: EditCardsModel,
    application: Application
) : BaseSliderCardsViewModel(
    defaultMode,
    deckDb,
    sliderCardsModel,
    studyCardsModel,
    newCardsModel,
    editCardsModel,
    application
) {

    /**
     * C_D_25 Delete the card
     */
    open fun deleteCard(page: Int, sliderCard: SliderCardUi) {
        sliderCardsModel.deleteCardAndSlideToNextPage(page, sliderCard)
    }

    /**
     * C_U_26 Undo card deletion
     */
    protected fun restoreDeletedCard(deletedPage: Int, sliderCard: SliderCardUi) {
        viewModelScope.launch(Dispatchers.IO) {
            sliderCardsModel.restoreDeletedCard(deletedPage, sliderCard)
        }
    }
}