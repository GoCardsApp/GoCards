package pl.gocards.ui.cards.slider.view

import android.annotation.SuppressLint
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pl.gocards.R
import pl.gocards.dynamic_pager.DynamicPagerStateBinder
import pl.gocards.ui.cards.slider.page.edit.testEditCardUi
import pl.gocards.ui.cards.slider.page.study.NoMoreCardsToRepeatDialog
import pl.gocards.ui.cards.slider.page.study.StudyCardLayoutParams
import pl.gocards.ui.cards.slider.page.study.testStudyCardUi
import pl.gocards.dynamic_pager.DynamicPagerUIMediator
import pl.gocards.ui.cards.slider.page.card.model.CardMode
import pl.gocards.ui.cards.slider.page.card.model.SliderCardUi
import pl.gocards.ui.theme.AppBar
import pl.gocards.ui.theme.AppTheme
import pl.gocards.ui.theme.Blue800
import pl.gocards.ui.theme.ExtendedTheme


@Preview(showBackground = true)
@Composable
@SuppressLint("MutableCollectionMutableState")
fun PreviewKtCardSliderActivity() {
    val testEditCardUi = testEditCardUi()
    val sliderCards = mutableListOf(SliderCardUi(id = 1, cardMode = remember { mutableStateOf(CardMode.EDIT) }))

    CardSliderScaffold(
        input = CardSliderUIMediator(
            isDarkTheme = false,
            preview = true,
            deckName = "Sample deck",
            sliderCards =  sliderCards,
            loaded = true,
            dynamicPager = DynamicPagerUIMediator(
                settledPage = remember { mutableIntStateOf(0) },
                items = remember { mutableStateOf(sliderCards) },
                setSettledPage = {},
                clearFocusPage = {},
                clearScrollToPage = {}
            ),
            studyPage = StudyPage(
                studyCards = remember { mutableStateOf(mapOf(1 to testStudyCardUi)) },
            ),
            editPage = EditPage(
                editCards = remember { mutableStateOf(mapOf(1 to testEditCardUi)) },
            ),
            newPage = NewPage(
                newCards = remember { mutableStateOf(mapOf(1 to testEditCardUi)) }
            )
        )
    )
}

/**
 * @author Grzegorz Ziemski
 */
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardSliderScaffold(input: CardSliderUIMediator) {
    AppTheme(isDarkTheme = input.isDarkTheme, preview = input.preview) {
        if (input.dynamicPager.getSize() > 0) {
            val currentPage = input.dynamicPager.settledPage.value ?: 0
            val studyCardLayoutParams = remember { mutableStateOf(StudyCardLayoutParams()) }

            LaunchedEffect(studyCardLayoutParams) {
                snapshotFlow { studyCardLayoutParams.value }
                    .collect {
                        input.setWindowHeightPx(it.windowHeightPx)
                    }
            }

            DynamicPagerStateBinder(input.dynamicPager) { pagerState ->
                Scaffold(
                    topBar = {
                        AppBar(
                            isDarkTheme = input.isDarkTheme,
                            title = {
                                val sliderCards = input.sliderCards
                                if (sliderCards.size > pagerState.currentPage) {
                                    val sliderCard = sliderCards[pagerState.currentPage]
                                    AppBarTitle(input.deckName, sliderCard)
                                }
                            },
                            onBack = input.onBack,
                            actions = {
                                CardSliderMenu(
                                    page = currentPage,
                                    sliderCards = input.sliderCards,
                                    studyPage = input.studyPage,
                                    newPage = input.newPage,
                                    editPage = input.editPage,
                                )
                            }
                        )
                    },
                    containerColor = ExtendedTheme.colors.behindWindowBackground,
                    snackbarHost = { SnackbarHost(hostState = input.snackbarHostState) },
                    content = { innerPadding ->
                        CardSliderPager(
                            pagerState = pagerState,
                            sliderCards = input.sliderCards,
                            studyCards = input.studyPage?.studyCards,
                            newCards = input.newPage.newCards,
                            editCards = input.editPage.editCards,
                            definitionButtonsActions = input.studyPage?.buttons,
                            studyCardLayoutParams = studyCardLayoutParams,
                            innerPadding = innerPadding,
                            darkMode = input.isDarkTheme,
                            userScrollEnabled = input.dynamicPager.userScrollEnabled
                        )

                        if (input.studyPage?.editingLocked?.value == true) {
                            EditingLocked(Modifier.padding(innerPadding))
                        }
                    }
                )
            }
        } else {
            EmptyScaffold(input.onBack, input.isDarkTheme, input.deckName)
            if (input.loaded) {
                NoMoreCardsToRepeatDialog { input.noMoreCardsToRepeat() }
            }
        }
    }
}

@Composable
private fun AppBarTitle(
    deckName: String,
    sliderCard: SliderCardUi
) {
    val mode = sliderCard.cardMode.value
    when (mode) {
        CardMode.NEW -> {
            Text(
                text = stringResource(R.string.card_new_title),
                maxLines = 1,
                modifier = Modifier.basicMarquee()
            )
        }
        CardMode.EDIT -> {
            Text(
                text = stringResource(R.string.card_edit_title),
                maxLines = 1,
                modifier = Modifier.basicMarquee()
            )
        }
        else -> {
            Text(
                text = deckName,
                maxLines = 1,
                modifier = Modifier.basicMarquee()
            )
        }
    }
}

@Composable
private fun EditingLocked(modifier: Modifier) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Surface(
            color = Blue800,
            shape = RoundedCornerShape(0.dp, 0.dp, 10.dp, 10.dp),
            modifier = modifier.align(Alignment.Center)
        ) {
            Text(
                modifier = Modifier.padding(10.dp, 8.dp),
                text = stringResource(R.string.cards_list_editing_locked),
                color = Color.White
            )
        }
    }
}