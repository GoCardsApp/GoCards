package pl.gocards.ui.common

import android.app.Activity
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.annotation.UiThread
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.recyclerview.widget.RecyclerView

/**
 * @author Grzegorz Ziemski
 */
abstract class CommonAdapter<VH : RecyclerView.ViewHolder>(
    open val activity: Activity,
    val snackbarHostState: SnackbarHostState
) : RecyclerView.Adapter<VH>() {

    @UiThread
    protected fun showShortToastMessage(@StringRes resId: Int) {
        Toast.makeText(
            activity,
            activity.getString(resId),
            Toast.LENGTH_SHORT
        ).show()
    }

    @Suppress("SameParameterValue")
    protected suspend fun showSnackbar(
        @StringRes message: Int,
        @StringRes button: Int,
        onAction: () -> Unit
    ) {
        showSnackbar(
            activity.getString(message),
            activity.getString(button),
            onAction,
            snackbarHostState,
            SnackbarDuration.Long
        )
    }
}