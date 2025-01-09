/**
 * @author Grzegorz Ziemski
 */

package pl.gocards.ui.cards.slider.page.card.model

import android.app.Application
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pl.gocards.db.room.AppDatabase
import pl.gocards.db.room.DeckDatabase
import pl.gocards.room.entity.deck.DeckConfig

/**
 * @author Grzegorz Ziemski
 */
open class MaxForgottenSliderCardManager(
    deckDb: DeckDatabase,
    appDb: AppDatabase,
    application: Application
) : CoreSliderCardsManager(deckDb, appDb, application) {

    companion object {
        private const val NO_LIMIT = 0
    }

    private var maxAllowedForgottenCards = DeckConfig.MAX_ALLOWED_FORGOTTEN_CARDS_DEFAULT

    /**
     * When the number of unmemorized cards reaches [maxAllowedForgottenCards],
     * the card stack is reset, and browsing returns to the first unmemorized card.
     */
    private var forgottenCardCount = 1

    init {
        viewModelScope.launch {
            loadMaxAllowedForgottenCards()
        }
    }

    private suspend fun loadMaxAllowedForgottenCards() {
        maxAllowedForgottenCards = withContext(Dispatchers.IO) {
            deckDb.deckConfigKtxDao().getByKey(DeckConfig.MAX_ALLOWED_FORGOTTEN_CARDS)?.value?.toInt()
                ?: appDb.appConfigKtxDao().getByKey(DeckConfig.MAX_ALLOWED_FORGOTTEN_CARDS)?.value?.toInt()
                ?: DeckConfig.MAX_ALLOWED_FORGOTTEN_CARDS_DEFAULT
        }
    }

    fun forgetAndSlideToNextCard(page: Int): Int {
        return if (hasExceededForgottenCardLimit()) {
            resetForgottenCardCount()
            slideToFirst()
            0
        } else {
            incrementForgottenCardCount()
            slideToNext(page)
        }
    }

    fun hasExceededForgottenCardLimit(): Boolean {
        return maxAllowedForgottenCards != NO_LIMIT && forgottenCardCount >= maxAllowedForgottenCards
    }

    private fun resetForgottenCardCount() {
        forgottenCardCount = 1
    }

    private fun incrementForgottenCardCount() {
        forgottenCardCount++
    }

    fun getMaxAllowedForgottenCards(): Int {
        return maxAllowedForgottenCards
    }
}