package pl.gocards.ui.cards.slider.page.study.model

import android.annotation.SuppressLint
import pl.gocards.db.room.AppDatabase
import pl.gocards.db.room.DeckDatabase
import pl.gocards.room.entity.deck.CardLearningHistory
import pl.gocards.room.entity.deck.CardLearningProgress
import pl.gocards.room.entity.deck.CardLearningProgressAndHistory
import pl.gocards.room.util.TimeUtil
import pl.gocards.ui.cards.slider.page.card.model.SliderCardUi
import pl.gocards.util.CardReplayScheduler

/**
 * C_R_30 Study the cards
 * @author Grzegorz Ziemski
 */
abstract class LearningProgressStudyCardsManager(
    deckDb: DeckDatabase,
    appDb: AppDatabase,
    deckDbPath: String
) : DisplaySettingsStudyCardsManager(deckDb, appDb, deckDbPath) {

    private val cardReplayScheduler = CardReplayScheduler()

    @SuppressLint("CheckResult")
    suspend fun getCurrent(cardId: Int): CardLearningProgressAndHistory? {
        return deckDb.cardLearningProgressAndHistoryKtxDao().findCurrentByCardId(cardId)
    }

    suspend fun getNextAfterAgain(cardId: Int): CardLearningProgressAndHistory {
        val current = deckDb.cardLearningProgressAndHistoryKtxDao().findCurrentByCardId(cardId)
        return if (current == null) {
            cardReplayScheduler.scheduleFirstAgainReplay(cardId)
        } else {
            cardReplayScheduler.scheduleAgainNextReplay(current)
        }
    }

    suspend fun getNextAfterQuick(cardId: Int): CardLearningProgressAndHistory? {
        val current = deckDb.cardLearningProgressAndHistoryKtxDao().findCurrentByCardId(cardId)
        return if (current == null) {
            cardReplayScheduler.scheduleNextQuickReplay(cardId)
        } else {
            scheduleNextReplayAfterQuick(current)
        }
    }

    suspend fun getNextAfterEasy(cardId: Int): CardLearningProgressAndHistory {
        val current = deckDb.cardLearningProgressAndHistoryKtxDao().findCurrentByCardId(cardId)
        return if (current == null) {
            cardReplayScheduler.scheduleFirstEasyReplay(cardId)
        } else {
            cardReplayScheduler.scheduleEasyNextReplay(current)
        }
    }

    suspend fun getNextAfterHard(cardId: Int): CardLearningProgressAndHistory {
        val current = deckDb.cardLearningProgressAndHistoryKtxDao().findCurrentByCardId(cardId)
        return if (current == null) {
            cardReplayScheduler.scheduleFirstHardReplay(cardId)
        } else {
            cardReplayScheduler.scheduleHardNextReplay(current)
        }
    }

    /**
     * RPL.7 The "Quick Repetition" is clicked and "Again" was clicked previously.
     *
     * The quick can be only used when the card has never been memorized yet.
     */
    private fun scheduleNextReplayAfterQuick(current: CardLearningProgressAndHistory): CardLearningProgressAndHistory? {
        val isMemorized = current.progress.isMemorized
        val emptyNextReplayAt = current.history.nextReplayAt == null
        val showOnlyFirstTime = !isMemorized && emptyNextReplayAt && current.history.replayId == 1
        return if (showOnlyFirstTime) {
            cardReplayScheduler.scheduleNextQuickReplay(current)
        } else {
            null
        }
    }

    /**
     * C_U_32 Again
     */
    suspend fun onAgainClick(sliderCard: SliderCardUi) {
        val cardId = sliderCard.id
        val studyCard = cards.value[cardId] ?: return

        onAgainClick(studyCard)
        saveDisplaySettings(cardId)
        refreshCard(cardId)
        hideDefinition(cardId)
    }

    /**
     * C_U_32 Again
     */
    private suspend fun onAgainClick(card: StudyCardUi) {
        val now = TimeUtil.getNowEpochSec()
        val next = card.nextAfterAgain
        val current = card.current
        return if (current != null) {
            val previous = cardReplayScheduler.onAgainUpdatePrevious(current, now)
            updateLearningProgress(previous, next, now)
        } else {
            updateLearningProgress(null, next, now)
        }
    }

    /**
     * C_U_33 Quick Repetition (5 min)
     */
    suspend fun onQuickClick(sliderCard: SliderCardUi) {
        val studyCard = cards.value[sliderCard.id] ?: return
        onQuickClick(studyCard)
    }

    /**
     * C_U_33 Quick Repetition (5 min)
     */
    private suspend fun onQuickClick(card: StudyCardUi) {
        val now = TimeUtil.getNowEpochSec()
        val next = card.nextAfterQuick ?: return
        return updateLearningProgress(null, next, now)
    }

    /**
     * C_U_35 Easy (5 days)
     */
    suspend fun onEasyClick(sliderCard: SliderCardUi) {
        val studyCard = cards.value[sliderCard.id] ?: return
        onEasyClick(studyCard)
    }

    /**
     * C_U_35 Easy (5 days)
     */
    private suspend fun onEasyClick(card: StudyCardUi) {
        val now = TimeUtil.getNowEpochSec()
        val next = card.nextAfterEasy
        val current = card.current
        return if (current != null) {
            val previous = cardReplayScheduler.onEasyUpdatePrevious(current, now)
            updateLearningProgress(previous, next, now)
        } else {
            updateLearningProgress(null, next, now)
        }
    }

    /**
     * C_U_34 Hard (3 days)
     */
    suspend fun onHardClick(sliderCard: SliderCardUi) {
        val studyCard = cards.value[sliderCard.id] ?: return
        onHardClick(studyCard)
    }

    /**
     * C_U_34 Hard (3 days)
     */
    private suspend fun onHardClick(card: StudyCardUi) {
        val now = TimeUtil.getNowEpochSec()
        val next = card.nextAfterHard
        val current = card.current
        return if (current != null) {
            val previous = cardReplayScheduler.onHardUpdatePrevious(current, now)
            updateLearningProgress(previous, next, now)
        } else {
            updateLearningProgress(null, next, now)
        }
    }

    private suspend fun updateLearningProgress(
        previous: CardLearningHistory?,
        next: CardLearningProgressAndHistory,
        now: Long
    ) {
        updatePrevious(previous)
        saveNext(next, now)
        appDb.deckDao().refreshLastUpdatedAt(deckDbPath)
    }

    private suspend fun updatePrevious(previous: CardLearningHistory?) {
        if (previous != null) {
            deckDb.cardLearningHistoryKtxDao().updateAll(previous)
        }
    }

    private suspend fun saveNext(next: CardLearningProgressAndHistory, now: Long) {
        val progress = next.progress
        val history = next.history
        if (history.id == null) {
            history.createdAt = now
            val learningHistoryId = deckDb.cardLearningHistoryKtxDao().insert(history)
            progress.cardLearningHistoryId = learningHistoryId.toInt()
            updateLearningProgress(progress)
        } else {
            deckDb.cardLearningProgressKtxDao().updateAll(progress)
            deckDb.cardLearningHistoryKtxDao().updateAll(history)
        }
    }

    private suspend fun updateLearningProgress(current: CardLearningProgress) {
        if (deckDb.cardLearningProgressKtxDao().exists(current.cardId)) {
            deckDb.cardLearningProgressKtxDao().update(
                current.cardId,
                current.cardLearningHistoryId,
                current.isMemorized
            )
        } else {
            deckDb.cardLearningProgressKtxDao().insert(current)
        }
    }
}