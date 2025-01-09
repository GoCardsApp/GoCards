package pl.gocards.ui.settings.model.deck

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pl.gocards.room.entity.deck.DeckConfig
import pl.gocards.db.room.AppDatabase
import pl.gocards.db.room.DeckDatabase
import pl.gocards.ui.settings.model.SettingModel
import pl.gocards.ui.settings.model.all_decks.MaxForgottenCardsModel

/**
 * S_U_03 This deck: Limit forgotten cards
 * @author Grzegorz Ziemski
 */
class DeckMaxForgottenCardsModel(
    private val appDb: AppDatabase,
    private val deckDb: DeckDatabase,
    application: Application
) : SettingModel(application) {

    lateinit var appMaxForgottenCards: MaxForgottenCardsModel

    val maxForgottenCards: MutableLiveData<String> = MutableLiveData(DeckConfig.MAX_ALLOWED_FORGOTTEN_CARDS_DEFAULT.toString())
    private var maxForgottenCardsDb: Int = DeckConfig.MAX_ALLOWED_FORGOTTEN_CARDS_DEFAULT

    @SuppressLint("CheckResult")
    fun init() {
        viewModelScope.launch(Dispatchers.IO) {
            val deckConfig = deckDb.deckConfigKtxDao().getByKey(DeckConfig.MAX_ALLOWED_FORGOTTEN_CARDS)
            if (deckConfig == null) {
                initApp()
            } else {
                set(deckConfig.value)
                maxForgottenCardsDb = deckConfig.value.toInt()
            }
        }
    }

    @SuppressLint("CheckResult")
    private fun initApp() {
        viewModelScope.launch(Dispatchers.IO) {
            val appConfig = appDb.appConfigKtxDao().getByKey(DeckConfig.MAX_ALLOWED_FORGOTTEN_CARDS)
            if (appConfig == null) {
                initDefault()
            } else {
                set(appConfig.value)
                maxForgottenCardsDb = appConfig.value.toInt()
            }
        }
    }

    private fun initDefault() {
        set(DeckConfig.MAX_ALLOWED_FORGOTTEN_CARDS_DEFAULT)
        maxForgottenCardsDb = DeckConfig.MAX_ALLOWED_FORGOTTEN_CARDS_DEFAULT
    }

    fun set(newValue: Any) {
        set(maxForgottenCards, newValue, "0")
    }

    fun reset() {
        maxForgottenCards.value = maxForgottenCardsDb.toString()
    }

    fun commit() {
        maxForgottenCards.value?.let {
            if (it.isNotEmpty()) {
                viewModelScope.launch(Dispatchers.IO) {
                    maxForgottenCardsDb = it.toInt()
                    deckDb.deckConfigKtxDao().update(
                        DeckConfig.MAX_ALLOWED_FORGOTTEN_CARDS,
                        it,
                        appMaxForgottenCards.appMaxForgottenCardsDb.toString()
                    )
                }
            }
        }
    }
}