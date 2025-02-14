package pl.gocards.ui.cards.slider.page.study.ui.definition

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import pl.gocards.R
import pl.gocards.room.entity.deck.CardLearningProgressAndHistory
import pl.gocards.ui.cards.slider.page.card.model.SliderCardUi
import pl.gocards.ui.cards.slider.page.study.model.StudyCardUi
import pl.gocards.ui.theme.Blue800
import pl.gocards.ui.theme.Green700
import pl.gocards.ui.theme.Orange800
import pl.gocards.ui.theme.Red900
import pl.gocards.util.CardReplayScheduler

/**
 * @author Grzegorz Ziemski
 */
@Composable
fun DefinitionBox(
    page: Int,
    pagerState: PagerState,
    sliderCard: SliderCardUi,
    studyCard: StudyCardUi,
    minSlideToY: Int,
    maxSlideToY: Int,
    sliderTouchSpace: Int,
    windowHeight: Int,
    darkMode: Boolean,
    onScroll: (enabled: Boolean) -> Unit,
    buttonsActions: DefinitionButtonsActions
) {
    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
        val (definitionBoxRef, ratingButtonsRef) = createRefs()

        DefinitionContentBox(
            page,
            pagerState,
            Modifier
                .constrainAs(definitionBoxRef) {
                    start.linkTo(parent.start)
                    top.linkTo(parent.top)
                    end.linkTo(parent.end)
                    if (buttonsActions.showRateButtons)
                        bottom.linkTo(ratingButtonsRef.top)
                    else bottom.linkTo(
                        parent.bottom
                    )
                    height = Dimension.fillToConstraints
                    width = Dimension.fillToConstraints
                },
            studyCard,
            minSlideToY,
            maxSlideToY,
            sliderTouchSpace,
            windowHeight,
            darkMode,
            onScroll
        )

        if (buttonsActions.showRateButtons) {
            Row(modifier = Modifier
                .height(intrinsicSize = IntrinsicSize.Max)
                .constrainAs(ratingButtonsRef) {
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    bottom.linkTo(parent.bottom)
                    width = Dimension.fillToConstraints
                }
            ) {
                RateButton(
                    text = stringResource(R.string.card_study_again),
                    onClick = { buttonsActions.onClickAgain(page, sliderCard) },
                    containerColor = Red900,
                )

                if (studyCard.nextAfterQuick != null) {
                    RateButton(
                        text = getQuickTextButton(
                            progressAndHistory = studyCard.nextAfterQuick
                        ),
                        textStyle = MaterialTheme.typography.labelLarge.copy(
                            lineHeight = 11.sp,
                            fontSize = 10.sp,
                        ),
                        onClick = { buttonsActions.onClickQuick(page, sliderCard) },
                        containerColor = Orange800,
                    )
                }

                RateButton(
                    text = getHardTextButton(
                        current = studyCard.current,
                        progressAndHistory = studyCard.nextAfterHard
                    ),
                    onClick = { buttonsActions.onClickHard(page, sliderCard) },
                    containerColor = Blue800,
                )

                RateButton(
                    text = getEasyTextButton(
                        current = studyCard.current,
                        progressAndHistory = studyCard.nextAfterEasy
                    ),
                    onClick = { buttonsActions.onClickEasy(page, sliderCard) },
                    containerColor = Green700,
                )
            }
        }
    }
}

data class DefinitionButtonsActions(
    val showRateButtons: Boolean = true,
    val onClickAgain: (Int, SliderCardUi) -> Unit = { _, _ -> },
    val onClickQuick: (Int, SliderCardUi) -> Unit = { _, _ -> },
    val onClickEasy: (Int, SliderCardUi) -> Unit = { _, _ -> },
    val onClickHard: (Int, SliderCardUi) -> Unit = { _, _ -> },
)

