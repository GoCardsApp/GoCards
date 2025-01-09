package pl.gocards.room.entity.deck

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @author Grzegorz Ziemski
 */
@Entity(tableName = "Core_DeckConfig")
@SuppressWarnings("unused")
data class DeckConfig(
    @PrimaryKey
    var key: String,
    var value: String
) {
    companion object {
        const val FILE_SYNC_EDITING_BLOCKED_AT = "FileSync_EditingBlockedAt"
        const val STUDY_CARD_TERM_FONT_SIZE = "StudyCard_Term_FontSize"
        const val STUDY_CARD_DEFINITION_FONT_SIZE = "StudyCard_Definition_FontSize"
        const val STUDY_CARD_FONT_SIZE_DEFAULT = 24
        const val STUDY_CARD_FONT_SIZE_MIN = 10
        const val STUDY_CARD_FONT_SIZE_MAX = 50
        const val MAX_ALLOWED_FORGOTTEN_CARDS = "StudyCard_MaxForgottenCards"
        const val MAX_ALLOWED_FORGOTTEN_CARDS_DEFAULT = 5
        const val MAX_LINES = "ListCards_MaxLines"
        const val MAX_LINES_DEFAULT = 4
    }
}