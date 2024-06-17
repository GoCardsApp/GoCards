package pl.gocards.ui.decks.kt.folders.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import pl.gocards.R

/**
 * F_C_03 Create a folder
 * @author Grzegorz Ziemski
 */
data class CreateFolderDialogInput(
    val onCreateFolder: (name: String) -> Unit = {},
    val onDismiss: () -> Unit
)

/**
 * F_C_03 Create a folder
 * @author Grzegorz Ziemski
 */
@Composable
fun CreateFolderDialog(
    input: CreateFolderDialogInput
) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        title = { Text(text = stringResource(R.string.decks_list_folder_create_dialog_title)) },
        text = {
            Column {
                Text(
                    modifier = Modifier
                        .padding(bottom = 10.dp),
                    text = stringResource(R.string.decks_list_folder_create_dialog_message)
                )
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                )
            }
        },
        onDismissRequest = {
            input.onDismiss()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotEmpty()) {
                        input.onCreateFolder(name)
                    }
                }
            ) {
                Text(stringResource(R.string.ok))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    input.onDismiss()
                }
            ) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}