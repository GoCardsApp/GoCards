package pl.gocards.ui.cards.slider.page.edit

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pl.gocards.R
import pl.gocards.ui.cards.slider.page.card.SliderCardPage
import pl.gocards.ui.cards.slider.page.edit.model.EditCardUi
import java.util.Date

val testEditCardUi = @Composable {
    EditCardUi(
        id = 1,
        nextReplayAt = Date(),
        disabled = remember { mutableStateOf(true) }
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewEditCardPage() {
    EditCardPage(
        page = 0,
        editCard = testEditCardUi(),
        pagerState = rememberPagerState(pageCount = { 1 }),
        innerPadding = PaddingValues(),
    )
}

/**
 * C_C_24 Edit the card
 * @author Grzegorz Ziemski
 */
@Composable
fun EditCardPage(
    page: Int,
    editCard: EditCardUi,
    pagerState: PagerState,
    innerPadding: PaddingValues,
) {
    SliderCardPage(
        page = page,
        pagerState = pagerState,
        modifier = Modifier.padding(innerPadding)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            MultilineEditField(R.string.card_new_term_label, editCard.term)
            MultilineEditField(R.string.card_new_definition_label, editCard.definition, 15)
            NextReplayAtField(editCard.nextReplayAt)
            DisabledField(editCard.disabled)
        }
    }
}

@Composable
fun ColumnScope.MultilineEditField(
    @StringRes label: Int,
    value: MutableState<String> = mutableStateOf(""),
    lines: Int = 6,
) {
    OutlinedTextField(
        value = value.value,
        onValueChange = { value.value = it },
        label = { Text(stringResource(label)) },
        minLines = lines,
        maxLines = lines,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp, 8.dp, 8.dp, 0.dp)
            .align(alignment = Alignment.CenterHorizontally),
        textStyle = LocalTextStyle.current.copy(
            textAlign = TextAlign.Center
        )
    )
}

@Composable
fun ColumnScope.NextReplayAtField(nextReplayAt: Date?) {
    OutlinedTextField(
        value = nextReplayAt?.toString() ?: "",
        enabled = false,
        onValueChange = { },
        label = { Text(stringResource(R.string.card_edit_next_replay_at_label)) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp, 8.dp, 8.dp, 0.dp)
            .align(alignment = Alignment.CenterHorizontally),
    )
}

@Composable
fun DisabledField(disabled: MutableState<Boolean>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp, 8.dp, 8.dp, 0.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = stringResource(R.string.card_new_disabled_label))
        Switch(
            checked = disabled.value,
            onCheckedChange = { disabled.value = it }
        )
    }
}