/**
 * @author Grzegorz Ziemski
 */

package pl.gocards.ui.cards.slider.page.card.model

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pl.gocards.db.room.AppDatabase
import pl.gocards.db.room.DeckDatabase
import pl.gocards.dynamic_pager.DynamicPagerViewModel
import pl.gocards.room.dao.deck.CardSliderKtxDao
import pl.gocards.room.entity.deck.CardSlider

/**
 * @author Grzegorz Ziemski
 */
open class CoreSliderCardsManager(
    val deckDb: DeckDatabase,
    val appDb: AppDatabase,
    application: Application
) : DynamicPagerViewModel<SliderCardUi>(application) {

    val loaded = mutableStateOf(false)

    /* -----------------------------------------------------------------------------------------
     * Loads cards
     * ----------------------------------------------------------------------------------------- */

    suspend fun fetchForgottenCards(cardMode: CardMode) = fetchAndMapCards(cardMode) {
        getNextCardsToReplay()
    }

    suspend fun fetchAllCards(cardMode: CardMode) = fetchAndMapCards(cardMode) {
        getAllCards()
    }

    private suspend fun fetchAndMapCards(
        cardMode: CardMode,
        loadAction: suspend CardSliderKtxDao.() -> List<CardSlider>
    ): MutableList<SliderCardUi> = withContext(Dispatchers.IO) {
        val sliderCards = deckDb.cardSliderKtxDao()
            .loadAction()
            .map { it.toSliderCardUi(cardMode) }
            .toMutableList()

        setItems(sliderCards)
        loaded.value = true
        return@withContext sliderCards
    }

    private fun CardSlider.toSliderCardUi(cardMode: CardMode) = SliderCardUi(
        this.id!!,
        this.ordinal,
        null,
        mutableStateOf(cardMode)
    )

    /* -----------------------------------------------------------------------------------------
     * C_C_23 Create a new card
     * ----------------------------------------------------------------------------------------- */

    /**
     * C_C_23 Create a new card
     */
    fun insertNewCardAfter(page: Int, id: Int, ordinal: Int) {
        insertPageAfter(
            page,
            SliderCardUi(
                id,
                ordinal,
                null,
                mutableStateOf(CardMode.NEW)
            )
        )
    }

    suspend fun findNextOrdinalAfterSavedCardBeforePage(page: Int): Int {
        val previousCard = findFirstSavedCardBeforePage(page)
        return if (previousCard == null) {
            1
        } else {
            val ordinal = deckDb.cardKtxDao().getOrdinal(previousCard.id)
            ordinal + 1
        }
    }

    private fun findFirstSavedCardBeforePage(page: Int): SliderCardUi? {
        return getItems()
            .subList(0, page)
            .asReversed()
            .firstOrNull { it.cardMode.value != CardMode.NEW }
    }

    /* -----------------------------------------------------------------------------------------
     * C_D_25 Delete the card
     * ----------------------------------------------------------------------------------------- */

    private var pendingDeletionCard: SliderCardUi? = null

    /**
     * C_D_25 Delete the card
     */
    fun deleteCardAndScrollNext(page: Int, sliderCard: SliderCardUi) {
        if (isMutating()) return

        if (cardCount() == 1) {
            viewModelScope.launch {
                deleteCardInDb(sliderCard)
                super.deleteAndSlideToNext(page, sliderCard)
            }
        } else {
            super.deleteAndSlideToNext(page, sliderCard)
            markCardForDeletion(sliderCard)
        }
    }

    /**
     * C_D_25 Delete the card
     */
    override suspend fun processDeletion(item: SliderCardUi) {
        val card = this.pendingDeletionCard

        if (card != null) {
            this.pendingDeletionCard = null
            deleteCardInDb(card)
        }

        super.processDeletion(item)
    }

    /**
     * C_D_25 Delete the card
     */
    private suspend fun deleteCardInDb(sliderCard: SliderCardUi) {
        val mode = sliderCard.cardMode.value
        if (mode != CardMode.NEW) {
            val card = deckDb.cardKtxDao().getCard(sliderCard.id)!!
            deckDb.cardKtxDao().delete(card)
        }
    }

    /**
     * C_D_25 Delete the card
     */
    private fun markCardForDeletion(sliderCard: SliderCardUi) {
        pendingDeletionCard = sliderCard
    }

    /* -----------------------------------------------------------------------------------------
     * C_U_26 Undo card deletion
     * ----------------------------------------------------------------------------------------- */

    /**
     * C_U_26 Undo card deletion
     */
    suspend fun restoreDeletedCard(targetPage: Int, cardToRestore: SliderCardUi) {
        restoreCardInDb(cardToRestore.id)
        restoreCardAndScroll(targetPage, cardToRestore)
    }

    fun findByOrdinalGreaterThanEqual(sliderCard: SliderCardUi): Int {
        return findByOrdinalGreaterThanEqual(sliderCard.ordinal!!)
            .takeIf { it != -1 }
            ?: cardCount()
    }

    private fun findByOrdinalGreaterThanEqual(ordinal: Int): Int {
        return getItems().indexOfFirst { it.ordinal!! >= ordinal }
    }

    private suspend fun restoreCardInDb(cardId: Int) {
        val card = deckDb.cardKtxDao().getCard(cardId)!!
        deckDb.cardKtxDao().restore(card)
    }

    fun restoreCardAndScroll(targetPage: Int, cardToRestore: SliderCardUi) {
        if (targetPage == cardCount()) {
            appendPageAndScroll(cardToRestore)
        } else {
            insertPageBeforeAndScroll(targetPage, cardToRestore)
        }
    }

    /* -----------------------------------------------------------------------------------------
     * Helpers
     * ----------------------------------------------------------------------------------------- */

    fun updateModeById(id: Int, cardMode: CardMode) {
        val sliderCard = findById(id)!!
        sliderCard.cardMode.value = cardMode
    }

    fun findById(id: Int): SliderCardUi? = getItems().firstOrNull { it.id == id }

    fun findPageById(id: Int): Int = getItems().indexOfFirst { it.id == id }

    suspend fun replaceCardById(id: Int, sliderCard: SliderCardUi): List<SliderCardUi> =
        updateItems { items ->
            items.replaceAll {
                if (it.id == id) {
                    sliderCard
                } else {
                    it
                }
            }
            items
        }

    fun hasCards() = getItems().isNotEmpty()

    fun cardCount(): Int = getItems().size
}