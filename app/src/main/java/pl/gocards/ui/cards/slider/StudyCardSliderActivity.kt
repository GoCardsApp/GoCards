package pl.gocards.ui.cards.slider

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import pl.gocards.App
import pl.gocards.db.deck.DeckDbUtil
import pl.gocards.ui.cards.slider.model.CardSliderViewModel
import pl.gocards.ui.cards.slider.page.card.model.CardMode
import pl.gocards.ui.cards.slider.view.CardSliderScaffold
import pl.gocards.ui.cards.slider.view.CardSliderUIMediatorFactory
import pl.gocards.ui.filesync_pro.AutoSyncViewModel
import pl.gocards.util.FirebaseAnalyticsHelper


/**
 * C_R_30 Study the cards
 * @author Grzegorz Ziemski
 */
@Immutable
class StudyCardSliderActivity : ComponentActivity() {

    companion object {
        const val DECK_DB_PATH = "DECK_DB_PATH"
        const val RESULT_NO_MORE_CARDS_TO_REPEAT = "NO_MORE_CARDS_TO_REPEAT"
    }

    private var autoSyncCardsModel: AutoSyncViewModel? = null

    private var viewModel: CardSliderViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val deckDbPath = intent.getStringExtra(DECK_DB_PATH) ?: return
        val context = this
        val owner = this
        val application = application as App
        val analytics = FirebaseAnalyticsHelper.getInstance(application)

        val viewModel = CardSliderViewModel.create(
            context,
            deckDbPath,
            CardMode.STUDY,
            analytics,
            this
        )
        this.viewModel = viewModel

        autoSyncCardsModel = AutoSyncViewModel.getInstance(deckDbPath, owner, application)

        autoSyncCardsModel?.autoSync {
            viewModel.fetchForgottenCards()
        }

        setContent {
            LaunchedEffect(true) {
                if (!viewModel.hasCards()) {
                    viewModel.fetchForgottenCards()
                } else {
                    viewModel.resetSettledPage()
                }
            }

            CardSliderScaffold(
                CardSliderUIMediatorFactory().getInstance(
                    onBack = { super.finish() },
                    deckName = DeckDbUtil.getDeckName(deckDbPath),
                    viewModel = viewModel,
                    autoSyncCardsModel = autoSyncCardsModel,
                    showRateButtons = true,
                    noMoreCardsToRepeat = {
                        val returnIntent = Intent()
                        returnIntent.putExtra("RESULT", RESULT_NO_MORE_CARDS_TO_REPEAT)
                        setResult(RESULT_OK, returnIntent)
                        super.finish()
                    },
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
