package pl.gocards.ui.cards.slider.page.study.model

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.unit.sp
import pl.gocards.db.room.AppDatabase
import pl.gocards.db.room.DeckDatabase
import pl.gocards.room.entity.deck.Card

/**
 * @author Grzegorz Ziemski
 */
class StudyCardManager(
    deckDb: DeckDatabase,
    appDb: AppDatabase,
    deckDbPath: String,
) : LearningProgressStudyCardsManager(deckDb, appDb, deckDbPath) {

    override suspend fun mapCard(card: Card): StudyCardUi {
        val cardId = card.id!!
        val termFontSize = deckDb.cardConfigKtxDao().getStudyCardTermFontSize(cardId)
        val definitionFontSize = deckDb.cardConfigKtxDao().getStudyCardDefinitionFontSize(cardId)
        val displayRatio = deckDb.cardConfigKtxDao().getStudyCardTdDisplayRatio(cardId)

        return StudyCardUi(
            id = cardId,
            term = card.term,
            definition = card.definition,
            isTermSimpleHtml = card.isTermSimpleHtml,
            isTermFullHtml = card.isTermFullHtml,
            isDefinitionSimpleHtml = card.isDefinitionSimpleHtml,
            isDefinitionFullHtml = card.isDefinitionFullHtml,
            termFontSize = mutableStateOf(termFontSize?.sp),
            termFontSizeSaved = termFontSize?.sp,
            defFontSize = mutableStateOf(definitionFontSize?.sp),
            defFontSizeSaved = definitionFontSize?.sp,
            displayRatio = displayRatio ?: 0f,
            current = getCurrent(cardId),
            nextAfterAgain = getNextAfterAgain(cardId),
            nextAfterQuick = getNextAfterQuick(cardId),
            nextAfterEasy = getNextAfterEasy(cardId),
            nextAfterHard = getNextAfterHard(cardId),
        )
    }

    override suspend fun mapCard(card: Card, old: StudyCardUi): StudyCardUi {
        val cardId = card.id!!
        val termFontSize = deckDb.cardConfigKtxDao().getStudyCardTermFontSize(cardId)
        val definitionFontSize = deckDb.cardConfigKtxDao().getStudyCardDefinitionFontSize(cardId)
        val displayRatio = deckDb.cardConfigKtxDao().getStudyCardTdDisplayRatio(cardId)

        return StudyCardUi(
            id = cardId,
            term = card.term,
            definition = card.definition,
            isTermSimpleHtml = card.isTermSimpleHtml,
            isTermFullHtml = card.isTermFullHtml,
            isDefinitionSimpleHtml = card.isDefinitionSimpleHtml,
            isDefinitionFullHtml = card.isDefinitionFullHtml,
            termFontSize = mutableStateOf(termFontSize?.sp),
            termFontSizeSaved = termFontSize?.sp,
            defFontSize = mutableStateOf(definitionFontSize?.sp),
            defFontSizeSaved = definitionFontSize?.sp,
            displayRatio = displayRatio ?: 0f,
            current = getCurrent(cardId),
            nextAfterAgain = getNextAfterAgain(cardId),
            nextAfterQuick = getNextAfterQuick(cardId),
            nextAfterEasy = getNextAfterEasy(cardId),
            nextAfterHard = getNextAfterHard(cardId),

            termHeightPx = mutableStateOf(old.termHeightPx.value),
            showDefinition = mutableStateOf(old.showDefinition.value),
        )
    }
}