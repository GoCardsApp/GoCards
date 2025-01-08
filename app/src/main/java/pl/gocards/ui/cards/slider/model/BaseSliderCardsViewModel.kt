package pl.gocards.ui.cards.slider.model

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pl.gocards.db.room.DeckDatabase
import pl.gocards.ui.cards.slider.page.add.model.NewCardsModel
import pl.gocards.ui.cards.slider.page.edit.model.EditCardUi
import pl.gocards.ui.cards.slider.page.edit.model.EditCardsModel
import pl.gocards.ui.cards.slider.page.study.model.StudyCardsModel
import pl.gocards.ui.cards.slider.slider.model.Mode
import pl.gocards.ui.cards.slider.slider.model.SliderCardUi
import pl.gocards.ui.cards.slider.slider.model.SliderCardsModel

/**
 * @author Grzegorz Ziemski
 */
open class BaseSliderCardsViewModel(
    val defaultMode: Mode,
    val deckDb: DeckDatabase,
    val sliderCardsModel: SliderCardsModel,
    val studyCardsModel: StudyCardsModel,
    val newCardsModel: NewCardsModel,
    val editCardsModel: EditCardsModel,

    application: Application
): AndroidViewModel(application) {

    init {
        viewModelScope.launch(Dispatchers.IO) {
            sliderCardsModel.loadMaxForgottenCards()
        }
    }

    /* -----------------------------------------------------------------------------------------
     * Start
     * ----------------------------------------------------------------------------------------- */

    fun isLoaded(): Boolean {
        return sliderCardsModel.items.value.isNotEmpty()
    }

    fun loadForgottenCards() {
        viewModelScope.launch(Dispatchers.IO) {
            val sliderCards = sliderCardsModel.loadForgottenCards(defaultMode)
            if (sliderCards.isNotEmpty()) {
                val currentPage = sliderCardsModel.getSettledPage() ?: 0
                setInitialPage(currentPage, sliderCards)
            }
        }
    }

    fun loadAllCards() {
        viewModelScope.launch(Dispatchers.IO) {
            val sliderCards = sliderCardsModel.loadAllCards(defaultMode)
            if (sliderCards.isNotEmpty()) {
                val currentPage = sliderCardsModel.getSettledPage() ?: 0
                setInitialPage(currentPage, sliderCards)
            }
        }
    }

    fun loadAllCardsAndAddNewCard() {
        viewModelScope.launch(Dispatchers.IO) {
            val sliderCards = sliderCardsModel.loadAllCards(defaultMode)
            val id = newCardsModel.addNewCard()
            sliderCardsModel.addNewCard(sliderCards.size - 1, id, sliderCards)
            sliderCardsModel.loaded.value = true
        }
    }

    /**
     * C_R_07 Add a new card here
     */
    fun loadAllCardsAndAddNewCard(newCardAfterCardId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val cards = sliderCardsModel.loadAllCards(defaultMode)
            val page = cards.indexOfFirst { card -> card.id == newCardAfterCardId }
            addNewCard(page, cards)
        }
    }

    /**
     * C_C_24 Edit the card
     */
    fun loadAllCardsAndSetCardId(cardId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val sliderCards = sliderCardsModel.loadAllCards(defaultMode)
            if (sliderCards.isNotEmpty()) {
                val card = sliderCards.find { it.id == cardId }
                val page = sliderCards.indexOf(card)
                setInitialPage(page, sliderCards)
                sliderCardsModel.setChangePagerPage(page)
            }
        }
    }

    /* -----------------------------------------------------------------------------------------
     * Current Page
     * ----------------------------------------------------------------------------------------- */

    fun refreshSettledPage() {
        viewModelScope.launch(Dispatchers.IO) {
            val currentPage = sliderCardsModel.getSettledPage()
            if (currentPage != null) {
                setInitialPage(currentPage, getSliderCards())
            }
        }
    }

    open fun setSettledPage(setPage: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            setInitialPage(setPage, getSliderCards())
        }
    }

    private suspend fun setInitialPage(
        setPage: Int,
        sliderCards: List<SliderCardUi>
    ) {
        if (setPage >= sliderCards.size) return
        loadCurrentCard(setPage, sliderCards)
        sliderCardsModel.setSettledPage(setPage)
    }

    private suspend fun setSettledPage(
        setPage: Int,
        sliderCards: List<SliderCardUi>
    ) {
        if (setPage >= sliderCards.size) return

        val currentPage = this.sliderCardsModel.getSettledPage()
        if (currentPage == setPage) return

        if (currentPage != null) {
            saveCard(currentPage, sliderCards)
        }

        setInitialPage(setPage, sliderCards)
    }

    private suspend fun loadCurrentCard(setPage: Int, sliderCards: List<SliderCardUi>) {
        loadCard(setPage - 1, sliderCards)
        loadCard(setPage, sliderCards)
        loadCard(setPage + 1, sliderCards)
        loadCard(setPage + 2, sliderCards)
        loadCard(setPage + 3, sliderCards)
    }

    private suspend fun loadCard(page: Int, sliderCards: List<SliderCardUi>) {
        if (page < 0 || page >= sliderCards.size) return
        val sliderCard = sliderCards[page]

        if (sliderCard.mode.value == Mode.STUDY) {
            studyCardsModel.loadCard(
                sliderCard.id,
                getCardId(page - 1, sliderCards)
            )
        } else if (sliderCard.mode.value == Mode.EDIT) {
            studyCardsModel.loadCard(
                sliderCard.id,
                getCardId(page - 1, sliderCards)
            )
            editCardsModel.loadCard(sliderCard.id)
        }
    }

    private fun getCardId(page: Int?, sliderCards: List<SliderCardUi>): Int? {
        if (page == null) return null
        if (page < 0 || page >= sliderCards.size) return null

        val sliderCard = sliderCards[page]
        val mode = sliderCard.mode.value

        return if (mode != Mode.NEW) {
            sliderCards[page].id
        } else null
    }

    /* -----------------------------------------------------------------------------------------
     * C_C_23 Create a new card
     * ----------------------------------------------------------------------------------------- */

    /**
     * C_C_23 Create a new card
     */
    fun addNewCard(page: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentPage = this@BaseSliderCardsViewModel.sliderCardsModel.getSettledPage()
            val sliderCards = getSliderCards()

            if (currentPage != null) {
                saveCard(currentPage, sliderCards)
            }

            val id = newCardsModel.addNewCard()
            sliderCardsModel.addNewCard(page, id, sliderCards.toMutableList())
        }
    }

    /**
     * C_R_07 Add a new card here
     */
    private fun addNewCard(page: Int, cards: MutableList<SliderCardUi>) {
        viewModelScope.launch(Dispatchers.IO) {
            val id = newCardsModel.addNewCard()
            sliderCardsModel.addNewCard(page, id, cards)
        }
    }

    /**
     * C_C_23 Create a new card
     */
    fun saveNewCard(page: Int, card: EditCardUi) {
        viewModelScope.launch(Dispatchers.IO) {
            val ordinal = sliderCardsModel.findOrdinalNotNewBefore(page)
            val id = newCardsModel.saveCard(card, ordinal)

            val editCard = SliderCardUi(id, ordinal, null, mutableStateOf(defaultMode))
            sliderCardsModel.replaceById(card.id, editCard)

            if (defaultMode == Mode.STUDY) {
                studyCardsModel.reloadCard(id)
            } else if (defaultMode == Mode.EDIT) {
                editCardsModel.reloadCard(id)
            }

            setMode(page, defaultMode)
        }
    }

    private fun setMode(page: Int, mode: Mode) {
        val sliderCards = sliderCardsModel.items.value
        val sliderCard = sliderCards[page]
        sliderCard.mode.value = mode
    }

    /* -----------------------------------------------------------------------------------------
     * C_C_24 Edit the card
     * ----------------------------------------------------------------------------------------- */

    /**
     * C_C_24 Edit the card
     */
    fun editMode(page: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val cards = sliderCardsModel.items.value.toMutableList()
            val sliderCard = cards[page]
            editCardsModel.loadCard(sliderCard.id)
            sliderCard.mode.value = Mode.EDIT
            sliderCardsModel.items.value = cards
        }
    }

    /**
     * C_C_24 Edit the card
     */
    fun saveCard(page: Int, card: EditCardUi) {
        viewModelScope.launch(Dispatchers.IO) {
            editCardsModel.saveCard(card)
            studyCardsModel.reloadCard(card.id)
            setMode(page, defaultMode)
        }
    }

    /* -----------------------------------------------------------------------------------------
     * Closing a card
     * ----------------------------------------------------------------------------------------- */

    fun saveCard() {
        val page = sliderCardsModel.getSettledPage() ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val sliderCards = sliderCardsModel.items.value
            saveCard(page, sliderCards)
        }
    }

    private suspend fun saveCard(page: Int, sliderCards: List<SliderCardUi>) {
        if (page >= sliderCards.size) return
        val sliderCard = sliderCards[page]
        val mode = sliderCard.mode.value
        if (mode == Mode.STUDY) {
            studyCardsModel.saveCard(sliderCard.id)
        }
    }

    /* -----------------------------------------------------------------------------------------
     * Others
     * ----------------------------------------------------------------------------------------- */

    fun setWindowHeightPx(windowHeightPx: Int) {
        if (windowHeightPx <= 0) return

        viewModelScope.launch(Dispatchers.IO) {
            studyCardsModel.setWindowHeightPx(windowHeightPx)

            val currentPage = sliderCardsModel.getSettledPage() ?: 0

            if (sliderCardsModel.items.value.size > currentPage) {
                val sliderCard = sliderCardsModel.getItem(currentPage)!!
                studyCardsModel.setTermHeightPx(id = sliderCard.id, previousId = null)
            }

            if (sliderCardsModel.items.value.size > currentPage + 1) {
                val sliderCard = sliderCardsModel.getItem(currentPage + 1)!!
                studyCardsModel.setTermHeightPx(id = sliderCard.id, previousId = null)
            }
        }
    }

    /* -----------------------------------------------------------------------------------------
     * Helpers
     * ----------------------------------------------------------------------------------------- */

    private fun getSliderCards(): List<SliderCardUi> {
        return sliderCardsModel.items.value
    }
}