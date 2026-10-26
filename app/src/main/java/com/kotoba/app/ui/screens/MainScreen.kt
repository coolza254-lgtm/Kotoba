package com.kotoba.app.ui.screens

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kotoba.app.audio.NativeAudioPlayer
import com.kotoba.app.data.AppPreferences
import com.kotoba.app.data.db.KotobaDatabase
import com.kotoba.app.data.model.WordEntity
import com.kotoba.app.fsrs.FsrsEngine
import com.kotoba.app.ui.theme.AccentRed
import com.kotoba.app.updater.GitHubRelease
import com.kotoba.app.updater.GitHubUpdater
import com.kotoba.app.updater.UpdateState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

sealed class NavigationTab(val route: String, val label: String, val icon: ImageVector) {
    data object Today : NavigationTab("today", "วันนี้", Icons.Default.Today)
    data object Words : NavigationTab("words", "คำศัพท์", Icons.AutoMirrored.Filled.MenuBook)
    data object Grammar : NavigationTab("grammar", "ไวยากรณ์", Icons.Default.AutoAwesome)
    data object Stats : NavigationTab("stats", "สถิติ", Icons.Default.BarChart)
    data object Settings : NavigationTab("settings", "ตั้งค่า", Icons.Default.Settings)
}

@Composable
fun MainScreen(
    database: KotobaDatabase,
    preferences: AppPreferences,
    audioPlayer: NativeAudioPlayer,
    updater: GitHubUpdater
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Reactive Preferences
    val themeMode by preferences.themeMode.collectAsState()
    val doneToday by preferences.doneToday.collectAsState()
    val streakDays by preferences.streakDays.collectAsState()
    val dailyNewLimit by preferences.dailyNewLimit.collectAsState()
    val desiredRetention by preferences.desiredRetention.collectAsState()
    val speechRate by preferences.speechRate.collectAsState()
    val savedSession by preferences.savedSession.collectAsState()

    val fsrsEngine = remember(desiredRetention) {
        FsrsEngine(desiredRetention = desiredRetention.toDouble())
    }

    // Database states
    val wordsList by database.wordDao().getAllWords().collectAsState(initial = emptyList())
    val grammarList by database.grammarDao().getAllGrammar().collectAsState(initial = emptyList())

    // Ensure database seeds on launch if empty
    LaunchedEffect(wordsList.size) {
        if (wordsList.isEmpty()) {
            database.checkAndSeedData(context, coroutineScope)
        }
    }

    // Active Tab & Sub-screens
    var currentTab by remember { mutableStateOf<NavigationTab>(NavigationTab.Today) }
    var selectedWordId by remember { mutableStateOf<Int?>(null) }
    var selectedGrammarId by remember { mutableStateOf<String?>(null) }

    // Flashcard session state
    var isInSession by remember { mutableStateOf(false) }
    var activeSessionWordIds by remember { mutableStateOf<List<Int>>(emptyList()) }
    var activeSessionStartIndex by remember { mutableIntStateOf(0) }

    // Grammar quiz state
    var isInGrammarQuiz by remember { mutableStateOf(false) }
    var grammarQuizFilterLevel by remember { mutableStateOf("all") }

    // Exit App Confirmation Dialog state
    var showExitAppDialog by remember { mutableStateOf(false) }

    val dueWords = remember(wordsList) {
        val now = System.currentTimeMillis()
        wordsList.filter { it.state != "new" && it.nextReview <= now && !it.isSuspended }
    }
    val newWords = remember(wordsList, dailyNewLimit) {
        wordsList.filter { it.state == "new" && !it.isSuspended }.take(dailyNewLimit)
    }

    // Global Back Handler for Tabs & Root Exit Confirmation
    BackHandler(
        enabled = !isInSession && !isInGrammarQuiz && selectedWordId == null && selectedGrammarId == null
    ) {
        if (currentTab != NavigationTab.Today) {
            currentTab = NavigationTab.Today
        } else {
            showExitAppDialog = true
        }
    }

    if (showExitAppDialog) {
        AlertDialog(
            onDismissRequest = { showExitAppDialog = false },
            title = { Text("ออกจากแอป Kotoba?", fontWeight = FontWeight.Bold) },
            text = { Text("คุณต้องการปิดแอปพลิเคชันตอนนี้หรือไม่? ข้อมูลการเรียนทั้งหมดถูกบันทึกไว้เรียบร้อยแล้ว") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExitAppDialog = false
                        (context as? Activity)?.finish()
                    }
                ) {
                    Text("ออกจากแอป", color = AccentRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Button(onClick = { showExitAppDialog = false }) {
                    Text("อยู่ต่อ")
                }
            }
        )
    }

    // Sub-screen 1: Word Detail Screen (can also be opened on top of lists)
    if (selectedWordId != null) {
        val idx = wordsList.indexOfFirst { it.id == selectedWordId }
        val word = wordsList.getOrNull(idx)
        if (word != null) {
            WordDetailScreen(
                word = word,
                hasPrevious = idx > 0,
                hasNext = idx in 0 until (wordsList.size - 1),
                onPrevious = {
                    if (idx > 0) selectedWordId = wordsList[idx - 1].id
                },
                onNext = {
                    if (idx < wordsList.size - 1) selectedWordId = wordsList[idx + 1].id
                },
                onBack = { selectedWordId = null },
                onPlayAudio = { audioPlayer.speak(it) }
            )
            return
        } else {
            selectedWordId = null
        }
    }

    // Sub-screen 2: Grammar Detail Screen
    if (selectedGrammarId != null) {
        val idx = grammarList.indexOfFirst { it.id == selectedGrammarId }
        val grammar = grammarList.getOrNull(idx)
        if (grammar != null) {
            GrammarDetailScreen(
                grammar = grammar,
                hasPrevious = idx > 0,
                hasNext = idx in 0 until (grammarList.size - 1),
                onPrevious = {
                    if (idx > 0) selectedGrammarId = grammarList[idx - 1].id
                },
                onNext = {
                    if (idx < grammarList.size - 1) selectedGrammarId = grammarList[idx + 1].id
                },
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
        } else {
            selectedGrammarId = null
        }
    }

    // Sub-screen 3: Grammar Comprehension Quiz
    if (isInGrammarQuiz) {
        val quizPool = remember(grammarList, grammarQuizFilterLevel) {
            when (grammarQuizFilterLevel) {
                "base" -> grammarList.filter { it.lvl == "base" }
                "N5" -> grammarList.filter { it.lvl == "N5" }
                "N4" -> grammarList.filter { it.lvl == "N4" }
                else -> grammarList
            }.ifEmpty { grammarList }
        }
        GrammarQuizScreen(
            grammarPool = quizPool,
            onPlayAudio = { audioPlayer.speak(it) },
            onMarkMastered = { grammar, mastered ->
                coroutineScope.launch {
                    if (grammar.isMastered != mastered) {
                        database.grammarDao().updateGrammar(grammar.copy(isMastered = mastered))
                    }
                }
            },
            onOpenGrammarDetail = { id ->
                selectedGrammarId = id
            },
            onExitQuiz = { isInGrammarQuiz = false }
        )
        return
    }

    // Sub-screen 4: Flashcard Session with Real-time Progress Persistence
    if (isInSession) {
        val wordMap = remember(wordsList) { wordsList.associateBy { it.id } }
        val sessionWords: List<WordEntity> = remember(activeSessionWordIds, wordMap) {
            activeSessionWordIds.mapNotNull { wordMap[it] }
        }
        FlashcardSessionScreen(
            words = sessionWords,
            initialIndex = activeSessionStartIndex,
            desiredRetention = desiredRetention.toDouble(),
            onRateWord = { word, rating, nextIndex ->
                // 1. Save session progress and increment daily counter immediately
                preferences.recordCardReviewed()
                preferences.saveSessionProgress(activeSessionWordIds, nextIndex)
                activeSessionStartIndex = nextIndex

                // 2. Persist FSRS state to SQLite
                coroutineScope.launch {
                    val updated = fsrsEngine.review(word.copy(), rating)
                    database.wordDao().updateWord(updated)
                }
            },
            onPlayAudio = { audioPlayer.speak(it) },
            onFinishSession = { completedAll ->
                if (completedAll) {
                    preferences.clearSavedSession()
                }
                isInSession = false
            }
        )
        return
    }

    // Initial Seeding Loading State
    if (wordsList.isEmpty() && grammarList.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                Text(
                    text = "กำลังเตรียมคลังคำศัพท์และไวยากรณ์ Kotoba...",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
        return
    }

    // Main 5-tab layout
    Scaffold(
        bottomBar = {
            NavigationBar(
                tonalElevation = 4.dp,
                containerColor = MaterialTheme.colorScheme.surface
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
                        label = {
                            Text(
                                text = tab.label,
                                fontSize = 11.sp,
                                fontWeight = if (currentTab == tab) FontWeight.Bold else FontWeight.Normal
                            )
                        }
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
            Crossfade(
                targetState = currentTab,
                animationSpec = tween(durationMillis = 180),
                label = "TabTransition"
            ) { tab ->
                when (tab) {
                    NavigationTab.Today -> TodayScreen(
                        dueCount = dueWords.size,
                        newCount = newWords.size,
                        streakDays = streakDays,
                        doneToday = doneToday,
                        totalVocabCount = wordsList.size,
                        totalGrammarCount = grammarList.size,
                        masteredGrammarCount = grammarList.count { it.isMastered },
                        savedSession = savedSession,
                        themeMode = themeMode,
                        onToggleTheme = { preferences.cycleThemeMode() },
                        onStartSession = {
                            val queue = (dueWords + newWords).ifEmpty {
                                wordsList.shuffled().take(dailyNewLimit)
                            }.shuffled()
                            val ids = queue.map { it.id }
                            activeSessionWordIds = ids
                            activeSessionStartIndex = 0
                            preferences.saveSessionProgress(ids, 0)
                            isInSession = true
                        },
                        onResumeSession = {
                            if (savedSession.isActive) {
                                activeSessionWordIds = savedSession.wordIds
                                activeSessionStartIndex = savedSession.currentIndex
                                isInSession = true
                            }
                        },
                        onNavigateGrammar = { currentTab = NavigationTab.Grammar },
                        onStartGrammarQuiz = {
                            grammarQuizFilterLevel = "all"
                            isInGrammarQuiz = true
                        }
                    )

                    NavigationTab.Words -> WordsScreen(
                        words = wordsList,
                        onSelectWord = { id -> selectedWordId = id }
                    )

                    NavigationTab.Grammar -> GrammarScreen(
                        grammarList = grammarList,
                        onSelectGrammar = { id -> selectedGrammarId = id },
                        onStartQuiz = { level ->
                            grammarQuizFilterLevel = level
                            isInGrammarQuiz = true
                        }
                    )

                    NavigationTab.Stats -> StatsScreen(
                        words = wordsList,
                        grammarList = grammarList,
                        doneToday = doneToday,
                        streakDays = streakDays
                    )

                    NavigationTab.Settings -> SettingsScreen(
                        updater = updater,
                        themeMode = themeMode,
                        dailyNewLimit = dailyNewLimit,
                        desiredRetention = desiredRetention,
                        speechRate = speechRate,
                        onChangeThemeMode = { preferences.setThemeMode(it) },
                        onChangeDailyNewLimit = { preferences.setDailyNewLimit(it) },
                        onChangeDesiredRetention = { preferences.setDesiredRetention(it) },
                        onChangeSpeechRate = {
                            preferences.setSpeechRate(it)
                            audioPlayer.setSpeechRate(it)
                        },
                        onTestAudio = {
                            audioPlayer.speak("こんにちは。ことばのアプリへようこそ。")
                        },
                        onReloadContent = {
                            coroutineScope.launch(Dispatchers.IO) {
                                KotobaDatabase.populateInitialData(context, database)
                            }
                        }
                    )
                }
            }
        }
    }
}
