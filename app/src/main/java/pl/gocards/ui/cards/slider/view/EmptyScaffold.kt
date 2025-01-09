package pl.gocards.ui.cards.slider.view

import android.annotation.SuppressLint
import androidx.compose.foundation.basicMarquee
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import pl.gocards.ui.theme.AppBar
import pl.gocards.ui.theme.ExtendedTheme

/**
 * @author Grzegorz Ziemski
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
fun EmptyScaffold(
    onBack: () -> Unit = {},
    isDarkTheme: Boolean,
    deckName: String,
) {
    Scaffold(
        topBar = {
            AppBar(
                isDarkTheme = isDarkTheme,
                title = {
                    Text(
                        text = deckName,
                        maxLines = 1,
                        modifier = Modifier.basicMarquee()
                    )
                },
                onBack = onBack
            )
        },
        containerColor = ExtendedTheme.colors.behindWindowBackground,
        content = { }
    )
}