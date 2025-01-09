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
 * C_D_25 Delete the card
 * @author Grzegorz Ziemski
 */
open class DeleteCardSliderViewModel(
    defaultMode: CardMode,
    deckDb: DeckDatabase,
    sliderCardManager: SliderCardManager,
    studyCardManager: StudyCardManager?,
    newCardManager: NewCardManager,
    editCardManager: EditCardManager,
    analytics: FirebaseAnalyticsHelper,
    application: Application
) : CoreCardSliderViewModel(
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
     * C_D_25 Delete the card
     */
    open fun deleteCard(page: Int, sliderCard: SliderCardUi) {
        sliderCardManager.deleteCardAndScrollNext(page, sliderCard)
    }

    /**
     * C_U_26 Undo card deletion
     */
    protected fun restoreDeletedCard(cardToRestore: SliderCardUi) {
        cardToRestore.deletedAt = null

        val targetPage = sliderCardManager.findByOrdinalGreaterThanEqual(cardToRestore)
        viewModelScope.launch(Dispatchers.IO) {
            sliderCardManager.restoreDeletedCard(targetPage, cardToRestore)
        }

        analytics.sliderRestoreCard(targetPage)
    }
}