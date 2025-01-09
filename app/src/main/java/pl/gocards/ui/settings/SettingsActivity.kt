package pl.gocards.ui.settings


import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pl.gocards.App
import pl.gocards.R
import pl.gocards.db.app.AppDbUtil
import pl.gocards.db.deck.AppDeckDbUtil
import pl.gocards.room.entity.app.AppConfig
import pl.gocards.room.entity.deck.DeckConfig
import pl.gocards.ui.theme.AppBar
import pl.gocards.ui.theme.AppTheme
import pl.gocards.ui.theme.Grey900
import pl.gocards.ui.settings.dialog.AutoSyncAlertDialog
import pl.gocards.ui.settings.dialog.AutoSyncDialogEntity
import pl.gocards.ui.settings.dialog.DarkModeAlertDialog
import pl.gocards.ui.settings.dialog.DarkModeAlertDialogEntity
import pl.gocards.ui.settings.dialog.EdgeBarAlertDialog
import pl.gocards.ui.settings.dialog.EdgeBarAlertDialogEntity
import pl.gocards.ui.settings.dialog.LimitForgottenCardsDialog
import pl.gocards.ui.settings.dialog.LimitForgottenCardsDialogEntity
import pl.gocards.ui.settings.dialog.MaxLinesDialog
import pl.gocards.ui.settings.dialog.MaxLinesDialogEntity
import pl.gocards.ui.settings.dialog.getShowEdgeBarLabel
import pl.gocards.ui.settings.model.SettingsViewModel

@Preview(showBackground = true)
@Composable
fun PreviewSettings() {
    SettingsScaffold(
        tabIndex = remember { mutableIntStateOf(0) },

        Deck_AutoSyncDialog_IsShown = remember { mutableStateOf(false) },
        Deck_LimitForgottenCardsDialog_IsShown = remember { mutableStateOf(false) },
        App_LimitForgottenCardsDialog_IsShown = remember { mutableStateOf(false) },
        Deck_MaxLinesDialog_IsShown = remember { mutableStateOf(false) },
        App_MaxLinesDialog_IsShown = remember { mutableStateOf(false) },
        App_LeftEdgeBarDialog_IsShown = remember { mutableStateOf(false) },
        App_RightEdgeBarDialog_IsShown = remember { mutableStateOf(false) },

        Deck_AutoSyncDialog_Entity = AutoSyncDialogEntity(
            autoSync = remember { mutableStateOf(true) },
            autoSyncDb = remember { mutableStateOf(true) },
            fileNameDb = remember { mutableStateOf("Example File Name") },
        ),
        Deck_LimitForgottenCardsDialog_Entity = LimitForgottenCardsDialogEntity(
            maxForgottenCards = remember { mutableStateOf(DeckConfig.MAX_ALLOWED_FORGOTTEN_CARDS_DEFAULT.toString()) },
        ),
        App_LimitForgottenCardsDialog_Entity = LimitForgottenCardsDialogEntity(
            maxForgottenCards = remember { mutableStateOf(DeckConfig.MAX_ALLOWED_FORGOTTEN_CARDS_DEFAULT.toString()) },
        ),
        Deck_MaxLinesDialog_Entity = MaxLinesDialogEntity(
            maxLines = remember { mutableStateOf(DeckConfig.MAX_LINES_DEFAULT.toString()) },
        ),
        App_MaxLinesDialog_Entity = MaxLinesDialogEntity(
            maxLines = remember { mutableStateOf(DeckConfig.MAX_LINES_DEFAULT.toString()) },
        ),
        App_LeftEdgeBarDialog_Entity = EdgeBarAlertDialogEntity(
            edgeBar = remember { mutableStateOf(AppConfig.LEFT_EDGE_BAR_DEFAULT) },
        ),
        App_RightEdgeBarDialog_Entity = EdgeBarAlertDialogEntity(
            edgeBar = remember { mutableStateOf(AppConfig.RIGHT_EDGE_BAR_DEFAULT) },
        ),

        DarkModeDialog_IsShown = remember { mutableStateOf(false) },
        DarkModeDialog_Entity = DarkModeAlertDialogEntity(
            darkMode = remember { mutableStateOf(AppConfig.DARK_MODE_OPTIONS[1]) },
            darkModeDb = remember { mutableStateOf(AppConfig.DARK_MODE_OPTIONS[1]) },
        ),

        isDarkTheme = remember { mutableStateOf(false) },
        preview = true
    )
}

