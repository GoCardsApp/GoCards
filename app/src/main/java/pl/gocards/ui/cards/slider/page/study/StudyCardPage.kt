package pl.gocards.ui.cards.slider.page.study

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import pl.gocards.R
import pl.gocards.room.entity.deck.CardLearningHistory
import pl.gocards.room.entity.deck.CardLearningProgress
import pl.gocards.room.entity.deck.CardLearningProgressAndHistory
import pl.gocards.ui.cards.slider.page.card.SliderCardPage
import pl.gocards.ui.cards.slider.page.card.model.CardMode
import pl.gocards.ui.cards.slider.page.card.model.SliderCardUi
import pl.gocards.ui.cards.slider.page.study.model.StudyCardUi
import pl.gocards.ui.cards.slider.page.study.ui.SlidingDivider
import pl.gocards.ui.cards.slider.page.study.ui.TermBox
import pl.gocards.ui.cards.slider.page.study.ui.definition.DefinitionBox
import pl.gocards.ui.cards.slider.page.study.ui.definition.DefinitionButtonsActions
import pl.gocards.ui.cards.slider.page.study.ui.definition.ShowDefinitionBox
import pl.gocards.ui.common.SliderDropdownMenuItem
import pl.gocards.ui.theme.AppTheme

val testStudyCardUi = StudyCardUi(
    id = 1,
    term = "Term",
    definition = "Definition",
    termHeightPx = mutableStateOf(800f),
    current = null,
    nextAfterAgain = CardLearningProgressAndHistory(
        progress = CardLearningProgress(),
        history = CardLearningHistory()
    ),
    nextAfterQuick = CardLearningProgressAndHistory(
        progress = CardLearningProgress(),
        history = CardLearningHistory()
    ),
    nextAfterEasy = CardLearningProgressAndHistory(
        progress = CardLearningProgress(),
        history = CardLearningHistory()
    ),
    nextAfterHard = CardLearningProgressAndHistory(
        progress = CardLearningProgress(),
        history = CardLearningHistory()
    )
)

val testSliderCardUi = SliderCardUi(
    id = 1,
    ordinal = 1,
    cardMode = mutableStateOf(CardMode.STUDY)
)

@Preview(showBackground = true)
@Composable
fun PreviewStudyCardPage() {
    AppTheme(preview=true) {
        StudyCardPage(
            sliderCard = testSliderCardUi,
            studyCard = testStudyCardUi,
            page = 0,
            pagerState = rememberPagerState(pageCount = { 1 }),
            innerPadding = PaddingValues(),
            darkMode = true,
            studyCardLayoutParams = remember {
                mutableStateOf(
                    StudyCardLayoutParams(
                        windowHeightPx = 800,
                        minSlideToY = 200,
                        maxSlideToY = 200,
                        sliderTouchSpace = 200
                    )
                )
            }
        )
    }
}


/**
 * C_R_30 Study the cards
 * @author Grzegorz Ziemski
 */
@Composable
fun StudyCardPage(
    sliderCard: SliderCardUi,
    studyCard: StudyCardUi,

    page: Int,
    pagerState: PagerState,
    innerPadding: PaddingValues,
    studyCardLayoutParams: MutableState<StudyCardLayoutParams>,
    darkMode: Boolean,
    onScroll: (enabled: Boolean) -> Unit = {},

    definitionButtonsActions: DefinitionButtonsActions = DefinitionButtonsActions()
) {
    SliderCardPage(
        page = page,
        pagerState = pagerState,
        modifier = Modifier
            .padding(innerPadding)
            .setStudyCardLayoutParams(studyCardLayoutParams)
    ) {
        TermBox(
            page = page,
            pagerState = pagerState,
            studyCard = studyCard,
            minSlideToY = studyCardLayoutParams.value.minSlideToY,
            maxSlideToY = studyCardLayoutParams.value.maxSlideToY,
            sliderTouchSpace = studyCardLayoutParams.value.sliderTouchSpace,
            height = studyCardLayoutParams.value.windowHeightPx,
            onScroll = onScroll,
            darkMode = darkMode,
        )
        SlidingDivider(
            termHeightPx = studyCard.termHeightPx,
            minSlideToY = studyCardLayoutParams.value.minSlideToY,
            maxSlideToY = studyCardLayoutParams.value.maxSlideToY
        )
        if (!studyCard.showDefinition.value) {
            ShowDefinitionBox(
                onClick = { studyCard.showDefinition.value = true },
                studyCard,
                minSlideToY = studyCardLayoutParams.value.minSlideToY,
                maxSlideToY = studyCardLayoutParams.value.maxSlideToY,
                sliderTouchSpace = studyCardLayoutParams.value.sliderTouchSpace,
            )
        } else {
            DefinitionBox(
                page,
                pagerState,
                sliderCard,
                studyCard,
                minSlideToY = studyCardLayoutParams.value.minSlideToY,
                maxSlideToY = studyCardLayoutParams.value.maxSlideToY,
                sliderTouchSpace = studyCardLayoutParams.value.sliderTouchSpace,
                windowHeight = studyCardLayoutParams.value.windowHeightPx,
                darkMode = darkMode,
                onScroll = onScroll,
                buttonsActions = definitionButtonsActions
            )
        }
    }
}

/**
 * C_U_37 Adjust the term/definition ratio by scrolling the slider.
 */
fun Modifier.setStudyCardLayoutParams(
    studyCardLayoutParams: MutableState<StudyCardLayoutParams>
) = onGloballyPositioned { coordinates ->
    val windowHeightPx = coordinates.size.height
    if (windowHeightPx != studyCardLayoutParams.value.windowHeightPx) {
        studyCardLayoutParams.value = StudyCardLayoutParams(
            windowHeightPx = windowHeightPx,
            minSlideToY = (0.1 * windowHeightPx).toInt(),
            maxSlideToY = (0.9 * windowHeightPx).toInt(),
            sliderTouchSpace = (0.1 * windowHeightPx).toInt()
        )
    }
}

data class StudyCardLayoutParams(
    val windowHeightPx: Int = 0,
    val minSlideToY: Int = 0,
    val maxSlideToY: Int = 0,
    val sliderTouchSpace: Int = 0
)

@Composable
fun StudyCardPageMenu(
    editingLock: Boolean,
    onClickMenuEditCard: () -> Unit = {},
    onClickMenuNewCard: () -> Unit = {},
    onClickMenuDeleteCard: () -> Unit = {},
    onClickMenuResetView: () -> Unit = {}
) {
    if (!editingLock) {
        IconButton(onClick = onClickMenuEditCard) {
            Icon(Icons.Filled.Edit, stringResource(R.string.edit))
        }
    }
    val showDropDown = remember { mutableStateOf(false) }
    IconButton(
        onClick = { showDropDown.value = true }) {
        Icon(Icons.Filled.MoreVert, stringResource(R.string.more))
    }
    DropdownMenu(showDropDown.value, { showDropDown.value = false }) {
        if (!editingLock) {
            SliderDropdownMenuItem(
                icon = Icons.Filled.Add,
                text = R.string.neww,
                showDropDown = showDropDown,
                onClick = onClickMenuNewCard
            )
            SliderDropdownMenuItem(
                icon = Icons.Filled.Delete,
                text = R.string.delete,
                showDropDown = showDropDown,
                onClick = onClickMenuDeleteCard
            )
        }
        SliderDropdownMenuItem(
            icon = Icons.AutoMirrored.Filled.Undo,
            text = R.string.card_study_menu_reset_view,
            showDropDown = showDropDown,
            onClick = onClickMenuResetView
        )
    }
}

