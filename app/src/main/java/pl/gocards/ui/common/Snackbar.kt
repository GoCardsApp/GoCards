package pl.gocards.ui.common

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

fun showSnackbar(
    message: String,
    actionLabel: String,
    @SuppressWarnings("unused")
    onAction: () -> Unit,
    snackbarHostState: SnackbarHostState,
    scope: CoroutineScope,
    duration: SnackbarDuration
) {
    scope.launch {
        showSnackbar(
            message,
            actionLabel,
            onAction,
            snackbarHostState,
            duration
        )
    }
}

suspend fun showSnackbar(
    message: String,
    actionLabel: String,
    @SuppressWarnings("unused")
    onAction: () -> Unit,
    snackbarHostState: SnackbarHostState,
    duration: SnackbarDuration
) {
    val result = snackbarHostState.showSnackbar(
        message,
        actionLabel,
        duration = duration
    )

    when (result) {
        SnackbarResult.ActionPerformed -> {
            onAction()
        }

        SnackbarResult.Dismissed -> {
            /* Handle snackbar dismissed */
        }
    }
}