/**
 * S_R_01 Settings
 * @author Grzegorz Ziemski
 */
class SettingsActivity : ComponentActivity() {
    companion object {
        const val DECK_DB_PATH = "DECK_DB_PATH"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val dbPath = intent.getStringExtra(DECK_DB_PATH)
            val context = LocalContext.current
            val application = LocalContext.current.applicationContext as App
            val darkMode = application.getDarkMode() ?: isSystemInDarkTheme()

            DeckSettings(
                onBack = { super.finish() },
                viewModel = SettingsViewModel(
                    AppDbUtil.getInstance(context).getDatabase(context),
                    dbPath?.let {  AppDeckDbUtil.getInstance(context).getDatabase(context, dbPath) },
                    darkMode,
                    application
                ),
                darkMode
            )
        }
    }
}

@Composable
fun DeckSettings(
    onBack: () -> Unit = {},
    viewModel: SettingsViewModel,
    isDarkTheme: Boolean
) {
    val tabIndex = remember { mutableIntStateOf(0) }
    val isShownDeckAutoSyncDialog = remember { mutableStateOf(false) }
    val isShownDeckLimitLearningCardsDialog = remember { mutableStateOf(false) }
    val isShownAppLimitLearningCardsDialog = remember { mutableStateOf(false) }
    val isShownDeckMaxLinesDialog = remember { mutableStateOf(false) }
    val isShownAppMaxLinesDialog = remember { mutableStateOf(false) }
    val isShownAppLeftEdgeBarDialog = remember { mutableStateOf(false) }
    val isShownAppRightEdgeBarDialog = remember { mutableStateOf(false) }
    val showDarkModeDialog = remember { mutableStateOf(false) }

    SettingsScaffold(

        // Current values
        tabIndex = tabIndex,
        Tab_OnClick = { tabIndex.intValue = it },

        // Fields
        Deck_AutoSyncField_OnClick = { isShownDeckAutoSyncDialog.value = true },
        Deck_LimitForgottenCardsField_OnClick = { isShownDeckLimitLearningCardsDialog.value = true },
        App_LimitForgottenCardsField_OnClick = { isShownAppLimitLearningCardsDialog.value = true },
        Deck_MaxLinesField_OnClick = { isShownDeckMaxLinesDialog.value = true },
        App_MaxLinesField_OnClick = { isShownAppMaxLinesDialog.value = true },
        App_LeftEdgeBarField_OnClick = { isShownAppLeftEdgeBarDialog.value = true },
        App_RightEdgeBarField_OnClick = { isShownAppRightEdgeBarDialog.value = true },

        // Dialogs
        Deck_AutoSyncDialog_IsShown = isShownDeckAutoSyncDialog,
        Deck_AutoSyncDialog_Entity = viewModel.autoSync?.autoSync?.let {
            AutoSyncDialogEntity(
                autoSync = viewModel.autoSync!!.autoSync.observeAsState(initial = false),
                autoSyncDb = viewModel.autoSync!!.autoSyncDb.observeAsState(initial = false),
                fileNameDb = viewModel.autoSync!!.deckFileNameDb.observeAsState(initial = ""),
                onSelectRadio = {
                    viewModel.autoSync!!.set(it)
                },
                onSave = {
                    isShownDeckAutoSyncDialog.value = false
                    viewModel.autoSync!!.commit()
                },
                onDismiss = {
                    isShownDeckAutoSyncDialog.value = false
                    viewModel.autoSync!!.reset()
                },
            )
        },

        Deck_LimitForgottenCardsDialog_IsShown = isShownDeckLimitLearningCardsDialog,
        Deck_LimitForgottenCardsDialog_Entity = viewModel.deckMaxForgottenCards?.let {
            LimitForgottenCardsDialogEntity(
                maxForgottenCards = viewModel.deckMaxForgottenCards!!.maxForgottenCards.observeAsState(
                    initial = ""
                ),
                onValueChange = {
                    viewModel.deckMaxForgottenCards!!.set(it)
                },
                onSave = {
                    isShownDeckLimitLearningCardsDialog.value = false
                    viewModel.deckMaxForgottenCards!!.commit()
                },
                onDismiss = {
                    isShownDeckLimitLearningCardsDialog.value = false
                    viewModel.deckMaxForgottenCards!!.reset()
                },
            )
        },

        App_LimitForgottenCardsDialog_IsShown = isShownAppLimitLearningCardsDialog,
        App_LimitForgottenCardsDialog_Entity = LimitForgottenCardsDialogEntity(
            maxForgottenCards = viewModel.appMaxForgottenCards.maxForgottenCards.observeAsState(initial = ""),
            onValueChange = {
                viewModel.appMaxForgottenCards.set(it)
            },
            onSave = {
                isShownAppLimitLearningCardsDialog.value = false
                viewModel.appMaxForgottenCards.commit()
            },
            onDismiss = {
                isShownAppLimitLearningCardsDialog.value = false
                viewModel.appMaxForgottenCards.reset()
            },
        ),

        Deck_MaxLinesDialog_IsShown = isShownDeckMaxLinesDialog,
        Deck_MaxLinesDialog_Entity = viewModel.deckMaxLines?.let {
            MaxLinesDialogEntity(
                maxLines = viewModel.deckMaxLines!!.maxLines.observeAsState(initial = "0"),
                onValueChange = {
                    viewModel.deckMaxLines!!.set(it)
                },
                onSave = {
                    isShownDeckMaxLinesDialog.value = false
                    viewModel.deckMaxLines!!.commit()
                },
                onDismiss = {
                    isShownDeckMaxLinesDialog.value = false
                    viewModel.deckMaxLines!!.reset()
                },
            )
        },

        App_MaxLinesDialog_IsShown = isShownAppMaxLinesDialog,
        App_MaxLinesDialog_Entity = MaxLinesDialogEntity(
            maxLines = viewModel.appMaxLines.maxLines.observeAsState(initial = "0"),
            onValueChange = {
                viewModel.appMaxLines.set(it)
            },
            onSave = {
                isShownAppMaxLinesDialog.value = false
                viewModel.appMaxLines.commit()
            },
            onDismiss = {
                isShownAppMaxLinesDialog.value = false
                viewModel.appMaxLines.reset()
            },
        ),


        App_LeftEdgeBarDialog_IsShown = isShownAppLeftEdgeBarDialog,
        App_LeftEdgeBarDialog_Entity = EdgeBarAlertDialogEntity(
            edgeBar = viewModel.showLeftEdgeBar.leftEdgeBar.observeAsState(initial = ""),
            onSelectRadio = {
                viewModel.showLeftEdgeBar.set(it as String)
            },
            onSave = {
                isShownAppLeftEdgeBarDialog.value = false
                viewModel.showLeftEdgeBar.commit()
            },
            onDismiss = {
                isShownAppLeftEdgeBarDialog.value = false
                viewModel.showLeftEdgeBar.reset()
            }
        ),

        App_RightEdgeBarDialog_IsShown = isShownAppRightEdgeBarDialog,
        App_RightEdgeBarDialog_Entity = EdgeBarAlertDialogEntity(
            edgeBar = viewModel.showRightEdgeBar.rightEdgeBar.observeAsState(initial = ""),
            onSelectRadio = {
                viewModel.showRightEdgeBar.set(it as String)
            },
            onSave = {
                isShownAppRightEdgeBarDialog.value = false
                viewModel.showRightEdgeBar.commit()
            },
            onDismiss = {
                isShownAppRightEdgeBarDialog.value = false
                viewModel.showRightEdgeBar.reset()
            }
        ),

        DarkModeField_OnClick = { showDarkModeDialog.value = true },
        DarkModeDialog_IsShown = showDarkModeDialog,
        DarkModeDialog_Entity = DarkModeAlertDialogEntity(
            darkMode = viewModel.darkMode.darkMode.observeAsState(initial = AppConfig.DARK_MODE_DEFAULT),
            darkModeDb = viewModel.darkMode.darkModeDb.observeAsState(initial = AppConfig.DARK_MODE_DEFAULT),
            onSelect = { viewModel.darkMode.set(it) },
            onSave = {
                showDarkModeDialog.value = false
                viewModel.darkMode.commit()
            },
            onDismiss = {
                showDarkModeDialog.value = false
                viewModel.darkMode.reset()
            },
        ),

        onBack = onBack,
        isDarkTheme = viewModel.darkMode.isDarkTheme.observeAsState(initial = isDarkTheme),
        preview = false
    )
}


