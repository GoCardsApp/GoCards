package pl.gocards.ui.home.view

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import pl.gocards.R
import pl.gocards.ui.decks.all.view.AllDecksInput
import pl.gocards.ui.decks.all.view.ListAllDecksPage
import pl.gocards.ui.decks.all.view.ListAllDecksTopBar
import pl.gocards.ui.decks.decks.view.DeckBottomMenu
import pl.gocards.ui.decks.decks.view.DeckBottomMenuInput
import pl.gocards.ui.decks.recent.view.ListRecentDecksPage
import pl.gocards.ui.decks.recent.view.ListRecentDecksPageData
import pl.gocards.ui.decks.recent.view.ListRecentDecksTopBar
import pl.gocards.ui.discover.DiscoverInput
import pl.gocards.ui.discover.DiscoverPage
import pl.gocards.ui.discover.EmptyDecksTopBar
import pl.gocards.ui.explore.ExploreInput
import pl.gocards.ui.explore.ExploreTopBar
import pl.gocards.ui.explore.SignInPage
import pl.gocards.ui.explore.underconstruction.UnderConstructionPage

/**
 * @author Grzegorz Ziemski
 */
@Composable
fun HomeView(
    input: HomeInput,
    setCurrentPage: (Int) -> Unit
) {

    LaunchedEffect(input.pagerState.currentPage) {
        snapshotFlow { input.pagerState.currentPage }
            .collect {
                setCurrentPage(input.pagerState.currentPage)
            }
    }

    Scaffold(
        topBar = {
            when (input.pagerState.currentPage) {
                HomePage.Recent.ordinal -> {
                    ListRecentDecksTopBar(
                        isDarkTheme = input.isDarkTheme,
                        onBack = input.recentDecks.onBack,
                        menu = input.recentDecks.menu,
                    )
                }
                HomePage.Decks.ordinal -> {
                    ListAllDecksTopBar(
                        isDarkTheme = input.isDarkTheme,
                        onBack = input.allDecks.onBack,
                        folderName = input.allDecks.folderName,
                        search = input.allDecks.searchBarInput,
                        menu = input.allDecks.menu
                    )
                }
                HomePage.Explore.ordinal -> {
                    ExploreTopBar(
                        isDarkTheme = input.isDarkTheme,
                        onBack = input.allDecks.onBack,
                        menu = input.explore.menu,
                        isAuth = input.explore.token.value != null
                    )
                }
                else -> {
                    EmptyDecksTopBar(
                        isDarkTheme = input.isDarkTheme,
                        onBack = input.allDecks.onBack,
                    )
                }
            }
        },
        content = { innerPadding ->
            HomePager(
                input.pagerState,
                innerPadding,
                input.recentDecks.page,
                input.allDecks,
                input.explore,
                input.discover,
                input.deckBottomMenu
            )
        },
        bottomBar = {
            BottomNavigation(input.pagerState)
        }
    )
}

@Composable
private fun HomePager(
    pagerState: PagerState,
    innerPadding: PaddingValues,
    recentDecks: ListRecentDecksPageData,
    allDecks: AllDecksInput,
    explore: ExploreInput,
    discover: DiscoverInput,
    deckBottomMenu: DeckBottomMenuInput
) {
    val userScrollEnabled = (pagerState.settledPage == HomePage.Recent.ordinal && recentDecks.isEmptyFolder)
            || (pagerState.settledPage == HomePage.Decks.ordinal && allDecks.page.isEmptyFolder)
            || (pagerState.settledPage == HomePage.Explore.ordinal)
            || (pagerState.settledPage == HomePage.Discover.ordinal)

    HorizontalPager(
        state = pagerState,
        userScrollEnabled = userScrollEnabled,
        flingBehavior = PagerDefaults.flingBehavior(
            state = pagerState,
            snapAnimationSpec = spring(stiffness = Spring.StiffnessHigh),
        )
    ) {
        when (it) {
            HomePage.Recent.ordinal -> {
                ListRecentDecksPage(innerPadding, recentDecks)
            }
            HomePage.Decks.ordinal -> {
                ListAllDecksPage(innerPadding, allDecks.page)
            }
            HomePage.Explore.ordinal -> {
                if (explore.underConstruction.underConstruction) {
                    UnderConstructionPage(innerPadding, explore.underConstruction)
                } else {
                    SignInPage(innerPadding, explore.token.value, explore.onClickLogin)
                }
            }
            HomePage.Discover.ordinal -> {
                DiscoverPage(innerPadding, discover)
            }
        }
    }
    DeckBottomMenu(deckBottomMenu)
}

@Composable
private fun BottomNavigation(pagerState: PagerState) {
    val scope = rememberCoroutineScope()
    NavigationBar(
        //containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
    ) {
        NavigationBarItem(
            icon = {
                Icon(
                    Icons.Filled.AccessTime,
                    contentDescription = stringResource(R.string.bottom_nav_menu_bottom_recent)
                )
            },
            label = { Text(stringResource(R.string.bottom_nav_menu_bottom_recent)) },
            selected = pagerState.currentPage == HomePage.Recent.ordinal,
            onClick = {
                scope.launch {
                    pagerState.animateScrollToPage(HomePage.Recent.ordinal)
                }
            }
        )
        NavigationBarItem(
            icon = {
                Icon(
                    Icons.Filled.Dashboard,
                    contentDescription = stringResource(R.string.bottom_nav_menu_all_decks)
                )
            },
            label = { Text(stringResource(R.string.bottom_nav_menu_all_decks)) },
            selected = pagerState.currentPage == HomePage.Decks.ordinal,
            onClick = {
                scope.launch {
                    pagerState.animateScrollToPage(HomePage.Decks.ordinal)
                }
            }
        )
        NavigationBarItem(
            icon = {
                Icon(
                    Icons.Filled.Public,
                    contentDescription = stringResource(R.string.bottom_nav_menu_explore)
                )
            },
            label = { Text(stringResource(R.string.bottom_nav_menu_explore)) },
            selected = pagerState.currentPage == HomePage.Explore.ordinal,
            onClick = {
                scope.launch {
                    pagerState.animateScrollToPage(HomePage.Explore.ordinal)
                }
            }
        )
        NavigationBarItem(
            icon = {
                Icon(
                    Icons.Filled.Newspaper,
                    contentDescription = stringResource(R.string.bottom_nav_menu_discover)
                )
            },
            label = { Text(stringResource(R.string.bottom_nav_menu_discover)) },
            selected = pagerState.currentPage == HomePage.Discover.ordinal,
            onClick = {
                scope.launch {
                    pagerState.animateScrollToPage(HomePage.Discover.ordinal)
                }
            }
        )
    }
}

enum class HomePage(val page: Int) {
    Recent(0), Decks(1), Explore(2), Discover(3);
}