package pl.gocards.ui.discover.premium

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import pl.gocards.R
import pl.gocards.ui.discover.DiscoverCard

/**
 * @author Grzegorz Ziemski
 */
data class PremiumInput(
    val isPremium: State<Boolean>,
    val isPremiumSwitch: State<Boolean>,
    val formattedPrice: State<String?>,
    val canFreeTrial: State<Boolean>,
    val setPremium: () -> Unit,
    val onClickBuyPremium: () -> Unit,
    val onDisableSubscription: () -> Unit,
    val onOpenSubscriptions: () -> Unit
)

@Composable
fun PremiumCard(input: PremiumInput) {
    DiscoverCard(
        title = {
            Column(modifier = Modifier.align(Alignment.CenterVertically)) {
                if (input.canFreeTrial.value) {
                    Text(stringResource(R.string.discover_premium_free_trial))
                } else {
                    Text(String.format(stringResource(R.string.discover_premium_title), input.formattedPrice.value))
                }
            }
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.End
            ) {
                Switch(
                    modifier = Modifier.align(Alignment.End),
                    checked = input.isPremiumSwitch.value,
                    onCheckedChange = {
                        if (input.isPremium.value) {
                            input.onDisableSubscription()
                        } else {
                            input.setPremium()
                            input.onClickBuyPremium()
                        }
                    }
                )
            }
        },
        body = {
            Text(
                modifier = Modifier.padding(15.dp),
                text = if (input.isPremium.value) {
                    stringResource(R.string.discover_premium_cancel_subscription) +
                            "\n\n" +
                            stringResource(R.string.discover_premium_features)
                } else if (input.canFreeTrial.value) {
                            stringResource(R.string.discover_premium_features) +
                            "\n\n" +
                            stringResource(R.string.discover_premium_free_trial_prevent_renewal)
                } else {
                    stringResource(R.string.discover_premium_catchphrase) +
                            "\n\n" +
                            stringResource(R.string.discover_premium_features) +
                            "\n\n" +
                            stringResource(R.string.discover_premium_trial_promo_code)
                }
            )
        },
        onClickBody = {
            if (input.isPremium.value) {
                input.onOpenSubscriptions()
            } else {
                input.onClickBuyPremium()
            }
        }
    )
}