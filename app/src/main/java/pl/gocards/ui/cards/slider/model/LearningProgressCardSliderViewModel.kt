package pl.gocards.ui.cards.slider.model

import android.app.Application
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pl.gocards.db.room.DeckDatabase
import pl.gocards.ui.cards.slider.page.add.model.NewCardManager
import pl.gocards.ui.cards.slider.page.card.model.CardMode
import pl.gocards.ui.cards.slider.page.card.model.SliderCardUi
import pl.gocards.ui.cards.slider.page.card.model.SliderCardManager
import pl.gocards.ui.cards.slider.page.edit.model.EditCardManager
import pl.gocards.ui.cards.slider.page.study.model.StudyCardManager
import pl.gocards.util.FirebaseAnalyticsHelper

/**
 * @author Grzegorz Ziemski
 */
open class LearningProgressCardSliderViewModel(
    defaultMode: CardMode,
    deckDb: DeckDatabase,
    sliderCardManager: SliderCardManager,
    studyCardManager: StudyCardManager?,
    newCardManager: NewCardManager,
    editCardManager: EditCardManager,
    analytics: FirebaseAnalyticsHelper,
    application: Application
) : HandleOnBackPressedCardSliderViewModel(
    defaultMode,
    deckDb,
    sliderCardManager,
    studyCardManager,
    newCardManager,
    editCardManager,
    analytics,
    application
) {

    /**
     * C_U_32 Again
     */
    fun onAgainClick(page: Int, sliderCard: SliderCardUi) {
        if (studyCardManager == null) return

        addUndoCards(sliderCard)
        viewModelScope.launch(Dispatchers.IO) {
            addForgottenInThisSession(sliderCard.id)
            studyCardManager.onAgainClick(sliderCard)
        }
        sliderCardManager.forgetAndSlideToNextCard(page)
    }

    private fun addForgottenInThisSession(id: Int) {
        if (studyCardManager == null) return

        val studyCard = studyCardManager.getCached(id)!!
        val current = studyCard.current

        if (current == null) {
            val cardId = studyCard.nextAfterAgain.progress.cardId
            forgottenInThisSession.add(cardId)
        } else if (current.progress.isMemorized) {
            val cardId = current.progress.cardId
            forgottenInThisSession.add(cardId)
        }
    }

    /**
     * C_U_33 Quick Repetition (5 min)
     */
    fun onQuickClick(page: Int, sliderCard: SliderCardUi) {
        if (studyCardManager == null) return

        addUndoCards(sliderCard)
        viewModelScope.launch(Dispatchers.IO) {
            studyCardManager.onQuickClick(sliderCard)
            sliderCardManager.deleteAndSlideToNext(page, sliderCard)
        }
    }

    /**
     * C_U_35 Easy (5 days)
     */
    fun onEasyClick(page: Int, sliderCard: SliderCardUi) {
        if (studyCardManager == null) return

        addUndoCards(sliderCard)
        viewModelScope.launch(Dispatchers.IO) {
            studyCardManager.onEasyClick(sliderCard)
            sliderCardManager.deleteAndSlideToNext(page, sliderCard)
        }
    }

    /**
     * C_U_34 Hard (3 days)
     */
    fun onHardClick(page: Int, sliderCard: SliderCardUi) {
        if (studyCardManager == null) return

        addUndoCards(sliderCard)
        viewModelScope.launch(Dispatchers.IO) {
            studyCardManager.onHardClick(sliderCard)
            sliderCardManager.deleteAndSlideToNext(page, sliderCard)
        }
    }
}