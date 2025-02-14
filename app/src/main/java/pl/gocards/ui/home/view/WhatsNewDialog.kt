package pl.gocards.ui.home.view

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.gocards.R


data class WhatsNewDialogInput(
    val onDismiss: () -> Unit
)

@Composable
fun WhatsNewDialog(
    input: WhatsNewDialogInput
) {
    AlertDialog(
        title = { Text(
            text = stringResource(R.string.home_whats_new_dialog_title),
            fontSize = 18.sp,
        ) },
        text = {

            Text(
                modifier = Modifier
                    .padding(bottom = 10.dp),
                text = stringResource(R.string.home_whats_new_dialog_message)
            )
        },
        onDismissRequest = {
            input.onDismiss()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    input.onDismiss()
                }
            ) {
                Text(stringResource(R.string.ok))
            }
        }
    )
}