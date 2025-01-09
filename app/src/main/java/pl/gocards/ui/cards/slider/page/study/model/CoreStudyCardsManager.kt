package pl.gocards.ui.cards.slider.page.study.model

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import pl.gocards.db.room.AppDatabase
import pl.gocards.db.room.DeckDatabase
import pl.gocards.room.entity.deck.Card

/**
 * @author Grzegorz Ziemski
 */
abstract class CoreStudyCardsManager(
    val deckDb: DeckDatabase,
    val appDb: AppDatabase,
    val deckDbPath: String
) {

    private val _cards = mutableStateOf(emptyMap<Int, StudyCardUi>())
    val cards: State<Map<Int, StudyCardUi>> get() = _cards

    protected suspend fun getCachedOrFetchCard(id: Int): StudyCardUi {
        return getCached(id) ?: fetchCard(id)
    }

    fun getCached(id: Int): StudyCardUi? {
        return _cards.value[id]
    }

    suspend fun fetchCard(id: Int): StudyCardUi {
        val cardDb = deckDb.cardKtxDao().getCard(id)!!
        val card = mapCard(cardDb)
        cacheCard(card)
        return card
    }

    suspend fun refreshCard(id: Int): StudyCardUi {
        val cardDb = deckDb.cardKtxDao().getCard(id)!!
        val card = mapCard(cardDb, getCached(id)!!)
        cacheCard(card)
        return card
    }

    abstract suspend fun mapCard(card: Card): StudyCardUi

    abstract suspend fun mapCard(card: Card, old: StudyCardUi): StudyCardUi

    private fun cacheCard(card: StudyCardUi) {
        _cards.value = _cards.value
            .toMutableMap()
            .apply { this[card.id] = card }
            .toMap()
    }

    open suspend fun onCardPause(cardId: Int) {
        hideDefinition(cardId)
    }

    protected fun hideDefinition(cardId: Int) {
        val studyCards = cards.value
        val studyCard = studyCards[cardId] ?: return
        studyCard.showDefinition.value = false
    }

    fun showDefinition(cardId: Int) {
        val studyCards = cards.value
        val studyCard = studyCards[cardId] ?: return
        studyCard.showDefinition.value = true
    }
}