package com.kotoba.app.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kotoba.app.audio.NativeAudioPlayer
import com.kotoba.app.data.db.KotobaDatabase
import com.kotoba.app.fsrs.FsrsEngine
import com.kotoba.app.updater.GitHubRelease
import com.kotoba.app.updater.GitHubUpdater
import com.kotoba.app.updater.UpdateState
import kotlinx.coroutines.launch

sealed class NavigationTab(val route: String, val label: String, val icon: ImageVector) {
    data object Today : NavigationTab("today", "วันนี้", Icons.Default.Today)
    data object Words : NavigationTab("words", "คำศัพท์", Icons.Default.MenuBook)
    data object Grammar : NavigationTab("grammar", "ไวยากรณ์", Icons.Default.AutoAwesome)
    data object Stats : NavigationTab("stats", "สถิติ", Icons.Default.BarChart)
    data object Settings : NavigationTab("settings", "ตั้งค่า", Icons.Default.Settings)
}

@Composable
fun MainScreen(
    database: KotobaDatabase,
    audioPlayer: NativeAudioPlayer,
    updater: GitHubUpdater
) {
    val coroutineScope = rememberCoroutineScope()
    val fsrsEngine = remember { FsrsEngine() }

    // Database states
    val wordsList by database.wordDao().getAllWords().collectAsState(initial = emptyList())
    val grammarList by database.grammarDao().getAllGrammar().collectAsState(initial = emptyList())

    // Active Tab & Sub-screens
    var currentTab by remember { mutableStateOf<NavigationTab>(NavigationTab.Today) }
    var selectedWordId by remember { mutableStateOf<Int?>(null) }
    var selectedGrammarId by remember { mutableStateOf<String?>(null) }
    var isInSession by remember { mutableStateOf(false) }

    // In-App Updater state
    val updateState by updater.updateState.collectAsState()
    var showUpdateDialog by remember { mutableStateOf(false) }

    val dueWords = remember(wordsList) {
        val now = System.currentTimeMillis()
        wordsList.filter { it.state != "new" && it.nextReview <= now && !it.isSuspended }
    }
    val newWords = remember(wordsList) {
        wordsList.filter { it.state == "new" && !it.isSuspended }.take(10)
    }

    // Handle back button / sub-screen navigation
    if (isInSession) {
        val sessionWords = remember { (dueWords + newWords).shuffled() }
        FlashcardSessionScreen(
            words = sessionWords,
            onRateWord = { word, rating ->
                coroutineScope.launch {
                    val updated = fsrsEngine.review(word, rating)
                    database.wordDao().updateWord(updated)
                }
            },
            onPlayAudio = { audioPlayer.speak(it) },
            onFinishSession = { isInSession = false }
        )
        return
    }

    if (selectedWordId != null) {
        val word = wordsList.find { it.id == selectedWordId }
        if (word != null) {
            WordDetailScreen(
                word = word,
                onBack = { selectedWordId = null },
                onPlayAudio = { audioPlayer.speak(it) }
            )
            return
        }
    }

    if (selectedGrammarId != null) {
        val grammar = grammarList.find { it.id == selectedGrammarId }
        if (grammar != null) {
            GrammarDetailScreen(
                grammar = grammar,
                onBack = { selectedGrammarId = null },
                onPlayAudio = { audioPlayer.speak(it) },
                onToggleMastered = { isMastered ->
                    coroutineScope.launch {
                        val updated = grammar.copy(isMastered = isMastered)
                        database.grammarDao().updateGrammar(updated)
                    }
                }
            )
            return
        }
    }

    // Main 5-tab layout
    Scaffold(
        bottomBar = {
            NavigationBar(
                tonalElevation = 8.dp
            ) {
                listOf(
                    NavigationTab.Today,
                    NavigationTab.Words,
                    NavigationTab.Grammar,
                    NavigationTab.Stats,
                    NavigationTab.Settings
                ).forEach { tab ->
                    NavigationBarItem(
                        selected = currentTab == tab,
                        onClick = { currentTab = tab },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label, fontSize = 11.sp) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                NavigationTab.Today -> TodayScreen(
                    dueCount = dueWords.size,
                    newCount = newWords.size,
                    streakDays = 12,
                    doneToday = 18,
                    onStartSession = { isInSession = true },
                    onNavigateGrammar = { currentTab = NavigationTab.Grammar }
                )

                NavigationTab.Words -> WordsScreen(
                    words = wordsList,
                    onSelectWord = { id -> selectedWordId = id }
                )

                NavigationTab.Grammar -> GrammarScreen(
                    grammarList = grammarList,
                    onSelectGrammar = { id -> selectedGrammarId = id }
                )

                NavigationTab.Stats -> StatsScreen(
                    words = wordsList
                )

                NavigationTab.Settings -> SettingsScreen(
                    currentVersion = "v1.0.0",
                    onCheckUpdate = {
                        showUpdateDialog = true
                        updater.checkForUpdate()
                    }
                )
            }

            // Updater Dialog
            if (showUpdateDialog && updateState !is UpdateState.Idle) {
                InAppUpdaterDialog(
                    updateState = updateState,
                    onDismiss = {
                        showUpdateDialog = false
                        updater.resetState()
                    },
                    onConfirmDownload = { release: GitHubRelease ->
                        val apkUrl = release.getApkDownloadUrl()
                        if (apkUrl != null) {
                            updater.downloadAndInstallApk(apkUrl, release.tagName)
                        }
                    }
                )
            }
        }
    }
}