@Suppress("SameParameterValue")
@Composable
private fun RowScope.RateButton(
    text: String,
    textStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.labelLarge.copy(
        lineHeight = 15.sp,
        fontSize = 14.sp,
    ),
    onClick: () -> Unit,
    containerColor: Color
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            contentColor = Color.White,
            containerColor = containerColor
        ),
        shape = RoundedCornerShape(10),
        modifier = Modifier
            .weight(1f)
            .padding(2.dp)
            .fillMaxHeight(),
        contentPadding = PaddingValues(
            start = 0.dp,
            top = 8.dp,
            end = 0.dp,
            bottom = 8.dp
        ),
    ) {
        Text(
            text = text,
            style = textStyle,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun getQuickTextButton(
    progressAndHistory: CardLearningProgressAndHistory
): String {
    return String.format(
        displayIntervalWithTime(
            progressAndHistory.history.interval.toFloat(),
            R.string.card_study_quick_repetition_min,
            R.string.card_study_quick_repetition_hours
        )
    )
}

@Composable
private fun getEasyTextButton(
    current: CardLearningProgressAndHistory?,
    progressAndHistory: CardLearningProgressAndHistory
): String {
    val history = progressAndHistory.history
    return if (wasNeverMemorized(current)) {
        // It always shows 1. It looks better without the day.
        displayIntervalWithDays(
            history.interval.toFloat(),
            R.string.card_study_easy_min,
            R.string.card_study_easy_hours,
            R.string.card_study_easy_day_only_again,
            R.string.card_study_easy_days
        )
    } else {
        displayIntervalWithDays(
            history.interval.toFloat(),
            R.string.card_study_easy_min,
            R.string.card_study_easy_hours,
            R.string.card_study_easy_day,
            R.string.card_study_easy_days
        )
    }
}

@Composable
private fun getHardTextButton(
    current: CardLearningProgressAndHistory?,
    progressAndHistory: CardLearningProgressAndHistory
): String {
    val history = progressAndHistory.history
    return if (wasNeverMemorized(current)) {
        // It always shows 1. It looks better without the day.
        displayIntervalWithDays(
            history.interval.toFloat(),
            R.string.card_study_hard_min,
            R.string.card_study_hard_hours,
            R.string.card_study_hard_day_only_again,
            R.string.card_study_hard_days
        )
    } else {
        displayIntervalWithDays(
            history.interval.toFloat(),
            R.string.card_study_hard_min,
            R.string.card_study_hard_hours,
            R.string.card_study_hard_day,
            R.string.card_study_hard_days
        )
    }
}

private const val DIVIDE_MINUTES_TO_HOURS = 60f
private const val DIVIDE_MINUTES_TO_DAYS = (24 * 60).toFloat()
private const val ONE_DAY = 24 * 60
private const val TWO_DAYs = 2 * ONE_DAY

@Composable
private fun displayIntervalWithDays(
    interval: Float,
    @StringRes min: Int,
    @StringRes hours: Int,
    @StringRes day: Int,
    @StringRes days: Int
): String {
    if (interval < ONE_DAY) {
        return displayIntervalWithTime(interval, min, hours)
    } else if (interval < TWO_DAYs) {
        return String.format(
            stringResource(day),
            interval / DIVIDE_MINUTES_TO_DAYS
        )
    }
    return String.format(stringResource(days), interval / DIVIDE_MINUTES_TO_DAYS)
}

@Composable
private fun displayIntervalWithTime(
    interval: Float,
    @StringRes min: Int,
    @StringRes hours: Int
): String {
    if (interval < 60) {
        return String.format(
            stringResource(min), interval.toInt()
        )
    } else if (interval < 24 * 60) {
        return String.format(
            stringResource(hours),
            interval / DIVIDE_MINUTES_TO_HOURS
        )
    }
    throw UnsupportedOperationException("It is more than 1 day.")
}

/**
 * The only click was in again.
 */
private fun wasNeverMemorized(current: CardLearningProgressAndHistory?): Boolean {
    return current != null
            && !current.progress.isMemorized
            && current.history.interval == CardReplayScheduler.AGAIN_FIRST_INTERVAL_MINUTES
}