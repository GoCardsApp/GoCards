package pl.gocards.ui.home

import android.os.Build
import android.os.Bundle
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import pl.gocards.App
import pl.gocards.db.deck.AppDeckDbUtil
import pl.gocards.ui.auth.AuthLauncher
import pl.gocards.ui.cards.slider.StudyCardSliderActivity
import pl.gocards.ui.decks.all.AllDecksAdapterFactory
import pl.gocards.ui.decks.decks.model.ListDecksViewModel
import pl.gocards.ui.decks.decks.model.ListDecksViewModelFactory
import pl.gocards.ui.decks.folders.model.CutPasteFolderViewModel
import pl.gocards.ui.decks.folders.model.ListFoldersViewModel
import pl.gocards.ui.decks.recent.ListRecentDecksAdapter
import pl.gocards.ui.decks.recent.RecentDecksAdapterFactory
import pl.gocards.ui.decks.search.SearchFoldersDecksAdapter
import pl.gocards.ui.decks.search.SearchFoldersDecksViewModel
import pl.gocards.ui.decks.search.SearchFoldersDecksViewModelFactory
import pl.gocards.ui.discover.premium.BillingClient
import pl.gocards.ui.discover.premium.PremiumViewModel
import pl.gocards.ui.discover.review.InAppReviewClient
import pl.gocards.ui.discover.review.ReviewViewModel
import pl.gocards.ui.explore.underconstruction.PollViewModel
import pl.gocards.ui.filesync.FileSyncViewModel
import pl.gocards.ui.home.view.HomeInputFactory
import pl.gocards.ui.home.view.HomeView
import pl.gocards.ui.theme.AppTheme
import pl.gocards.ui.theme.ExtendedTheme
import pl.gocards.util.FirebaseAnalyticsHelper
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path


/**
 * @author Grzegorz Ziemski
 */
@Immutable
class HomeActivity : AppCompatActivity(), ActivityResultCallback<ActivityResult> {

    private var currentPage = 0

    var recentAdapter: ListRecentDecksAdapter? = null
        private set

    var allAdapter: SearchFoldersDecksAdapter? = null
        private set

    lateinit var premiumViewModel: PremiumViewModel
        private set

    lateinit var billingClient: BillingClient
        private set

    lateinit var reviewViewModel: ReviewViewModel
        private set

    private lateinit var analytics: FirebaseAnalyticsHelper

    lateinit var inAppReviewClient: InAppReviewClient
        private set

    var fileSyncViewModel: FileSyncViewModel? = null
        private set

    private lateinit var searchFoldersDecksViewModel: SearchFoldersDecksViewModel

    lateinit var exploreViewModel: PollViewModel
        private set

