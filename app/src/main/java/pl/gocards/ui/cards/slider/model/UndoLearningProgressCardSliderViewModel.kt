package pl.gocards.ui.cards.slider.model

import android.app.Application
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pl.gocards.db.room.DeckDatabase
import pl.gocards.room.entity.deck.CardLearningHistory
import pl.gocards.ui.cards.slider.page.add.model.NewCardManager
import pl.gocards.ui.cards.slider.page.card.model.CardMode
import pl.gocards.ui.cards.slider.page.card.model.SliderCardUi
import pl.gocards.ui.cards.slider.page.card.model.SliderCardManager
import pl.gocards.ui.cards.slider.page.edit.model.EditCardManager
import pl.gocards.ui.cards.slider.page.study.model.StudyCardManager
import pl.gocards.util.CardReplayScheduler
import pl.gocards.util.FirebaseAnalyticsHelper
import java.util.Deque
import java.util.LinkedList

/**
 * C_U_39 Undo click on the study buttons.
 * @author Grzegorz Ziemski
 */
open class UndoLearningProgressCardSliderViewModel(
    defaultMode: CardMode,
    deckDb: DeckDatabase,
    sliderCardManager: SliderCardManager,
    studyCardManager: StudyCardManager?,
    newCardManager: NewCardManager,
    editCardManager: EditCardManager,
    analytics: FirebaseAnalyticsHelper,
    application: Application
) : DeleteCardSliderViewModel(
    defaultMode,
    deckDb,
    sliderCardManager,
    studyCardManager,
    newCardManager,
    editCardManager,
    analytics,
    application
) {

    private var lastPage: Int? = null

    /**
     * It is cleared when cards are slided by the user.
     */
    protected val undoCards: Deque<SliderCardUi> = LinkedList()

    /**
     * Undo the first click again, only if it happened during the current study session.
     */
    protected val forgottenInThisSession: MutableList<Int> = mutableListOf()

    protected fun revertPreviousLearningProgress(cardToRestore: SliderCardUi) {
        if (sliderCardManager.isMutating()) return

        viewModelScope.launch(Dispatchers.IO) {
            sliderCardManager.withLocking {
                val existingCard = sliderCardManager.findById(cardToRestore.id)
                val wasAgain: Boolean = existingCard != null
                val targetPage = sliderCardManager.findByOrdinalGreaterThanEqual(cardToRestore)

                revertPreviousLearningProgressInDb(cardToRestore.id)
                studyCardManager?.showDefinition(cardToRestore.id)

                if (wasAgain) {
                    withContext(Dispatchers.Main) {
                        sliderCardManager.setScrollToPage(targetPage)
                    }
                } else {
                    sliderCardManager.restoreCardAndScroll(targetPage, cardToRestore)
                }
                analytics.revertLearningProgress(targetPage)
            }
        }
    }

    private suspend fun revertPreviousLearningProgressInDb(cardId: Int) {
        if (studyCardManager == null) return
        val current = deckDb.cardLearningHistoryKtxDao().findCurrentByCardId(cardId) ?: return
        val previous = findPreviousLearningHistory(current)

        if (previous != null) {
            revertPrevious(current, previous)
        } else {
            revertFirst(current)
        }

        studyCardManager.refreshCard(cardId)
    }

    protected open fun revertFirst(current: CardLearningHistory) {
        if (current.countNotMemorized == 1) {
            revertFirstNoMemorized(current)
        } else {
            revertFirstMemorized(current)
        }
    }

    protected open fun revertFirstNoMemorized(current: CardLearningHistory) {
        if (forgottenInThisSession.contains(current.cardId)) {
            revertFirstMemorized(current)
        } else {
            deckDb.cardLearningProgressDao().updateIsMemorized(current.cardId, false)
            current.interval = CardReplayScheduler.AGAIN_FIRST_INTERVAL_MINUTES
            deckDb.cardLearningHistoryDao().update(current)
        }
    }

    protected open fun revertFirstMemorized(current: CardLearningHistory) {
        deckDb.cardLearningProgressDao().deleteByCardId(current.cardId)
        deleteLearningHistory(current)
    }

    protected open fun revertPrevious(current: CardLearningHistory, previous: CardLearningHistory) {
        if (!previous.wasMemorized!!) {
            revertPreviousNoMemorized(current, previous)
        } else {
            revertPreviousMemorized(current, previous)
        }
    }

    /**
     * Restores when "Again" has been clicked
     */
    protected open fun revertPreviousNoMemorized(
        current: CardLearningHistory,
        previous: CardLearningHistory
    ) {
        if (forgottenInThisSession.contains(current.cardId)) {
            revertPreviousMemorized(current, previous)
        } else {
            deckDb.cardLearningProgressDao().updateIsMemorized(current.cardId, false)
            current.interval = previous.interval
            deckDb.cardLearningHistoryDao().update(current)
        }
    }

    protected open fun revertPreviousMemorized(
        current: CardLearningHistory,
        previous: CardLearningHistory
    ) {
        setPreviousLearningHistoryAsCurrent(previous)
        deleteLearningHistory(current)
    }

    private fun findPreviousLearningHistory(current: CardLearningHistory): CardLearningHistory? {
        return deckDb.cardLearningHistoryDao()
            .findByCardIdAndLearningHistoryId(
                current.cardId,
                current.replayId - 1
            )
    }

    private fun setPreviousLearningHistoryAsCurrent(previous: CardLearningHistory) {
        deckDb.cardLearningProgressDao().update(
            previous.cardId,
            previous.id!!,
            true
        )
        previous.wasMemorized = null
        previous.memorizedDuration = null
        deckDb.cardLearningHistoryDao().update(previous)
    }

    private fun deleteLearningHistory(current: CardLearningHistory) {
        deckDb.cardLearningHistoryDao().delete(current)
    }

    protected fun addUndoCards(sliderCard: SliderCardUi) {
        undoCards.add(sliderCard)
    }
}