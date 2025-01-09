package pl.gocards.ui.cards.slider.model

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import pl.gocards.db.room.DeckDatabase
import pl.gocards.ui.cards.slider.page.add.model.NewCardManager
import pl.gocards.ui.cards.slider.page.card.model.CardMode
import pl.gocards.ui.cards.slider.page.card.model.SliderCardManager
import pl.gocards.ui.cards.slider.page.card.model.SliderCardUi
import pl.gocards.ui.cards.slider.page.edit.model.EditCardManager
import pl.gocards.ui.cards.slider.page.edit.model.EditCardUi
import pl.gocards.ui.cards.slider.page.study.model.StudyCardManager
import pl.gocards.util.FirebaseAnalyticsHelper

/**
 * @author Grzegorz Ziemski
 */
open class CoreCardSliderViewModel(
    val defaultMode: CardMode,
    val deckDb: DeckDatabase,
    val sliderCardManager: SliderCardManager,
    val studyCardManager: StudyCardManager?,
    val newCardManager: NewCardManager,
    val editCardManager: EditCardManager,
    var analytics: FirebaseAnalyticsHelper,
    application: Application
) : AndroidViewModel(application) {

    companion object {
        private const val ADJACENT_CARD_OFFSET = 3
    }

    /* -----------------------------------------------------------------------------------------
     * Start
     * ----------------------------------------------------------------------------------------- */

    fun fetchForgottenCards() = fetchCards(sliderCardManager::fetchForgottenCards)
    fun fetchAllCards() = fetchCards(sliderCardManager::fetchAllCards)

    private fun fetchCards(loadCardsFn: suspend (CardMode) -> List<SliderCardUi>) {
        viewModelScope.launch(Dispatchers.IO) {
            val sliderCards = loadCardsFn(defaultMode)
            if (sliderCards.isNotEmpty()) {
                updateSettledPage(0)
            }
        }
    }

    fun fetchAllCardsAndAppendNew() {
        viewModelScope.launch(Dispatchers.IO) {
            val sliderCards = sliderCardManager.fetchAllCards(defaultMode)
            val page = (sliderCards.size - 1).coerceAtLeast(0)

            val id = newCardManager.addNewCard()
            val ordinal = sliderCards[sliderCards.size - 1].ordinal ?: 1
            sliderCardManager.insertNewCardAfter(page, id, ordinal)
        }
    }

    /**
     * C_R_07 Add a new card here
     */
    fun fetchAllCardsAndInsertAfter(cardId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val sliderCards = sliderCardManager.fetchAllCards(defaultMode)
            val page = sliderCards.indexOfFirst { card -> card.id == cardId }

            val id = newCardManager.addNewCard()
            val ordinal = sliderCards[sliderCards.size - 1].ordinal ?: 1
            sliderCardManager.insertNewCardAfter(page, id, ordinal)
        }
    }

    /**
     * C_C_24 Edit the card
     */
    fun fetchAllCardsAndFocusOnCard(cardId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val sliderCards = sliderCardManager.fetchAllCards(defaultMode)
            sliderCards
                .find { it.id == cardId }
                ?.let { targetCard ->
                    val targetPage = sliderCards.indexOf(targetCard)
                    sliderCardManager.setFocusPage(targetPage)
                }
        }
    }

    /* -----------------------------------------------------------------------------------------
     * Current Page
     * ----------------------------------------------------------------------------------------- */

    fun resetSettledPage() {
        updateSettledPage(sliderCardManager.getSettledPage() ?: return)
    }

    private val mutex = Mutex()

    fun updateSettledPage(nextPage: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            mutex.withLock {
                val sliderCards = sliderCardManager.getItems()
                if (nextPage >= sliderCards.size) return@launch

                val settledPage = sliderCardManager.getSettledPage()
                if (settledPage == nextPage) return@launch

                val settledCard = settledPage?.let { sliderCards.getOrNull(it) }
                settledCard?.let { onCardPause(it) }

                setupCurrentAndAdjacentCards(nextPage, sliderCards)
                sliderCardManager.setSettledPage(nextPage)

                analytics.sliderScroll(
                    settledPage,
                    settledCard?.cardMode?.value?.toString(),
                    nextPage,
                    sliderCards[nextPage].cardMode.value.toString(),
                )
            }
        }
    }

    private suspend fun setupCurrentAndAdjacentCards(
        setPage: Int,
        sliderCards: List<SliderCardUi>
    ) {
        setupDisplayedCard(setPage, sliderCards)
        setupCardBeforeDisplay(setPage - 1, sliderCards)

        (setPage + 1..setPage + ADJACENT_CARD_OFFSET)
            .filter { it in sliderCards.indices }
            .forEach { pageIndex ->
                if (pageIndex in sliderCards.indices) {
                    setupCardBeforeDisplay(pageIndex, sliderCards)
                }
            }
    }

    private suspend fun setupCardBeforeDisplay(page: Int, sliderCards: List<SliderCardUi>) {
        if (page !in sliderCards.indices) return
        val sliderCard = sliderCards[page]
        val cardMode = sliderCard.cardMode.value

        when (cardMode) {
            CardMode.NEW -> {}
            CardMode.EDIT -> editCardManager.fetchCardIfNeeded(sliderCard.id)
            else -> {
                studyCardManager?.setupCardBeforeDisplay(
                    id = sliderCard.id,
                    previousId = getPreviousId(sliderCards, page)
                )
            }
        }
    }

    private suspend fun setupDisplayedCard(page: Int, sliderCards: List<SliderCardUi>) {
        if (page !in sliderCards.indices) return
        val sliderCard = sliderCards[page]

        when (sliderCard.cardMode.value) {
            CardMode.NEW -> {}
            CardMode.EDIT -> editCardManager.fetchCardIfNeeded(sliderCard.id)
            else -> {
                studyCardManager?.setupDisplayedCard(
                    id = sliderCard.id,
                    previousId = getPreviousId(sliderCards, page)
                )
            }
        }
    }

    private fun getPreviousId(sliderCards: List<SliderCardUi>, page: Int): Int? {
        val previous = sliderCards.getOrNull(page - 1)
        // If it's a new card, it means it doesn't exist in the database yet and this will cause NPE.
        return if (previous?.cardMode?.value != CardMode.NEW) {
            previous?.id
        } else null
    }

    /* -----------------------------------------------------------------------------------------
     * C_C_23 Create a new card
     * ----------------------------------------------------------------------------------------- */

    /**
     * C_C_23 Create a new card
     */
    fun addNewCardAfter(page: Int) {
        viewModelScope.launch {
            val sliderCard = sliderCardManager.getItemOrNull(page)
            sliderCard?.let {
                onCardPause(it)
            }

            val id = newCardManager.addNewCard()
            val ordinal = sliderCard?.ordinal ?: 1
            sliderCardManager.insertNewCardAfter(page, id, ordinal)
        }
    }

    /**
     * C_C_23 Create a new card
     */
    fun persistNewCard(page: Int, card: EditCardUi) {
        viewModelScope.launch(Dispatchers.IO) {
            val ordinal = sliderCardManager.findNextOrdinalAfterSavedCardBeforePage(page)
            val id = newCardManager.saveCard(card, ordinal)

            val sliderCard = SliderCardUi(id, ordinal, null, mutableStateOf(defaultMode))
            val sliderCards = sliderCardManager.replaceCardById(card.id, sliderCard)
            setupDisplayedCard(page, sliderCards)
        }
    }

    /* -----------------------------------------------------------------------------------------
     * C_C_24 Edit the card
     * ----------------------------------------------------------------------------------------- */

    /**
     * C_C_24 Edit the card
     */
    fun switchToEditMode(page: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val sliderCard = sliderCardManager.getItem(page)
            onCardPause(sliderCard)

            editCardManager.fetchCardIfNeeded(sliderCard.id)
            sliderCard.cardMode.value = CardMode.EDIT
            sliderCardManager.setItems(sliderCardManager.getItems())
        }
    }

    /**
     * C_C_24 Edit the card
     */
    fun persistCard(card: EditCardUi) {
        viewModelScope.launch(Dispatchers.IO) {
            editCardManager.persistCard(card)
            if (defaultMode == CardMode.STUDY) {
                studyCardManager?.fetchCard(card.id)
            }
            sliderCardManager.updateModeById(card.id, defaultMode)

            val currentPage = sliderCardManager.findPageById(card.id)
            setupCardBeforeDisplay(currentPage, getSliderCards())
        }
    }

    /* -----------------------------------------------------------------------------------------
     * Save a current Card Display Settings
     * ----------------------------------------------------------------------------------------- */

    fun onCardPause() {
        val currentPage = sliderCardManager.getSettledPage() ?: return
        val sliderCards = getSliderCards()

        viewModelScope.launch(Dispatchers.IO) {
            onCardPause(sliderCards[currentPage])
        }
    }

    private suspend fun onCardPause(sliderCard: SliderCardUi) {
        val mode = sliderCard.cardMode.value
        if (mode == CardMode.STUDY) {
            studyCardManager?.onCardPause(sliderCard.id)
        }
    }

    /* -----------------------------------------------------------------------------------------
     * Others
     * ----------------------------------------------------------------------------------------- */

    fun updateWindowHeight(windowHeightPx: Int) {
        if (windowHeightPx <= 0) return

        viewModelScope.launch {
            mutex.withLock { studyCardManager?.setWindowHeight(windowHeightPx) }
            val currentPage = sliderCardManager.getSettledPage()

            if (currentPage != null) {
                listOf(currentPage, currentPage + 1)
                    .filter { it in 0 until sliderCardManager.cardCount() }
                    .map { page -> sliderCardManager.getItem(page) }
                    .forEach { card -> studyCardManager?.computeAndSetTermHeight(card.id) }
            }
        }
    }

    /* -----------------------------------------------------------------------------------------
     * Helpers
     * ----------------------------------------------------------------------------------------- */

    fun hasCards(): Boolean {
        return sliderCardManager.hasCards()
    }

    private fun getSliderCards(): List<SliderCardUi> {
        return sliderCardManager.getItems()
    }
}