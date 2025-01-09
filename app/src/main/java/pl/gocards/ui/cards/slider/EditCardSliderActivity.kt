package pl.gocards.ui.cards.slider

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import pl.gocards.App
import pl.gocards.db.deck.DeckDbUtil
import pl.gocards.ui.cards.slider.model.CardSliderViewModel
import pl.gocards.ui.cards.slider.page.card.model.CardMode
import pl.gocards.ui.cards.slider.view.CardSliderScaffold
import pl.gocards.ui.cards.slider.view.CardSliderUIMediatorFactory
import pl.gocards.util.FirebaseAnalyticsHelper

/**
 * C_C_24 Edit the card
 * @author Grzegorz Ziemski
 */
class EditCardSliderActivity : ComponentActivity() {

    companion object {
        const val DECK_DB_PATH = "DECK_DB_PATH"
        const val ADD_NEW_CARD = "ADD_NEW_CARD"
        const val NEW_CARD_AFTER_CARD_ID = "NEW_CARD_AFTER_CARD_ID"
        const val EDIT_CARD_ID = "EDIT_CARD_ID"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val deckDbPath = intent.getStringExtra(DECK_DB_PATH) ?: return
        val addNewCard = intent.getBooleanExtra(ADD_NEW_CARD, false)
        val newCardAfterCardId = intent.getIntExtra(NEW_CARD_AFTER_CARD_ID, 0)
        val editCardId = intent.getIntExtra(EDIT_CARD_ID, 0)

        val application = application as App
        val analytics = FirebaseAnalyticsHelper.getInstance(application)
        val viewModel = CardSliderViewModel.create(
            this,
            deckDbPath,
            CardMode.EDIT,
            analytics,
            this
        )


        setContent {

            LaunchedEffect(true) {
                if (!viewModel.hasCards()) {
                    if (addNewCard) {
                        viewModel.fetchAllCardsAndAppendNew()
                    } else if (newCardAfterCardId != 0) {
                        viewModel.fetchAllCardsAndInsertAfter(newCardAfterCardId)
                    } else if (editCardId != 0) {
                        viewModel.fetchAllCardsAndFocusOnCard(editCardId)
                    } else {
                        viewModel.fetchAllCards()
                    }
                }
            }


            CardSliderScaffold(
                CardSliderUIMediatorFactory().getInstance(
                    onBack = { super.finish() },
                    deckName = DeckDbUtil.getDeckName(deckDbPath),
                    viewModel = viewModel,
                    autoSyncCardsModel = null,
                    showRateButtons = true,
                    noMoreCardsToRepeat = { super.finish() },
                    analytics = analytics,
                    application = application
                )
            )
        }

        this.onBackPressedDispatcher.addCallback(this) {
            if (!viewModel.handleOnBackPressed()) super.finish()
        }
    }
}