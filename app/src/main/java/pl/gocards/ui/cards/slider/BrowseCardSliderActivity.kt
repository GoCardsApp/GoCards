package pl.gocards.ui.cards.slider

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import pl.gocards.App
import pl.gocards.db.deck.DeckDbUtil
import pl.gocards.ui.cards.slider.model.CardSliderViewModel
import pl.gocards.ui.cards.slider.page.card.model.CardMode
import pl.gocards.ui.cards.slider.view.CardSliderScaffold
import pl.gocards.ui.cards.slider.view.CardSliderUIMediatorFactory
import pl.gocards.ui.filesync_pro.AutoSyncViewModel
import pl.gocards.util.FirebaseAnalyticsHelper

/**
 * @author Grzegorz Ziemski
 */
class BrowseCardSliderActivity : ComponentActivity() {

    companion object {
        const val DECK_DB_PATH = "DECK_DB_PATH"
    }

    private var viewModel: CardSliderViewModel? = null

    private var autoSyncCardsModel: AutoSyncViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val deckDbPath = intent.getStringExtra(DECK_DB_PATH) ?: return
        val owner = this
        val application = application as App
        val analytics = FirebaseAnalyticsHelper.getInstance(application)

        val viewModel = CardSliderViewModel.create(
            this,
            deckDbPath,
            CardMode.STUDY,
            analytics,
            this,
        )
        this.viewModel = viewModel

        autoSyncCardsModel = AutoSyncViewModel.getInstance(deckDbPath, owner, application)

        viewModel.fetchAllCards()
        autoSyncCardsModel?.autoSync {
            viewModel.fetchAllCards()
        }

        setContent {
            CardSliderScaffold(
                CardSliderUIMediatorFactory().getInstance(
                    onBack = { super.finish() },
                    deckName = DeckDbUtil.getDeckName(deckDbPath),
                    viewModel = viewModel,
                    autoSyncCardsModel = autoSyncCardsModel,
                    showRateButtons = false,
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

    override fun onStop() {
        super.onStop()
        viewModel?.onCardPause()
        autoSyncCardsModel?.autoSync()
    }
}