package pl.gocards.ui.home

import android.app.Activity
import kotlinx.coroutines.CoroutineScope
import pl.gocards.App
import pl.gocards.room.dao.app.AppConfigKtxDao
import pl.gocards.room.entity.app.AppConfig
import pl.gocards.ui.common.addViewToRoot
import pl.gocards.ui.home.view.WhatsNewDialog
import pl.gocards.ui.home.view.WhatsNewDialogInput
import pl.gocards.ui.theme.AppTheme

/**
 * @author Grzegorz Ziemski
 */
class HomeDialogs(
    private val appConfigKtxDao: AppConfigKtxDao,
    private val activity: Activity,
    private val scope: CoroutineScope,
    val application: App
) {

    suspend fun showWhatsNewDialogIfNeeded() {
        planDisplayCountIfNeeded()
        val remainingShows = appConfigKtxDao.getLongByKey(AppConfig.WHATS_NEW_REMAINING_SHOWS)
        if (remainingShows != null) {
            showWhatsNewDialog()
            reduceRemainingShows(remainingShows)
        }
    }

    private suspend fun planDisplayCountIfNeeded() {
        val lastUsedVersion = appConfigKtxDao.getLongByKey(AppConfig.LATEST_SHOWN_WHATS_NEW_VERSION)

        if (lastUsedVersion == null || lastUsedVersion < CURRENT_WHATS_NEW_VERSION) {
            appConfigKtxDao.update(
                AppConfig.LATEST_SHOWN_WHATS_NEW_VERSION,
                CURRENT_WHATS_NEW_VERSION.toString(),
                null
            )
            appConfigKtxDao.update(
                AppConfig.WHATS_NEW_REMAINING_SHOWS,
                2L.toString(),
                null
            )
        }
    }

    private suspend fun reduceRemainingShows(remainingShows: Long) {
        if (remainingShows <= 1) {
            appConfigKtxDao.deleteByKey(AppConfig.WHATS_NEW_REMAINING_SHOWS)
        } else {
            appConfigKtxDao.update(
                AppConfig.WHATS_NEW_REMAINING_SHOWS,
                (remainingShows - 1).toString(),
                0L.toString()
            )
        }
    }

    private fun showWhatsNewDialog() {
        addViewToRoot(activity, scope) { onDismiss ->
            val input = WhatsNewDialogInput(onDismiss = onDismiss)
            AppTheme(isDarkTheme = true) {
                WhatsNewDialog(input)
            }
        }
    }

    companion object {
        const val CURRENT_WHATS_NEW_VERSION = 1L
    }
}