    private val startActivityForResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult(), this)

    var logInLauncher: AuthLauncher = AuthLauncher(this)
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            enableEdgeToEdge()
        }
        createDbRootFolderIfNotExists()

        /**
         * It needs to be an AppCompatActivity instead of a ComponentActivity,
         * because dark mode does not work with the XML adapter
         */
        supportActionBar?.hide()

        val application = this.applicationContext as App
        val owner = this


        premiumViewModel = PremiumViewModel.create(application)
        billingClient = createBillingClient(premiumViewModel)
        fileSyncViewModel = FileSyncViewModel.getInstance(owner, application)
        val listDecksViewModel = createListDecksViewModel()
        val listFoldersViewModel = ListFoldersViewModel(application)
        searchFoldersDecksViewModel = createSearchFoldersDecksViewModel()
        reviewViewModel = ReviewViewModel.create(application)
        analytics = FirebaseAnalyticsHelper.getInstance(application)
        inAppReviewClient = InAppReviewClient(this, application)
        exploreViewModel = PollViewModel.create(application)
        val cutPasteFolderViewModel = CutPasteFolderViewModel(
            listFoldersViewModel.currentFolder,
            application,
            owner
        )


        setContent {
            val isShownMoreDeckMenu: MutableState<Path?> = remember { mutableStateOf(null) }
            val isPremium = premiumViewModel.isPremium().value

            AppTheme {
                recentAdapter = createRecentAdapter(
                    isShownMoreDeckMenu,
                    isPremium
                )
                allAdapter = createAllDecksAdapter(
                    listDecksViewModel,
                    listFoldersViewModel,
                    cutPasteFolderViewModel,
                    isShownMoreDeckMenu,
                    isPremium
                )
                CreateView()
            }

            LaunchedEffect(isPremium) { initItems() }
        }

        this.onBackPressedDispatcher.addCallback(this) {
            this@HomeActivity.handleOnBackPressed()
        }
    }

    @Composable
    private fun CreateView() {
        HomeView(
            HomeInputFactory().create(this),
            setCurrentPage = { currentPage = it },
        )
    }

    private fun initItems() {
        if (searchFoldersDecksViewModel.isSearchActive.value) {
            val query = searchFoldersDecksViewModel.getSearchQuery().value
            if (query != null) {
                allAdapter?.searchItems(query)
            } else {
                loadItems()
            }
        } else {
            loadItems()
        }
    }

    private fun loadItems() {
        recentAdapter!!.loadItems()
        allAdapter!!.loadItems()
    }

    fun handleOnBackPressed() {
        if (currentPage == 1) {
            if (searchFoldersDecksViewModel.isSearchActive.value) {
                allAdapter?.clearSearch()
            } else if (allAdapter?.openFolderUp() == false) {
                super.finish()
            }
        } else {
            super.finish()
        }
    }

    @Override
    override fun onResume() {
        super.onResume()
        recentAdapter?.loadItems()
        allAdapter?.loadItems()
    }

    private fun createDbRootFolderIfNotExists() {
        try {
            Files.createDirectories(
                AppDeckDbUtil.getInstance(applicationContext)
                    .getDbFolder(applicationContext)
            )
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    @Composable
    private fun createRecentAdapter(
        isShownMoreDeckMenu: MutableState<Path?>,
        isPremium: Boolean
    ): ListRecentDecksAdapter {
        return RecentDecksAdapterFactory().create(
            isShownMoreDeckMenu,
            isPremium,
            { loadItems() },
            ExtendedTheme.colors,
            this.startActivityForResultLauncher,
            this,
            this.lifecycleScope,
            application as App
        )
    }

    @Composable
    private fun createAllDecksAdapter(
        listDecksViewModel: ListDecksViewModel,
        listFoldersViewModel: ListFoldersViewModel,
        cutPasteFolderViewModel: CutPasteFolderViewModel,
        isShownMoreDeckMenu: MutableState<Path?>,
        isPremium: Boolean
    ): SearchFoldersDecksAdapter {
        return AllDecksAdapterFactory().create(
            listDecksViewModel,
            listFoldersViewModel,
            cutPasteFolderViewModel,
            searchFoldersDecksViewModel,
            isShownMoreDeckMenu,
            isPremium,
            { loadItems() },
            ExtendedTheme.colors,
            this.startActivityForResultLauncher,
            this,
            this,
            application as App
        )
    }

    private fun createBillingClient(
        premiumViewModel: PremiumViewModel
    ): BillingClient {
        return BillingClient(
            premiumViewModel = premiumViewModel,
            context = this,
            scope = this.lifecycleScope
        )
    }

    private fun createListDecksViewModel(): ListDecksViewModel {
        return ViewModelProvider(
            this,
            ListDecksViewModelFactory(application)
        )[ListDecksViewModel::class.java]
    }

    private fun createSearchFoldersDecksViewModel(): SearchFoldersDecksViewModel {
        return ViewModelProvider(
            this,
            SearchFoldersDecksViewModelFactory(application)
        )[SearchFoldersDecksViewModel::class.java]
    }

    override fun onActivityResult(activityResult: ActivityResult) {
        val result = activityResult.data?.getStringExtra("RESULT")

        if (result == StudyCardSliderActivity.RESULT_NO_MORE_CARDS_TO_REPEAT) {
            if (reviewViewModel.studyCanReview.value) {
                inAppReviewClient.launch(
                    onSuccess = {
                        analytics.studyOpenReviewInApp()
                    }
                )
            }
        }
    }
}
