package pl.gocards.room.entity.app

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @author Grzegorz Ziemski
 */
@Entity
data class AppConfig(
    @PrimaryKey
    var key: String,
    var value: String
) {
    companion object {
        const val DARK_MODE = "DarkMode"
        const val DARK_MODE_DEFAULT = "System"
        const val DARK_MODE_ON = "On"
        const val DARK_MODE_OFF = "Off"
        val DARK_MODE_OPTIONS = listOf(DARK_MODE_DEFAULT, DARK_MODE_ON, DARK_MODE_OFF)
        const val EDGE_BAR_OFF = "Off"
        const val EDGE_BAR_SHOW_LEARNING_STATUS = "LearningStatus"
        const val EDGE_BAR_SHOW_RECENTLY_SYNCED = "RecentlySynced"
        const val LEFT_EDGE_BAR = "LeftEdgeBar"
        const val LEFT_EDGE_BAR_DEFAULT = EDGE_BAR_SHOW_LEARNING_STATUS
        const val RIGHT_EDGE_BAR = "RightEdgeBar"
        const val RIGHT_EDGE_BAR_DEFAULT = EDGE_BAR_SHOW_RECENTLY_SYNCED
        const val PREMIUM = "Premium"
        const val FIRST_USED_AT = "FirstUsedAt"
        const val LAST_EXCEPTION_AT = "LastExceptionAt"
        const val EXPLORE_POLL_COMPLETED = "ExplorePollCompleted"
        const val LATEST_SHOWN_WHATS_NEW_VERSION = "LatestShownWhatsNewVersion"
        const val WHATS_NEW_REMAINING_SHOWS = "WhatsNewRemainingShows"
    }
}