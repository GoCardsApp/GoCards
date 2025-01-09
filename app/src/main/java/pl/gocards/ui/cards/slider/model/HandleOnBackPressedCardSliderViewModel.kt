package pl.gocards.ui.cards.slider.model

import android.app.Application
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pl.gocards.db.room.DeckDatabase
import pl.gocards.room.util.TimeUtil
import pl.gocards.ui.cards.slider.page.add.model.NewCardManager
import pl.gocards.ui.cards.slider.page.card.model.CardMode
import pl.gocards.ui.cards.slider.page.card.model.SliderCardManager
import pl.gocards.ui.cards.slider.page.card.model.SliderCardUi
import pl.gocards.ui.cards.slider.page.edit.model.EditCardManager
import pl.gocards.ui.cards.slider.page.study.model.StudyCardManager
import pl.gocards.util.FirebaseAnalyticsHelper

/**
 * @author Grzegorz Ziemski
 */
open class HandleOnBackPressedCardSliderViewModel(
    defaultMode: CardMode,
    deckDb: DeckDatabase,
    sliderCardManager: SliderCardManager,
    studyCardManager: StudyCardManager?,
    newCardManager: NewCardManager,
    editCardManager: EditCardManager,
    analytics: FirebaseAnalyticsHelper,
    application: Application
) : UndoLearningProgressCardSliderViewModel(
    defaultMode,
    deckDb,
    sliderCardManager,
    studyCardManager,
    newCardManager,
    editCardManager,
    analytics,
    application
) {

    fun handleOnBackPressed(): Boolean {
        val currentPage = sliderCardManager.getSettledPage() ?: return false
        val currentCard = sliderCardManager.getItem(currentPage)

        return when (currentCard.cardMode.value) {
            CardMode.EDIT -> {
                handleEditModeBackPress(currentPage, currentCard)
            }

            CardMode.NEW -> {
                handleNewModeBackPress(currentPage, currentCard)
                true
            }

            CardMode.STUDY -> {
                handleStudyModeBackPress(currentCard)
            }
        }
    }

    private fun handleEditModeBackPress(currentPage: Int, currentCard: SliderCardUi): Boolean {
        return when (defaultMode) {
            CardMode.STUDY -> {
                switchToStudyMode(currentPage, currentCard)
                true
            }

            CardMode.EDIT -> {
                processUndoCardsStack()
            }

            else -> false
        }
    }

    private fun handleNewModeBackPress(currentPage: Int, currentCard: SliderCardUi) {
        sliderCardManager.deleteAndSlideToPrevious(currentPage, currentCard)
        analytics.sliderDeleteNewCard(currentPage)
    }

    private fun handleStudyModeBackPress(currentCard: SliderCardUi): Boolean {
        val studyCard = studyCardManager?.getCached(currentCard.id)!!
        return if (studyCard.showDefinition.value) {
            studyCard.showDefinition.value = false
            true
        } else {
            processUndoCardsStack()
        }
    }

    private fun switchToStudyMode(currentPage: Int, currentCard: SliderCardUi) {
        currentCard.cardMode.value = CardMode.STUDY
        analytics.sliderStudyMode(currentPage)
    }

    private fun processUndoCardsStack(): Boolean {
        val cardToRestore: SliderCardUi = this.undoCards.pollLast() ?: return false
        return if (cardToRestore.deletedAt != null) {
            super.restoreDeletedCard(cardToRestore)
            true
        } else {
            revertPreviousLearningProgress(cardToRestore)
            true
        }
    }

    /**
     * C_D_25 Delete the card
     */
    override fun deleteCard(page: Int, sliderCard: SliderCardUi) {
        sliderCard.deletedAt = TimeUtil.getNowEpochSec()
        //sliderCard.ordinal = page + 1
        undoCards.add(sliderCard)
        super.deleteCard(page, sliderCard)
    }

    /**
     * C_U_26 Undo card deletion
     */
    fun restoreLastDeletedCard() {
        val cardToRestore = undoCards.descendingIterator().asSequence()
            .firstOrNull { it.deletedAt != null }
            ?: return

        if (!undoCards.remove(cardToRestore)) return
        super.restoreDeletedCard(cardToRestore)
    }
}