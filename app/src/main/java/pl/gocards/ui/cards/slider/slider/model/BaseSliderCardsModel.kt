package pl.gocards.ui.cards.slider.slider.model

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pl.gocards.db.room.AppDatabase
import pl.gocards.db.room.DeckDatabase
import pl.gocards.room.entity.deck.CardSlider
import pl.gocards.ui.common.pager.dynamic.DynamicPagerModel
import kotlin.jvm.optionals.getOrNull

/**
 * @author Grzegorz Ziemski
 */
open class BaseSliderCardsModel(
    val deckDb: DeckDatabase,
    val appDb: AppDatabase,
    application: Application,
    onScroll: (Int?, Int) -> Unit
): DynamicPagerModel<SliderCardUi>(application, onScroll) {

    val loaded = mutableStateOf(false)

    /* -----------------------------------------------------------------------------------------
     * Loads cards
     * ----------------------------------------------------------------------------------------- */

    suspend fun loadForgottenCards(mode: Mode): MutableList<SliderCardUi> {
        val newCards = deckDb.cardSliderKtxDao()
            .getNextCardsToReplay()
            .map { mapCard(it, mode) }
            .toMutableList()

        items.value = newCards
        loaded.value = true
        return newCards
    }

    suspend fun loadAllCards(mode: Mode): MutableList<SliderCardUi> {
        val newCards = deckDb.cardSliderKtxDao()
            .getAllCards()
            .map { mapCard(it, mode) }
            .toMutableList()

        items.value = newCards
        loaded.value = true
        return newCards
    }

    private fun mapCard(card: CardSlider, mode: Mode): SliderCardUi {
        return SliderCardUi(card.id!!, card.ordinal, null, mutableStateOf(mode))
    }

    /* -----------------------------------------------------------------------------------------
     * C_C_23 Create a new card
     * ----------------------------------------------------------------------------------------- */

    /**
     * C_C_23 Create a new card
     */
    fun addNewCard(page: Int, id: Int, items: MutableList<SliderCardUi>) {
        addNewPage(page, SliderCardUi(id, null, null, mutableStateOf(Mode.NEW)), items)
    }

    suspend fun findOrdinalNotNewBefore(page: Int): Int {
        val previousCard = findFirstNotNewBefore(page)
        val isFirstCard = previousCard == null
        val ordinal = if (isFirstCard) 1 else {
            val ordinal = deckDb.cardKtxDao().getOrdinal(previousCard?.id!!)
            ordinal + 1
        }
        return ordinal
    }

    private fun findFirstNotNewBefore(page: Int): SliderCardUi? {
        val list = ArrayList(items.value.subList(0, page))
        list.reverse()
        return list
            .stream()
            .filter { it.mode.value != Mode.NEW }
            .findFirst()
            .orElse(null)
    }

    /* -----------------------------------------------------------------------------------------
     * C_D_25 Delete the card
     * ----------------------------------------------------------------------------------------- */

    private var waitingToDeleteCard: SliderCardUi? = null

    /**
     * C_D_25 Delete the card
     */
    fun deleteCardAndSlideToNextPage(page: Int, sliderCard: SliderCardUi) {
        val cards = this.items.value
        if (cards.size == 1) {
            deleteCardDb(page, sliderCard)
        }

        super.deleteAndSlideToNextPage(page, sliderCard)

        waitingToDeleteCard = sliderCard
    }

    /**
     * C_D_25 Delete the card
     */
    override fun deleteWaitingItem() {
        val waitingToDeleteCard = this.waitingToDeleteCard

        if (waitingToDeleteCard != null) {
            this.waitingToDeleteCard = null
            val sliderCards = items.value
            val deletePage = sliderCards.indexOf(waitingToDeleteCard)
            deleteCardDb(deletePage, waitingToDeleteCard)
        }
        super.deleteWaitingItem()
    }

    /**
     * C_D_25 Delete the card
     */
    private fun deleteCardDb(page: Int, sliderCard: SliderCardUi) {
        val cards = items.value

        if (cards[page].id != sliderCard.id) {
            throw IllegalArgumentException()
        }

        val mode = sliderCard.mode.value
        if (mode != Mode.NEW) {
            viewModelScope.launch(Dispatchers.IO) {
                val card = deckDb.cardKtxDao().getCard(sliderCard.id)!!
                deckDb.cardKtxDao().delete(card)
            }
        }
    }

    /* -----------------------------------------------------------------------------------------
     * C_U_26 Undo card deletion
     * ----------------------------------------------------------------------------------------- */

    /**
     * C_U_26 Undo card deletion
     */
    suspend fun restoreDeletedCard(deletedPage: Int, sliderCard: SliderCardUi) {
        val card = deckDb.cardKtxDao().getCard(sliderCard.id)!!
        deckDb.cardKtxDao().restore(card)
        restorePage(deletedPage, sliderCard)
    }

    /* -----------------------------------------------------------------------------------------
     * Helpers
     * ----------------------------------------------------------------------------------------- */

    fun isNotEmpty(): Boolean {
        return items.value.isNotEmpty()
    }

    fun size(): Int {
        return items.value.size
    }

    fun findById(id: Int): SliderCardUi? {
        return this.items.value
            .stream()
            .filter { it.id == id }
            .findAny()
            .getOrNull()
    }

    fun replaceById(id: Int, sliderCard: SliderCardUi) {
        synchronized (items) {
            val sliderCards = items.value.toMutableList()
            sliderCards.replaceAll {
                if (it.id == id) {
                    sliderCard
                } else {
                    it
                }
            }
            items.value = sliderCards
        }
    }

    fun setMode(page: Int, mode: Mode) {
        val sliderCards = items.value
        val sliderCard = sliderCards[page]
        sliderCard.mode.value = mode
    }
}