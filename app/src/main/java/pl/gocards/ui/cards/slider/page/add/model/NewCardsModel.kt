package pl.gocards.ui.cards.slider.page.add.model

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
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
class NewCardsModel internal constructor(
    val deckDb: DeckDatabase,
    val appDb: AppDatabase,
    val deckDbPath: String,
    application: Application
): AndroidViewModel(application) {

    private val cards = mutableStateOf(mapOf<Int, EditCardUi>())

    private var lastCardId: Int = 0

    suspend fun addNewCard(): Int {
        val lastCardId = getLastCardId()
        val cards = cards.value.toMutableMap()
        cards[lastCardId] = EditCardUi(id = lastCardId)
        this.cards.value = cards
        return lastCardId
    }

    suspend fun saveCard(card: EditCardUi, ordinal: Int): Int {
        val updatedAt = TimeUtil.getNowEpochSec()
        val cardId = deckDb.cardKtxDao().insertAfter(
            mapCard(card),
            ordinal,
            updatedAt
        ).toInt()
        appDb.deckKtxDao().refreshLastUpdatedAt(deckDbPath, updatedAt)
        removeCard(cardId)
        return cardId
    }

    private fun mapCard(card: EditCardUi): Card {
        val newCard = Card(
            term = card.term.value,
            definition = card.definition.value,
            disabled = card.disabled.value
        )
        setHtmlFlags(newCard)
        return newCard
    }

    private fun removeCard(id: Int) {
        val cards = cards.value.toMutableMap()
        cards.remove(id)
        this.cards.value = cards
    }

    private suspend fun getLastCardId(): Int {
        return if (this.lastCardId == 0) {
            val idsPoolToSave = 1000
            val lastCardId = deckDb.cardKtxDao().lastId() + idsPoolToSave
            this@NewCardsModel.lastCardId = lastCardId
            return lastCardId
        } else {
            lastCardId += 1
            lastCardId
        }
    }

    fun getCardsState(): State<Map<Int, EditCardUi>> {
        return cards
    }
}

class NewCardsModelFactory(
    val deckDb: DeckDatabase,
    val appDb: AppDatabase,
    val deckDbPath: String,
    val application: Application
): ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(NewCardsModel::class.java)) {
            NewCardsModel(deckDb, appDb, deckDbPath, application) as T
        } else {
            throw IllegalArgumentException("ViewModel Not Found")
        }
    }
}