package pl.gocards.ui.cards.list

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import pl.gocards.App
import pl.gocards.db.app.AppDbUtil
import pl.gocards.ui.cards.list.edge_bar.filesync.FileSyncEdgeBarListCardsAdapter
import pl.gocards.ui.cards.list.edge_bar.filesync.FileSyncEdgeBarViewModel
import pl.gocards.ui.cards.list.edge_bar.learning_progress.LearningProgressViewModel
import pl.gocards.ui.cards.list.filesync.FileSyncListCardsAdapter
import pl.gocards.ui.cards.list.search.SearchListCardsViewModel
import pl.gocards.ui.cards.list.search.SearchListCardsViewModelFactory
import pl.gocards.ui.cards.list.select.SelectCardsViewModel
import pl.gocards.ui.cards.list.select.SelectCardsViewModelFactory
import pl.gocards.ui.cards.slider.EditCardSliderActivity
import pl.gocards.ui.discover.premium.PremiumViewModel
import pl.gocards.ui.filesync.FileSyncViewModel
import pl.gocards.ui.filesync_pro.AutoSyncViewModel
import pl.gocards.ui.theme.AppTheme
import pl.gocards.ui.theme.ExtendedColors
import pl.gocards.ui.theme.ExtendedTheme
import pl.gocards.ui.settings.SettingsActivity


/**
 * C_R_01 Display all cards
 * @author Grzegorz Ziemski
 */
@Immutable
class ListCardsActivity : AppCompatActivity() {

    companion object {
        const val DECK_DB_PATH = "DECK_DB_PATH"
    }

    lateinit var deckDbPath: String
        private set

    var adapter: FileSyncEdgeBarListCardsAdapter? = null
        private set

    lateinit var viewModel: SearchListCardsViewModel
        private set

     var fileSyncViewModel: FileSyncViewModel? = null
        private set

    private var autoSyncCardsModel: AutoSyncViewModel? = null

    lateinit var premiumViewModel: PremiumViewModel
        private set


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        deckDbPath = intent.getStringExtra(DECK_DB_PATH) ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            enableEdgeToEdge()
        }

        /**
         * It needs to be an AppCompatActivity instead of a ComponentActivity,
         * because dark mode does not work with the XML adapter
         */
        supportActionBar?.hide()

        val application = applicationContext as App
        val owner = this

        val searchListCardsViewModelFactory = SearchListCardsViewModelFactory(deckDbPath, application)
        viewModel = ViewModelProvider(this, searchListCardsViewModelFactory)[SearchListCardsViewModel::class.java]

        val selectViewModelFactory = SelectCardsViewModelFactory(application)
        val selectViewModel = ViewModelProvider(this, selectViewModelFactory)[SelectCardsViewModel::class.java]

        fileSyncViewModel = FileSyncViewModel.getInstance(owner, application)

        autoSyncCardsModel = AutoSyncViewModel.getInstance(deckDbPath, owner, application)
        autoSyncCardsModel?.autoSync {
            adapter?.loadCards()
        }

        premiumViewModel = PremiumViewModel(
            appDb = AppDbUtil.getInstance(application).getDatabase(application),
            application = application
        )

        setContent {
            AppTheme(isDarkTheme = application.getDarkMode()) {
                adapter = createAdapter(
                    this,
                    viewModel,
                    selectViewModel,
                    deckDbPath,
                    ExtendedTheme.colors,
                    fileSyncViewModel?.inProgress(deckDbPath),
                    remember { SnackbarHostState() }
                )

                LaunchedEffect(true) {
                    if (viewModel.items.size == 0) {
                        adapter?.loadCards()
                    }
                }

                CreateView()
            }
        }

        this.onBackPressedDispatcher.addCallback(this) {
            this@ListCardsActivity.handleOnBackPressed()
        }
    }

    @Composable
    private fun CreateView() {
        ListCardsScaffold(
            ListCardsScaffoldInputFactory().getInstance(this)
        )
    }

    fun handleOnBackPressed() {
        if (viewModel.isSearchActive().value) {
            adapter?.clearSearch()
        } else {
            super.finish()
        }
    }

    override fun onResume() {
        super.onResume()
        adapter?.loadCards()
    }

    override fun onStop() {
        super.onStop()
        autoSyncCardsModel?.autoSync { }
    }

    private fun createAdapter(
        activity: ListCardsActivity,
        viewModel: SearchListCardsViewModel,
        selectCardsViewModel: SelectCardsViewModel,
        deckDbPath: String,
        colors: ExtendedColors,
        isSyncProgress: LiveData<Boolean>?,
        snackbarHostState: SnackbarHostState
    ): FileSyncEdgeBarListCardsAdapter {

        val learningProgressViewModel = LearningProgressViewModel.getInstance(activity, deckDbPath)
        val fileSyncEdgeBarViewModel = FileSyncEdgeBarViewModel.getInstance(activity, deckDbPath)

        return FileSyncListCardsAdapter(
            activity = activity,
            viewModel = viewModel,
            selectViewModel = selectCardsViewModel,
            learningProgressViewModel = learningProgressViewModel,
            fileSyncEdgeBarViewModel = fileSyncEdgeBarViewModel,
            isSyncProgress = isSyncProgress ?: MutableLiveData(false),
            colors = colors,
            owner = activity,
            snackbarHostState = snackbarHostState
        )
    }

    /**
     * C_C_24 Edit the card
     */
    fun startEditCardActivity(editCardId: Int) {
        val intent = Intent(this, EditCardSliderActivity::class.java)
        intent.putExtra(EditCardSliderActivity.DECK_DB_PATH, deckDbPath)
        intent.putExtra(EditCardSliderActivity.EDIT_CARD_ID, editCardId)
        this.startActivity(intent)
    }

    /**
     * C_R_07 Add a new card here
     */
    fun startNewCardActivity(afterCardId: Int) {
        val intent = Intent(this, EditCardSliderActivity::class.java)
        intent.putExtra(EditCardSliderActivity.DECK_DB_PATH, deckDbPath)
        intent.putExtra(EditCardSliderActivity.NEW_CARD_AFTER_CARD_ID, afterCardId)
        this.startActivity(intent)
    }

    /**
     * C_C_23 Create a new card
     */
    fun startNewCardActivity() {
        val intent = Intent(this, EditCardSliderActivity::class.java)
        intent.putExtra(EditCardSliderActivity.DECK_DB_PATH, deckDbPath)
        intent.putExtra(EditCardSliderActivity.ADD_NEW_CARD, true)
        this.startActivity(intent)
    }

    /**
     * S_R_01 Settings
     */
    fun startDeckSettingsActivity() {
        val intent = Intent(this, SettingsActivity::class.java)
        intent.putExtra(DECK_DB_PATH, deckDbPath)
        startActivity(intent)
    }
}