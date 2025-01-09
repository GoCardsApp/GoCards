/**
 * @author Grzegorz Ziemski
 */

package pl.gocards.ui.cards.slider.page.card.model

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable

/**
 * The card used in the UI slider.
 *
 * @author Grzegorz Ziemski
 */
@Stable
data class SliderCardUi(
    var id: Int,
    /**
     * Th value may be incorrect because it is not refreshed after slider changes
     */
    var ordinal: Int? = null,
    var deletedAt: Long? = null,
    val cardMode: MutableState<CardMode>
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SliderCardUi

        if (id != other.id) return false
        if (ordinal != other.ordinal) return false
        if (deletedAt != other.deletedAt) return false
        return cardMode.value == other.cardMode.value
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + (ordinal ?: 0)
        result = 31 * result + (deletedAt?.hashCode() ?: 0)
        result = 31 * result + cardMode.value.hashCode()
        return result
    }
}

enum class CardMode {
    STUDY, EDIT, NEW
}