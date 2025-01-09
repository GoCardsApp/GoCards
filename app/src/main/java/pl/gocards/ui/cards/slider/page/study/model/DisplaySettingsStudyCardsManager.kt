package pl.gocards.ui.cards.slider.page.study.model

import androidx.compose.runtime.MutableState
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pl.gocards.db.room.AppDatabase
import pl.gocards.db.room.DeckDatabase
import pl.gocards.room.entity.deck.CardConfig
import pl.gocards.room.entity.deck.DeckConfig

/**
 * @author Grzegorz Ziemski
 */
abstract class DisplaySettingsStudyCardsManager(
    deckDb: DeckDatabase,
    appDb: AppDatabase,
    deckDbPath: String
) : CoreStudyCardsManager(deckDb, appDb, deckDbPath) {

    companion object {
        private const val DEFAULT_DISPLAY_RATIO = 0.5f
    }

    private var windowHeightPx: Int? = null

    /* -----------------------------------------------------------------------------------------
     * Setup the card
     * ----------------------------------------------------------------------------------------- */

    suspend fun setupDisplayedCard(id: Int, previousId: Int?) {
        fetchCards(id, previousId) { current, previous ->
            setupDisplayedCard(current, previous)
        }
    }

    suspend fun setupCardBeforeDisplay(id: Int, previousId: Int?) {
        fetchCards(id, previousId) { current, previous ->
            setupCardBeforeDisplay(current, previous)
        }
    }

    private suspend fun fetchCards(
        id: Int,
        previousId: Int?,
        processFn: suspend (StudyCardUi, StudyCardUi?) -> Unit
    ) {
        val current = getCachedOrFetchCard(id)
        val previous = previousId?.let { getCachedOrFetchCard(it) }
        processFn(current, previous)
    }

    private fun setupCardBeforeDisplay(card: StudyCardUi, previous: StudyCardUi?) {
        if (card.wasDisplayed) return

        copyDisplaySettingsIfNeeded(card, previous)
        computeAndSetTermHeight(card, previous)
    }

    private fun setupDisplayedCard(card: StudyCardUi, previous: StudyCardUi?) {
        if (card.wasDisplayed) return

        copyDisplaySettingsIfNeeded(card, previous)
        computeAndSetTermHeight(card, previous)

        if (card.termHeightPx.value != null) {
            card.wasDisplayed = true
        }
    }

    private fun copyDisplaySettingsIfNeeded(card: StudyCardUi, previousCard: StudyCardUi?) {
        applyFontSize(
            card.termFontSize,
            card.termFontSizeSaved,
            previousCard?.termFontSize,
            DeckConfig.STUDY_CARD_FONT_SIZE_DEFAULT.sp
        )
        applyFontSize(
            card.defFontSize,
            card.defFontSizeSaved,
            previousCard?.defFontSize,
            DeckConfig.STUDY_CARD_FONT_SIZE_DEFAULT.sp
        )
    }

    private fun applyFontSize(
        fontSize: MutableState<TextUnit?>,
        savedFontSize: TextUnit?,
        previousFontSize: MutableState<TextUnit?>?,
        defaultFontSize: TextUnit
    ) {
        fontSize.value = savedFontSize ?: previousFontSize?.value ?: defaultFontSize
    }

    /* -----------------------------------------------------------------------------------------
     * Set a term height
     * ----------------------------------------------------------------------------------------- */

    fun computeAndSetTermHeight(cardId: Int) {
        val card = getCached(cardId) ?: return
        computeAndSetTermHeight(card, null)
    }

    private fun computeAndSetTermHeight(card: StudyCardUi, previous: StudyCardUi?) {
        if (card.wasDisplayed) return

        val termHeightPx = computeTermHeight(card, previous)
        if (termHeightPx != null) {
            card.termHeightPx.value = termHeightPx
        }
    }

    private fun computeTermHeight(
        card: StudyCardUi,
        previousCard: StudyCardUi?
    ): Float? {
        val lastTermHeightPx = previousCard?.termHeightPx?.value
        return if (windowHeightPx == null && lastTermHeightPx == null) {
            return null
        } else {
            computeTermHeightPx(
                card.displayRatio,
                windowHeightPx,
                lastTermHeightPx
            )
        }
    }

    private fun computeTermHeightPx(
        displayRatio: Float,
        windowHeightPx: Int?,
        lastTermHeightPx: Float?
    ): Float {
        return when {
            displayRatio != 0f && windowHeightPx != null -> displayRatio * windowHeightPx
            lastTermHeightPx != null -> lastTermHeightPx
            windowHeightPx != null -> CardConfig.STUDY_CARD_TD_DISPLAY_RATIO_DEFAULT * windowHeightPx
            else -> 0f
        }
    }

    fun setWindowHeight(windowHeightPx: Int) {
        require(windowHeightPx > 0) { "Window height must be greater than 0" }
        this.windowHeightPx = windowHeightPx
    }

    /* -----------------------------------------------------------------------------------------
     * Save
     * ----------------------------------------------------------------------------------------- */

    override suspend fun onCardPause(cardId: Int) {
        super.onCardPause(cardId)
        saveDisplaySettings(cardId)
    }

    suspend fun saveDisplaySettings(id: Int) = withContext(Dispatchers.IO) {
        val card = cards.value[id]
        if (card != null) {
            saveDisplaySettings(card)
        }
    }

    private suspend fun saveDisplaySettings(card: StudyCardUi) {
        saveTermFontSize(card)
        saveDefinitionFontSize(card)
        saveDisplayRatio(card)
    }

    private suspend fun saveTermFontSize(card: StudyCardUi) {
        card.termFontSize.value?.value?.toInt()?.let {
            deckDb.cardConfigKtxDao().updateStudyCardTermFontSize(
                card.id,
                it
            )
        }
    }

    private suspend fun saveDefinitionFontSize(card: StudyCardUi) {
        card.defFontSize.value?.value?.toInt()?.let {
            deckDb.cardConfigKtxDao().updateStudyCardDefinitionFontSize(
                card.id,
                it
            )
        }
    }

    private suspend fun saveDisplayRatio(card: StudyCardUi) {
        val windowHeightPx = this.windowHeightPx ?: return
        val termHeightPx = card.termHeightPx.value ?: return
        val displayRatio = termHeightPx / windowHeightPx

        deckDb.cardConfigKtxDao().updateStudyCardTdDisplayRatio(card.id, displayRatio)
    }

    /* -----------------------------------------------------------------------------------------
     * Others
     * ----------------------------------------------------------------------------------------- */

    /**
     * C_U_38 Reset display settings
     */
    fun resetDisplaySettings(card: StudyCardUi) {
        card.termFontSize.value = DeckConfig.STUDY_CARD_FONT_SIZE_DEFAULT.sp
        card.defFontSize.value = DeckConfig.STUDY_CARD_FONT_SIZE_DEFAULT.sp
        card.termHeightPx.value = computeTermHeightPx(DEFAULT_DISPLAY_RATIO, windowHeightPx, null)
    }
}