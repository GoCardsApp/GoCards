/**
 * C_C_23 Create a new card
 * @author Grzegorz Ziemski
 */

package pl.gocards.ui.cards.slider.page.add.model

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pl.gocards.db.room.AppDatabase
import pl.gocards.db.room.DeckDatabase
import pl.gocards.room.entity.deck.Card
import pl.gocards.room.entity.deck.Card.Companion.setHtmlFlags
import pl.gocards.room.util.TimeUtil
import pl.gocards.ui.cards.slider.page.edit.model.EditCardUi

/**
 * C_C_23 Create a new card
 * @author Grzegorz Ziemski
 */
class NewCardManager internal constructor(
    val deckDb: DeckDatabase,
    val appDb: AppDatabase,
    val deckDbPath: String,
    application: Application
): AndroidViewModel(application) {

    private val _cards = mutableStateOf<Map<Int, EditCardUi>>(emptyMap())
    val cards: State<Map<Int, EditCardUi>> get() = _cards

    private var lastCardId: Int = 0

    suspend fun addNewCard(): Int = withContext(Dispatchers.IO) {
        val cardId = generateNextCardId()

        _cards.value = _cards.value
            .toMutableMap()
            .apply { this[cardId] = EditCardUi(id = cardId) }

        return@withContext cardId
    }

    suspend fun saveCard(card: EditCardUi, ordinal: Int): Int {
        val updatedAt = TimeUtil.getNowEpochSec()

        val cardId = deckDb.cardKtxDao().insertAfter(
            mapToCardEntity(card),
            ordinal,
            updatedAt
        ).toInt()

        appDb.deckKtxDao().refreshLastUpdatedAt(deckDbPath, updatedAt)
        removeCardFromCache(cardId)
        return cardId
    }

    private fun mapToCardEntity(card: EditCardUi): Card {
        return Card(
            term = card.term.value,
            definition = card.definition.value,
            disabled = card.disabled.value
        ).apply { setHtmlFlags(this) }
    }

    private fun removeCardFromCache(id: Int) {
        _cards.value = _cards.value.toMutableMap().apply {
            remove(id)
        }
    }

    private suspend fun generateNextCardId(): Int {
        if (lastCardId == 0) {
            val idsPoolSize = 1000
            lastCardId = deckDb.cardKtxDao().lastId() + idsPoolSize
        }
        return ++lastCardId
    }
}

/**
 * C_C_23 Create a new card
 * @author Grzegorz Ziemski
 */
class NewCardsManagerFactory(
    val deckDb: DeckDatabase,
    val appDb: AppDatabase,
    val deckDbPath: String,
    val application: Application
): ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(NewCardManager::class.java) -> {
                NewCardManager(deckDb, appDb, deckDbPath, application) as T
            }
            else -> throw IllegalArgumentException("ViewModel Not Found: ${modelClass.name}")
        }
    }
}