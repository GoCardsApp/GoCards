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
data class FeedbackReviewInput(
    val onClickTakeSurvey: () -> Unit
)

@Composable
fun FeedbackReviewCard(input: FeedbackReviewInput) {
    DiscoverCard(
        title = {
            Column(modifier = Modifier.align(Alignment.CenterVertically)) {
                Text(
                    modifier = Modifier.padding(0.dp, 10.dp),
                    text = stringResource(R.string.discover_feedback_review_title),
                )
            }
        },
        body = {
            Text(
                modifier = Modifier.padding(15.dp),
                text = stringResource(R.string.discover_feedback_review_description)
            )
        },
        onClickBody = { input.onClickTakeSurvey() }
    )
}