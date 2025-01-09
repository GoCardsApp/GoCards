/**
 * @author Grzegorz Ziemski
 */

package pl.gocards.ui.cards.slider.page.card.model

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import pl.gocards.db.room.AppDatabase
import pl.gocards.db.room.DeckDatabase

/**
 * @author Grzegorz Ziemski
 */
class SliderCardManager(
    deckDb: DeckDatabase,
    appDb: AppDatabase,
    application: Application
): MaxForgottenSliderCardManager(deckDb, appDb, application)

class SliderCardManagerFactory(
    private val deckDb: DeckDatabase,
    private val appDb: AppDatabase,
    private val application: Application
): ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(SliderCardManager::class.java) -> {
                SliderCardManager(deckDb, appDb, application) as T
            }
            else -> throw IllegalArgumentException("ViewModel Not Found: ${modelClass.name}")
        }
    }
}