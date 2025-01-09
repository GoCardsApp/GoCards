package pl.gocards.ui.cards.slider.view

import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.CoroutineScope
import pl.gocards.App
import pl.gocards.R
import pl.gocards.dynamic_pager.DynamicPagerUIMediator
import pl.gocards.ui.cards.slider.model.CardSliderViewModel
import pl.gocards.ui.cards.slider.page.card.model.SliderCardUi
import pl.gocards.ui.cards.slider.page.edit.model.EditCardUi
import pl.gocards.ui.cards.slider.page.study.model.StudyCardUi
import pl.gocards.ui.cards.slider.page.study.ui.definition.DefinitionButtonsActions
import pl.gocards.ui.common.showSnackbar
import pl.gocards.ui.filesync_pro.AutoSyncViewModel
import pl.gocards.util.FirebaseAnalyticsHelper

/**
 * @author Grzegorz Ziemski
 */
data class CardSliderUIMediator(
    val onBack: () -> Unit = {},
    val isDarkTheme: Boolean,
    val preview: Boolean,
    val snackbarHostState: SnackbarHostState = SnackbarHostState(),

    val deckName: String,
    val sliderCards: List<SliderCardUi>,
    val loaded: Boolean = false,

    val dynamicPager: DynamicPagerUIMediator<SliderCardUi>,

    val studyPage: StudyPage?,
    val setWindowHeightPx: (Int) -> Unit = {},
    val noMoreCardsToRepeat: () -> Unit = {},

    val editPage: EditPage,
    val newPage: NewPage
)

/**
 * @author Grzegorz Ziemski
 */
class CardSliderUIMediatorFactory {

    private lateinit var snackbarHostState: SnackbarHostState
    private lateinit var scope: CoroutineScope

    private lateinit var cardDeletedMessage: String
    private lateinit var restoreLabel: String
    private lateinit var analytics: FirebaseAnalyticsHelper

    private lateinit var application: App
    private lateinit var viewModel: CardSliderViewModel

    @Composable
    fun getInstance(
        onBack: () -> Unit = {},
        deckName: String,
        viewModel: CardSliderViewModel,
        autoSyncCardsModel: AutoSyncViewModel?,
        showRateButtons: Boolean,
        noMoreCardsToRepeat: () -> Unit,
        analytics: FirebaseAnalyticsHelper,
        application: App
    ): CardSliderUIMediator {
        this.viewModel = viewModel
        this.analytics = analytics
        this.application = application

        snackbarHostState = remember { SnackbarHostState() }
        scope = rememberCoroutineScope()

        cardDeletedMessage = stringResource(R.string.cards_list_toast_deleted_card)
        restoreLabel = stringResource(R.string.restore)

        return CardSliderUIMediator(
            onBack = onBack,
            isDarkTheme = application.getDarkMode() ?: isSystemInDarkTheme(),
            preview = false,
            deckName = deckName,
            snackbarHostState = snackbarHostState,
            sliderCards = viewModel.sliderCardManager.items.value,
            loaded = viewModel.sliderCardManager.loaded.value,
            dynamicPager = DynamicPagerUIMediator.create(viewModel.sliderCardManager)
                .copy(
                    setSettledPage = { viewModel.updateSettledPage(it) },
                ),
            noMoreCardsToRepeat = noMoreCardsToRepeat,
            studyPage = if (viewModel.studyCardManager != null) StudyPage(
                studyCards = viewModel.studyCardManager.cards,
                editingLocked = autoSyncCardsModel?.getEditingLocked() ?: remember {
                    mutableStateOf(
                        false
                    )
                },
                buttons = DefinitionButtonsActions(
                    showRateButtons = showRateButtons,
                    onClickAgain = { page, sliderCard ->
                        if (viewModel.sliderCardManager.hasExceededForgottenCardLimit()) {
                            showShortToastLimitForgottenCardsExceeded()
                        }
                        viewModel.onAgainClick(page, sliderCard)
                        analytics.again(page)
                    },
                    onClickQuick = { page, sliderCard ->
                        viewModel.onQuickClick(page, sliderCard)
                        analytics.quick(page)
                    },
                    onClickEasy = { page, sliderCard ->
                        viewModel.onEasyClick(page, sliderCard)
                        analytics.easy(page)
                    },
                    onClickHard = { page, sliderCard ->
                        viewModel.onHardClick(page, sliderCard)
                        analytics.hard(page)
                    },
                ),
                onClickMenuEditCard = { page ->
                    viewModel.switchToEditMode(page)
                    analytics.sliderEditCard(page)
                },
                onClickMenuNewCard = {
                    viewModel.addNewCardAfter(it)
                    analytics.sliderNewCard(it)
                },
                onClickMenuDeleteCard = { page, card ->
                    viewModel.deleteCard(page, card)
                    showSnackbarDeletedCard()
                    analytics.sliderDeleteCard(page)
                },
                onClickMenuResetView = { card ->
                    viewModel.studyCardManager.resetDisplaySettings(card)
                },
            ) else null,
            editPage = EditPage(
                editCards = viewModel.editCardManager.cards,
                onClickMenuNewCard = {
                    viewModel.addNewCardAfter(it)
                    analytics.sliderNewCard(it)
                },
                onClickMenuDeleteCard = { page, card ->
                    viewModel.deleteCard(page, card)
                    showSnackbarDeletedCard()
                    analytics.sliderDeleteCard(page)
                },
                onClickMenuSaveEditCard = { page, card ->
                    viewModel.persistCard(card)
                    showShortToastMessage(R.string.card_edit_card_updated_toast)
                    analytics.updateCard(page)
                }),
            newPage = NewPage(
                newCards = viewModel.newCardManager.cards,
                onClickMenuNewCard = {
                    viewModel.addNewCardAfter(it)
                    analytics.sliderNewCard(it)
                },
                onClickMenuDeleteCard = { page, card ->
                    viewModel.deleteCard(page, card)
                    analytics.sliderDeleteNewCard(page)
                },
                onClickMenuSaveNewCard = { page, card ->
                    viewModel.persistNewCard(page, card)
                    showShortToastMessage(R.string.card_edit_card_added_toast)
                    analytics.createCard(page)
                }),
            setWindowHeightPx = { viewModel.updateWindowHeight(it) })
    }

