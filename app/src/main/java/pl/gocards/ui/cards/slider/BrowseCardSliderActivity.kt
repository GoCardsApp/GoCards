package pl.gocards.ui.cards.slider

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import pl.gocards.App
import pl.gocards.db.deck.DeckDbUtil
import pl.gocards.ui.cards.slider.model.SliderCardsViewModel
import pl.gocards.ui.cards.slider.slider.CardSliderScaffold
import pl.gocards.ui.cards.slider.slider.CardSliderScaffoldInputFactory
import pl.gocards.ui.cards.slider.slider.model.Mode
import pl.gocards.ui.filesync_pro.AutoSyncViewModel
import pl.gocards.util.FirebaseAnalyticsHelper

/**
 * @author Grzegorz Ziemski
 */
class BrowseCardSliderActivity : ComponentActivity() {

    companion object {
        const val DECK_DB_PATH = "DECK_DB_PATH"
    }

    private lateinit var viewModel: SliderCardsViewModel

    private var autoSyncCardsModel: AutoSyncViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            enableEdgeToEdge()
        }

        val deckDbPath = intent.getStringExtra(DECK_DB_PATH) ?: return
        val owner = this
        val application = application as App
        val analytics = FirebaseAnalyticsHelper.getInstance(application)

        viewModel = SliderCardsViewModel.getInstance(
            this,
            deckDbPath,
            Mode.STUDY,
            analytics,
            this,
        )

        autoSyncCardsModel = AutoSyncViewModel.getInstance(deckDbPath, owner, application)

        viewModel.loadAllCards()
        autoSyncCardsModel?.autoSync {
            viewModel.loadAllCards()
        }

        setContent {
            CardSliderScaffold(
                CardSliderScaffoldInputFactory().getInstance(
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
    }

    override fun onStop() {
        super.onStop()
        viewModel.saveCard()
        autoSyncCardsModel?.autoSync { }
    }
}