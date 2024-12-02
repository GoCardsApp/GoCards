package pl.gocards.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat


@Composable
fun SelectBarColorsTheme(
    isDarkTheme: Boolean,
    colorScheme: ColorScheme,
    isSelectionMode: Boolean,
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        return
    }

    if (isSelectionMode) {
        val appBarContainerColor by animateColorAsState(
            targetValue = ExtendedTheme.colors.colorItemSelected,
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
            label = "appBarContainerColor",
        )
        BarColorsTheme(isDarkTheme, statusBarColor = appBarContainerColor)
    } else {
        BarColorsTheme(isDarkTheme, colorScheme)
    }
}

@Composable
private fun BarColorsTheme(
    isDarkTheme: Boolean,
    colorScheme: ColorScheme? = null,
    statusBarColor: Color = colorScheme?.surfaceColorAtElevation(2.dp) ?: Color.Unspecified,
    navigationBarColor: Color = colorScheme?.surfaceColorAtElevation(3.dp) ?: Color.Unspecified
) {
    val view = LocalView.current
    SideEffect {
        val window = (view.context as Activity).window
        window.statusBarColor = statusBarColor.toArgb()
        window.navigationBarColor = navigationBarColor.toArgb()

        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDarkTheme
        WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !isDarkTheme
    }
}