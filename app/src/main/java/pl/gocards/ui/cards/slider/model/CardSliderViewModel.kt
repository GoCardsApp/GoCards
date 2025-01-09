package pl.gocards.ui.cards.slider.model

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import pl.gocards.App
import pl.gocards.db.app.AppDbUtil
import pl.gocards.db.deck.AppDeckDbUtil
import pl.gocards.db.room.DeckDatabase
import pl.gocards.ui.cards.slider.page.add.model.NewCardManager
import pl.gocards.ui.cards.slider.page.add.model.NewCardsManagerFactory
import pl.gocards.ui.cards.slider.page.card.model.CardMode
import pl.gocards.ui.cards.slider.page.card.model.SliderCardManager
import pl.gocards.ui.cards.slider.page.card.model.SliderCardManagerFactory
import pl.gocards.ui.cards.slider.page.edit.model.EditCardManager
import pl.gocards.ui.cards.slider.page.edit.model.EditCardsManagerFactory
import pl.gocards.ui.cards.slider.page.study.model.StudyCardManager
import pl.gocards.util.FirebaseAnalyticsHelper

/**
 * @author Grzegorz Ziemski
 */
class CardSliderViewModel(
    defaultMode: CardMode,
    deckDb: DeckDatabase,
    sliderCardManager: SliderCardManager,
    studyCardManager: StudyCardManager?,
    newCardManager: NewCardManager,
    editCardManager: EditCardManager,
    analytics: FirebaseAnalyticsHelper,
    application: Application
) : LearningProgressCardSliderViewModel(
    defaultMode,
    deckDb,
    sliderCardManager,
    studyCardManager,
    newCardManager,
    editCardManager,
    analytics,
    application
) {
    companion object {

        fun create(
            context: Context,
            deckDbPath: String,
            defaultMode: CardMode,
            analytics: FirebaseAnalyticsHelper,
            viewModelStoreOwner: ViewModelStoreOwner
        ): CardSliderViewModel {
            val application = context.applicationContext as App
            val deckDb = AppDeckDbUtil.getInstance(context).getDatabase(context, deckDbPath)
            val appDb = AppDbUtil.getInstance(context).getDatabase(context)

            val sliderCardManager = SliderCardManagerFactory(deckDb, appDb, application).let {
                ViewModelProvider(viewModelStoreOwner, it)[SliderCardManager::class.java]
            }

            val newCardManager = NewCardsManagerFactory(deckDb, appDb, deckDbPath, application).let {
                ViewModelProvider(viewModelStoreOwner, it)[NewCardManager::class.java]
            }

            val editCardManager = EditCardsManagerFactory(deckDb, appDb, deckDbPath, application).let {
                ViewModelProvider(viewModelStoreOwner, it)[EditCardManager::class.java]
            }

            /**
             * StudyCardManager shouldn't persist across configuration changes, such as screen rotations.
             * Display settings, like termHeight, need to be recalculated.
             */
            val studyCardManager = if (defaultMode == CardMode.STUDY) StudyCardManager(
                deckDb,
                appDb,
                deckDbPath
            ) else null

            return CardSliderViewModel(
                defaultMode,
                deckDb,
                sliderCardManager,
                studyCardManager,
                newCardManager,
                editCardManager,
                analytics,
                application
            )
        }
    }
}