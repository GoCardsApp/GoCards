package pl.gocards.ui.cards.slider.page.edit.model

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import pl.gocards.db.room.AppDatabase
import pl.gocards.db.room.DeckDatabase
import pl.gocards.room.entity.deck.Card
import pl.gocards.room.util.TimeUtil

/**
 * C_C_24 Edit the card
 * @author Grzegorz Ziemski
 */
class EditCardManager(
    val deckDb: DeckDatabase,
    val appDb: AppDatabase,
    val deckDbPath: String,
    application: Application
): AndroidViewModel(application) {

    private var _cardsInternal: MutableMap<Int, EditCardUi> = mutableMapOf()
    private val _cards = mutableStateOf(mapOf<Int, EditCardUi>())
    val cards: State<Map<Int, EditCardUi>> get() = _cards

    suspend fun fetchCardIfNeeded(id: Int) {
        if (!_cardsInternal.containsKey(id)) {
            fetchCard(id)
        }
    }

    private suspend fun fetchCard(id: Int) {
        val cardDb = deckDb.cardKtxDao().getCard(id)!!
        cacheCard(mapCard(cardDb))
    }

    private suspend fun mapCard(card: Card): EditCardUi {
        val cardId = card.id!!
        val learningHistory = deckDb.cardLearningHistoryKtxDao().findCurrentByCardId(cardId)

        return EditCardUi(
            id = cardId,
            term = mutableStateOf(card.term) ,
            definition = mutableStateOf(card.definition),
            nextReplayAt = learningHistory?.nextReplayAt,
            disabled = mutableStateOf(card.disabled)
        )
    }

    suspend fun persistCard(editCard: EditCardUi) {
        val updatedAt = TimeUtil.getNowEpochSec()

        val card = toCard(editCard).apply {
            this.updatedAt = updatedAt
        }

        deckDb.cardKtxDao().updateAll(card)
        appDb.deckKtxDao().refreshLastUpdatedAt(deckDbPath, updatedAt)
    }

    private suspend fun toCard(editCard: EditCardUi): Card {
        val card = deckDb.cardKtxDao().getCard(editCard.id)!!
        card.term = editCard.term.value
        card.definition = editCard.definition.value
        card.disabled = editCard.disabled.value
        Card.setHtmlFlags(card)
        return card
    }

    private fun cacheCard(card: EditCardUi) {
        _cardsInternal[card.id] = card
        _cards.value = _cardsInternal.toMap()
    }
}

class EditCardsManagerFactory(
    val deckDb: DeckDatabase,
    val appDb: AppDatabase,
    val deckDbPath: String,
    val application: Application
): ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(EditCardManager::class.java) -> {
                EditCardManager(deckDb, appDb, deckDbPath, application) as T
            }
            else -> throw IllegalArgumentException("ViewModel Not Found: ${modelClass.name}")
        }
    }
}