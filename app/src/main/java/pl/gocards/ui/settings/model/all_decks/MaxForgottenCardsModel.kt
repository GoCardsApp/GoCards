package pl.gocards.ui.settings.model.all_decks

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pl.gocards.room.entity.deck.DeckConfig
import pl.gocards.db.room.AppDatabase
import pl.gocards.ui.settings.model.SettingModel
import pl.gocards.ui.settings.model.deck.DeckMaxForgottenCardsModel

/**
 * S_U_05 All decks: Limit forgotten cards
 * @author Grzegorz Ziemski
 */
class MaxForgottenCardsModel(
    private val appDb: AppDatabase,
    application: Application
) : SettingModel(application) {

    var deckMaxForgottenCards: DeckMaxForgottenCardsModel? = null

    val maxForgottenCards: MutableLiveData<String> = MutableLiveData(DeckConfig.MAX_ALLOWED_FORGOTTEN_CARDS_DEFAULT.toString())
    var appMaxForgottenCardsDb: Int = DeckConfig.MAX_ALLOWED_FORGOTTEN_CARDS_DEFAULT

    @SuppressLint("CheckResult")
    fun init() {
        viewModelScope.launch(Dispatchers.IO) {
            val appConfig = appDb.appConfigKtxDao().getByKey(DeckConfig.MAX_ALLOWED_FORGOTTEN_CARDS)
            if (appConfig != null) {
                set(appConfig.value)
                appMaxForgottenCardsDb = appConfig.value.toInt()
            }
        }
    }

    fun set(newValue: Any) {
        set(maxForgottenCards, newValue, "0")
    }

    fun reset() {
        maxForgottenCards.value = appMaxForgottenCardsDb.toString()
    }

    @SuppressLint("CheckResult")
    fun commit() {
        maxForgottenCards.value?.let {
            if (it.isNotEmpty()) {
                viewModelScope.launch(Dispatchers.IO) {
                    appMaxForgottenCardsDb = it.toInt()
                    appDb.appConfigKtxDao().update(
                        DeckConfig.MAX_ALLOWED_FORGOTTEN_CARDS,
                        it,
                        DeckConfig.MAX_ALLOWED_FORGOTTEN_CARDS_DEFAULT.toString()
                    )
                    deckMaxForgottenCards?.init()
                }
            }
        }
    }
}