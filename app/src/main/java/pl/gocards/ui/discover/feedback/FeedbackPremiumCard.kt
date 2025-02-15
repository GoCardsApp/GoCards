package pl.gocards.ui.discover.feedback

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import pl.gocards.R
import pl.gocards.ui.discover.DiscoverCard

/**
 * @author Grzegorz Ziemski
 */
data class FeedbackPremiumInput(
    val onClickTakeSurvey: () -> Unit
)

@Composable
fun FeedbackPremiumCard(input: FeedbackPremiumInput) {
    DiscoverCard(
        title = {
            Column(modifier = Modifier.align(Alignment.CenterVertically)) {
                Text(
                    modifier = Modifier.padding(0.dp, 10.dp),
                    text = stringResource(R.string.discover_feedback_premium_title),
                )
            }
        },
        body = {
            Text(
                modifier = Modifier.padding(15.dp),
                text = stringResource(R.string.discover_feedback_premium_description)
            )
        },
        onClickBody = { input.onClickTakeSurvey() }
    )
}