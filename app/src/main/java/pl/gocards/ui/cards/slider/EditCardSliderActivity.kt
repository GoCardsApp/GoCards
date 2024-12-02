package pl.gocards.ui.cards.slider

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import pl.gocards.App
import pl.gocards.db.deck.DeckDbUtil
import pl.gocards.ui.cards.slider.model.SliderCardsViewModel
import pl.gocards.ui.cards.slider.slider.CardSliderScaffold
import pl.gocards.ui.cards.slider.slider.CardSliderScaffoldInputFactory
import pl.gocards.ui.cards.slider.slider.model.Mode
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            enableEdgeToEdge()
        }

        val deckDbPath = intent.getStringExtra(DECK_DB_PATH) ?: return
        val addNewCard = intent.getBooleanExtra(ADD_NEW_CARD, false)
        val newCardAfterCardId = intent.getIntExtra(NEW_CARD_AFTER_CARD_ID, 0)
        val editCardId = intent.getIntExtra(EDIT_CARD_ID, 0)

        val application = application as App
        val analytics = FirebaseAnalyticsHelper.getInstance(application)
        val viewModel = SliderCardsViewModel.getInstance(
            this,
            deckDbPath,
            Mode.EDIT,
            analytics,
            this
        )


        setContent {

            LaunchedEffect(true) {
                if (!viewModel.isLoaded()) {
                    if (addNewCard) {
                        viewModel.loadAllCardsAndAddNewCard()
                    } else if (newCardAfterCardId != 0) {
                        viewModel.loadAllCardsAndAddNewCard(newCardAfterCardId)
                    } else if (editCardId != 0) {
                        viewModel.loadAllCardsAndSetCardId(editCardId)
                    } else {
                        viewModel.loadAllCards()
                    }
                }
            }


            CardSliderScaffold(
                CardSliderScaffoldInputFactory().getInstance(
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
    }
}