    private fun showSnackbarDeletedCard() {
        showSnackbar(
            cardDeletedMessage,
            restoreLabel,
            { viewModel.restoreLastDeletedCard() },
            snackbarHostState,
            scope,
            SnackbarDuration.Short
        )
    }

    private fun showShortToastLimitForgottenCardsExceeded() {
        showShortToastMessage(
            String.format(
                application.getString(R.string.card_study_limit_forgotten_cards_exceeded_toast),
                viewModel.sliderCardManager.getMaxAllowedForgottenCards()
            )
        )
    }

    private fun showShortToastMessage(@StringRes resId: Int) {
        showShortToastMessage(application.getString(resId))
    }

    private fun showShortToastMessage(text: String) {
        Toast.makeText(application, text, Toast.LENGTH_SHORT).show()
    }
}

data class StudyPage(
    val studyCards: State<Map<Int, StudyCardUi>>,
    val editingLocked: State<Boolean> = mutableStateOf(false),
    val buttons: DefinitionButtonsActions = DefinitionButtonsActions(true),
    val onClickMenuEditCard: (page: Int) -> Unit = {},
    val onClickMenuNewCard: (page: Int) -> Unit = {},
    val onClickMenuDeleteCard: (page: Int, card: SliderCardUi) -> Unit = { _, _ -> },
    val onClickMenuResetView: (card: StudyCardUi) -> Unit = {},
)

data class EditPage(
    val editCards: State<Map<Int, EditCardUi>>,
    val onClickMenuNewCard: (page: Int) -> Unit = {},
    val onClickMenuDeleteCard: (page: Int, card: SliderCardUi) -> Unit = { _, _ -> },
    val onClickMenuSaveEditCard: (page: Int, card: EditCardUi) -> Unit = { _, _ -> },
)

data class NewPage(
    val newCards: State<Map<Int, EditCardUi>>,
    val onClickMenuNewCard: (page: Int) -> Unit = {},
    val onClickMenuDeleteCard: (page: Int, card: SliderCardUi) -> Unit = { _, _ -> },
    val onClickMenuSaveNewCard: (page: Int, card: EditCardUi) -> Unit = { _, _ -> }
)