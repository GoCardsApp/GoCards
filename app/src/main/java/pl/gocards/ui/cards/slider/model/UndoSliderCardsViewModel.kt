package pl.gocards.ui.cards.slider.model

import android.app.Application
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pl.gocards.db.room.DeckDatabase
import pl.gocards.room.util.TimeUtil
import pl.gocards.ui.cards.slider.page.add.model.NewCardsModel
import pl.gocards.ui.cards.slider.page.edit.model.EditCardsModel
import pl.gocards.ui.cards.slider.page.study.model.StudyCardsModel
import pl.gocards.ui.cards.slider.slider.model.Mode
import pl.gocards.ui.cards.slider.slider.model.SliderCardUi
import pl.gocards.ui.cards.slider.slider.model.SliderCardsModel
import pl.gocards.util.FirebaseAnalyticsHelper

/**
 * @author Grzegorz Ziemski
 */
open class UndoSliderCardsViewModel(
    defaultMode: Mode,
    deckDb: DeckDatabase,
    sliderCardsModel: SliderCardsModel,
    studyCardsModel: StudyCardsModel,
    newCardsModel: NewCardsModel,
    editCardsModel: EditCardsModel,
    var analytics: FirebaseAnalyticsHelper,
    application: Application
) : UndoLearningProgressViewModel(
    defaultMode,
    deckDb,
    sliderCardsModel,
    studyCardsModel,
    newCardsModel,
    editCardsModel,
    application
) {

    fun handleOnBackPressed(): Boolean {
        val currentPage = sliderCardsModel.getSettledPage() ?: return false
        val sliderCards = sliderCardsModel.getItems()
        val currentCard = sliderCards[currentPage]

        when (currentCard.mode.value) {
            Mode.EDIT -> {
                return if (defaultMode == Mode.STUDY) {
                    currentCard.mode.value = Mode.STUDY
                    analytics.sliderStudyMode(currentPage)
                    true
                } else {
                    false
                }
            }
            Mode.NEW -> {
                return if (defaultMode == Mode.STUDY) {
                    // TODO Add implementation for EDIT mode
                    viewModelScope.launch(Dispatchers.IO) {
                        sliderCardsModel.deleteAndSlideToPreviousPage(currentPage, currentCard)
                        analytics.sliderDeleteNewCard(currentPage)
                    }
                    true
                } else {
                    false
                }
            }
            else -> {
                val backToCard: SliderCardUi = this.undoCards.pollLast() ?: return false
                if (backToCard.deletedAt != null) {
                    super.restoreDeletedCard(backToCard.ordinal!! - 1, backToCard)
                    analytics.sliderRestoreCard(currentPage)
                } else {
                    revertPreviousLearningProgress(
                        backToCard,
                        currentPage,
                        currentCard,
                        sliderCards
                    )
                    analytics.revertLearningProgress(currentPage)
                }
                return true
            }
        }
    }

    /**
     * C_D_25 Delete the card
     */
    override fun deleteCard(page: Int, sliderCard: SliderCardUi) {
        sliderCard.deletedAt = TimeUtil.getNowEpochSec()
        sliderCard.ordinal = page + 1
        undoCards.add(sliderCard)
        super.deleteCard(page, sliderCard)
    }

    /**
     * C_U_26 Undo card deletion
     */
    fun restoreDeletedCard(sliderCard: SliderCardUi) {
        if (!undoCards.remove(sliderCard)) return
        sliderCard.deletedAt = null
        super.restoreDeletedCard(sliderCard.ordinal!! - 1, sliderCard)
    }
}