@Composable
@Suppress("LocalVariableName")
@SuppressLint("PrivateResource")
@SuppressWarnings("unused")
@OptIn(ExperimentalMaterial3Api::class)
fun SettingsScaffold(
    tabIndex: State<Int>,
    Tab_OnClick: (Int) -> Unit = {},

    Deck_AutoSyncField_OnClick: () -> Unit = {},
    Deck_AutoSyncDialog_IsShown: State<Boolean>,
    Deck_AutoSyncDialog_Entity: AutoSyncDialogEntity?,

    Deck_LimitForgottenCardsField_OnClick: () -> Unit = {},
    Deck_LimitForgottenCardsDialog_IsShown: State<Boolean>,
    Deck_LimitForgottenCardsDialog_Entity: LimitForgottenCardsDialogEntity?,

    App_LimitForgottenCardsField_OnClick: () -> Unit = {},
    App_LimitForgottenCardsDialog_IsShown: State<Boolean>,
    App_LimitForgottenCardsDialog_Entity: LimitForgottenCardsDialogEntity,

    Deck_MaxLinesField_OnClick: () -> Unit = {},
    Deck_MaxLinesDialog_IsShown: State<Boolean>,
    Deck_MaxLinesDialog_Entity: MaxLinesDialogEntity?,

    App_MaxLinesField_OnClick: () -> Unit = {},
    App_MaxLinesDialog_IsShown: State<Boolean>,
    App_MaxLinesDialog_Entity: MaxLinesDialogEntity,

    App_LeftEdgeBarField_OnClick: () -> Unit = {},
    App_LeftEdgeBarDialog_IsShown: State<Boolean>,
    App_LeftEdgeBarDialog_Entity: EdgeBarAlertDialogEntity,

    App_RightEdgeBarField_OnClick: () -> Unit = {},
    App_RightEdgeBarDialog_IsShown: State<Boolean>,
    App_RightEdgeBarDialog_Entity: EdgeBarAlertDialogEntity,

    DarkModeField_OnClick: () -> Unit = {},
    DarkModeDialog_IsShown: State<Boolean>,
    DarkModeDialog_Entity: DarkModeAlertDialogEntity,

    onBack: () -> Unit = {},
    isDarkTheme: State<Boolean>,
    preview: Boolean
) {
    AppTheme(isDarkTheme = isDarkTheme.value, preview = preview) {

        if (Deck_AutoSyncDialog_IsShown.value && Deck_AutoSyncDialog_Entity != null) {
            AutoSyncAlertDialog(Deck_AutoSyncDialog_Entity)
        }
        if (Deck_LimitForgottenCardsDialog_IsShown.value && Deck_LimitForgottenCardsDialog_Entity != null) {
            LimitForgottenCardsDialog(Deck_LimitForgottenCardsDialog_Entity)
        }
        if (App_LimitForgottenCardsDialog_IsShown.value) {
            LimitForgottenCardsDialog(App_LimitForgottenCardsDialog_Entity)
        }
        if (Deck_MaxLinesDialog_IsShown.value && Deck_MaxLinesDialog_Entity != null) {
            MaxLinesDialog(Deck_MaxLinesDialog_Entity)
        }
        if (App_MaxLinesDialog_IsShown.value) {
            MaxLinesDialog(App_MaxLinesDialog_Entity)
        }
        if (App_LeftEdgeBarDialog_IsShown.value) {
            EdgeBarAlertDialog(App_LeftEdgeBarDialog_Entity.copy(title = stringResource(R.string.settings_edge_bar_show_left_edge_bar)))
        }
        if (App_RightEdgeBarDialog_IsShown.value) {
            EdgeBarAlertDialog(App_RightEdgeBarDialog_Entity.copy(title = stringResource(R.string.settings_edge_bar_show_right_edge_bar)))
        }
        if (DarkModeDialog_IsShown.value) {
            DarkModeAlertDialog(DarkModeDialog_Entity)
        }
        Scaffold(
            topBar = {
                AppBar(
                    modifier = Modifier,
                    isDarkTheme = isDarkTheme.value,
                    title = { Text(stringResource(R.string.settings_title)) },
                    onBack = onBack
                )
            },
            content = { innerPadding ->
                Column(
                    modifier = Modifier.padding(innerPadding),
                ) {
                    val titles = ArrayList<String>()
                    Deck_AutoSyncDialog_Entity?.let { titles += stringResource(R.string.settings_this_deck) }
                    titles += stringResource(R.string.settings_all_decks)
                    titles += stringResource(R.string.settings_app)
                    Column {
                        TabRow(
                            selectedTabIndex = tabIndex.value,
                            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp),
                        ) {
                            titles.forEachIndexed { index, title ->
                                Tab(
                                    selected = tabIndex.value == index,
                                    onClick = { Tab_OnClick(index) },
                                    text = {
                                        Text(
                                            text = title,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                )
                            }
                        }
                    }
                    if (titles[tabIndex.value] == stringResource(R.string.settings_this_deck)) {
                        // S_R_02 Settings: This deck
                        Column(Modifier.padding(15.dp, 15.dp, 0.dp, 0.dp)) {}
                        Deck_AutoSyncDialog_Entity?.let {
                            SettingField(
                                isDarkTheme = isDarkTheme.value,
                                title = stringResource(R.string.settings_auto_sync_title),
                                icon = R.drawable.ic_sharp_sync_24,
                                value = if (Deck_AutoSyncDialog_Entity.autoSyncDb.value)
                                    Deck_AutoSyncDialog_Entity.fileNameDb.value
                                        ?: stringResource(R.string.settings_auto_sync_no_file)
                                else stringResource(R.string.off),
                                onClick = Deck_AutoSyncField_OnClick
                            )
                        }
                        GroupSettings(stringResource(R.string.settings_studying_title))
                        Deck_LimitForgottenCardsDialog_Entity?.let {
                            val deckMaxForgottenCards =
                                Deck_LimitForgottenCardsDialog_Entity.maxForgottenCards
                            SettingField(
                                isDarkTheme = isDarkTheme.value,
                                title = stringResource(R.string.settings_limit_forgotten_cards_title),
                                icon = R.drawable.ic_round_123_24,
                                value = if (deckMaxForgottenCards.value.isEmpty() || deckMaxForgottenCards.value == "0")
                                    stringResource(R.string.off)
                                else deckMaxForgottenCards.value,
                                onClick = Deck_LimitForgottenCardsField_OnClick
                            )
                        }
                        GroupSettings(stringResource(R.string.settings_card_list_title))
                        Deck_MaxLinesDialog_Entity?.let {
                            SettingField(
                                isDarkTheme = isDarkTheme.value,
                                title = stringResource(R.string.settings_max_lines_title),
                                icon = R.drawable.ic_baseline_density_small_24,
                                value = Deck_MaxLinesDialog_Entity.maxLines.value,
                                onClick = Deck_MaxLinesField_OnClick
                            )
                        }
                    } else if (titles[tabIndex.value] == stringResource(R.string.settings_all_decks)) {
                        // S_R_03 Settings: All decks
                        GroupSettings(stringResource(R.string.settings_studying_title))
                        val appMaxForgottenCards = App_LimitForgottenCardsDialog_Entity.maxForgottenCards
                        SettingField(
                            isDarkTheme = isDarkTheme.value,
                            title = stringResource(R.string.settings_limit_forgotten_cards_title),
                            icon = R.drawable.ic_round_123_24,
                            value = if (appMaxForgottenCards.value.isEmpty() || appMaxForgottenCards.value == "0")
                                stringResource(R.string.off)
                            else appMaxForgottenCards.value,
                            onClick = App_LimitForgottenCardsField_OnClick
                        )
                        GroupSettings(stringResource(R.string.settings_card_list_title))
                        SettingField(
                            isDarkTheme = isDarkTheme.value,
                            title = stringResource(R.string.settings_max_lines_title),
                            icon = R.drawable.ic_baseline_density_small_24,
                            value = App_MaxLinesDialog_Entity.maxLines.value,
                            onClick = App_MaxLinesField_OnClick
                        )
                        SettingField(
                            isDarkTheme = isDarkTheme.value,
                            title = stringResource(R.string.settings_edge_bar_show_left_edge_bar),
                            icon = R.drawable.ic_round_align_horizontal_left_24,
                            value = getShowEdgeBarLabel(App_LeftEdgeBarDialog_Entity.edgeBar.value),
                            onClick = App_LeftEdgeBarField_OnClick
                        )
                        SettingField(
                            isDarkTheme = isDarkTheme.value,
                            title = stringResource(R.string.settings_edge_bar_show_right_edge_bar),
                            icon = R.drawable.ic_round_align_horizontal_right_24,
                            value = getShowEdgeBarLabel(App_RightEdgeBarDialog_Entity.edgeBar.value),
                            onClick = App_RightEdgeBarField_OnClick
                        )
                    } else {
                        // S_R_04 Settings: App
                        Column(Modifier.padding(15.dp, 15.dp, 0.dp, 0.dp)) {}
                        SettingField(
                            isDarkTheme = isDarkTheme.value,
                            title = stringResource(R.string.app_settings_dark_mode),
                            icon = R.drawable.ic_round_dark_mode_24,
                            value = DarkModeDialog_Entity.darkModeDb.value,
                            onClick = DarkModeField_OnClick
                        )
                    }
                }
            }
        )
    }
}

@Composable
private fun GroupSettings(title: String) {
    Column(Modifier.padding(15.dp, 30.dp, 0.dp, 0.dp)) {
        Text(
            title.uppercase(),
            style = MaterialTheme.typography.titleSmall.copy(
                color = Color.Gray
            )
        )
    }
}

@Composable
@SuppressWarnings("unused")
fun SettingField(
    isDarkTheme: Boolean,
    title: String,
    icon: Int,
    value: String,
    onClick: () -> Unit = {}
) {
    Surface(
        modifier = Modifier
            .clickable(onClick = { onClick() })
            .fillMaxWidth(),
    ) {
        Row(Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp)) {
            Column {
                Box(Modifier.padding(5.dp)) {
                    Box(
                        modifier = Modifier
                            .size(35.dp)
                            .clip(CircleShape)
                            .background(
                                if (isDarkTheme) Grey900
                                else MaterialTheme.colorScheme.primary
                            )
                    )
                    Image(
                        painter = painterResource(icon),
                        contentDescription = title,
                        colorFilter = ColorFilter.tint(Color.White),
                        modifier = Modifier
                            .size(35.dp)
                            .padding(7.dp)
                    )
                }
            }
            Column(Modifier.padding(15.dp, 3.dp, 0.dp, 0.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(text = value, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
    }
}