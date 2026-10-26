package com.kotoba.app.ui.screens

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.kotoba.app.audio.NativeAudioPlayer
import com.kotoba.app.data.AppPreferences
import com.kotoba.app.data.db.KotobaDatabase
import com.kotoba.app.data.model.WordEntity
import com.kotoba.app.fsrs.FsrsEngine
import com.kotoba.app.ui.theme.AccentRed
import com.kotoba.app.updater.GitHubUpdater
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

sealed class NavigationTab(val route: String, val label: String, val icon: ImageVector) {
    data object Today : NavigationTab("today", "วันนี้", Icons.Default.Today)
    data object Words : NavigationTab("words", "คำศัพท์", Icons.AutoMirrored.Filled.MenuBook)
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
    val prefNewDoneToday by preferences.newDoneToday.collectAsState()
    val streakDays by preferences.streakDays.collectAsState()
    val dailyNewLimit by preferences.dailyNewLimit.collectAsState()
    val desiredRetention by preferences.desiredRetention.collectAsState()
    val speechRate by preferences.speechRate.collectAsState()
    val selectedLevelFilter by preferences.newWordLevelFilter.collectAsState()
    val savedSession by preferences.savedSession.collectAsState()

    val fsrsEngine = remember(desiredRetention) {
        FsrsEngine(desiredRetention = desiredRetention.toDouble())
    }

    // Database state
    val wordsList by database.wordDao().getAllWords().collectAsState(initial = emptyList())

    // Live clock so 10-minute same-day reviews surface automatically without restarting the app
    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(20_000L)
            preferences.refreshDailyState()
            nowMillis = System.currentTimeMillis()
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                preferences.refreshDailyState()
                nowMillis = System.currentTimeMillis()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Ensure database seeds on launch if empty
    LaunchedEffect(wordsList.size) {
        if (wordsList.isEmpty()) {
            database.checkAndSeedData(context, coroutineScope)
        }
    }

    // Sync newDoneToday with SQLite firstStudiedAt truth (covers upgrades from older builds on the same day)
    val dbNewTodayCount = remember(wordsList, nowMillis) {
        val startOfDay = preferences.startOfTodayMillis()
        wordsList.count { it.state != "new" && it.firstStudiedAt >= startOfDay }
    }
    LaunchedEffect(dbNewTodayCount) {
        if (dbNewTodayCount > 0) {
            preferences.ensureNewDoneTodayAtLeast(dbNewTodayCount)
        }
    }

    // Effective new words studied today is the max of prefs and DB firstStudiedAt
    val effectiveNewDoneToday = maxOf(prefNewDoneToday, dbNewTodayCount)
    val remainingNewQuota = (dailyNewLimit - effectiveNewDoneToday).coerceAtLeast(0)

    // Active Tab & Sub-screens
    var currentTab by remember { mutableStateOf<NavigationTab>(NavigationTab.Today) }
    var selectedWordId by remember { mutableStateOf<Int?>(null) }

    // Flashcard session state
    var isInSession by remember { mutableStateOf(false) }
    var activeSessionWordIds by remember { mutableStateOf<List<Int>>(emptyList()) }
    var activeSessionStartIndex by remember { mutableIntStateOf(0) }
    var activeSessionIsExtra by remember { mutableStateOf(false) }
    val sessionRetries = remember { mutableStateMapOf<Int, Int>() }
    val sessionCountedNewIds = remember { mutableStateListOf<Int>() }

    // Exit App Confirmation Dialog state
    var showExitAppDialog by remember { mutableStateOf(false) }

    // Due reviews: ALWAYS across all levels, strictly already-learned words whose nextReview <= now
    val dueWords = remember(wordsList, nowMillis) {
        wordsList.filter { it.state != "new" && it.nextReview <= nowMillis && !it.isSuspended }
            .sortedBy { it.nextReview }
    }

    // New words for today: STRICTLY capped by remainingNewQuota! Once quota hits 0, empty for the rest of the day.
    val newWords = remember(wordsList, remainingNewQuota, selectedLevelFilter) {
        if (remainingNewQuota <= 0) {
            emptyList()
        } else {
            wordsList.filter { word ->
                word.state == "new" && !word.isSuspended &&
                    (selectedLevelFilter == "ALL" || word.lvl == selectedLevelFilter)
            }.take(remainingNewQuota)
        }
    }

    val learnedWords = remember(wordsList) {
        wordsList.filter { it.state != "new" && !it.isSuspended }
    }

    val nextDueHint = remember(wordsList, dueWords, nowMillis, remainingNewQuota) {
        if (dueWords.isNotEmpty() || remainingNewQuota > 0) {
            null
        } else {
            val nextCard = wordsList
                .filter { it.state != "new" && !it.isSuspended && it.nextReview > nowMillis }
                .minByOrNull { it.nextReview }
            if (nextCard == null) {
                "ครบโควตาคำศัพท์ใหม่วันนี้แล้ว"
            } else {
                val diffMin = ((nextCard.nextReview - nowMillis) / 60_000L).coerceAtLeast(1)
                when {
                    diffMin < 60 -> "โควตาคำใหม่ครบแล้ว · มีคำรอทบทวนถัดไปในอีก $diffMin นาที"
                    diffMin < 24 * 60 -> "โควตาคำใหม่ครบแล้ว · มีคำรอทบทวนถัดไปในอีก ${diffMin / 60} ชั่วโมง"
                    else -> "ครบโควตาคำใหม่วันนี้แล้ว · รอทบทวนรอบถัดไปพรุ่งนี้"
                }
            }
        }
    }

    // Global Back Handler for Tabs & Root Exit Confirmation
    BackHandler(
        enabled = !isInSession && selectedWordId == null
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

    // Sub-screen 1: Word Detail Screen
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

    // Sub-screen 2: Flashcard Session with Real-time Progress Persistence & Re-queue on "จำไม่ได้"
    if (isInSession) {
        val wordMap = remember(wordsList) { wordsList.associateBy { it.id } }
        val sessionWords: List<WordEntity> = remember(activeSessionWordIds, wordMap, sessionCountedNewIds.size) {
            activeSessionWordIds.mapNotNull { id ->
                val w = wordMap[id]
                // If a card was already seen once in this session, ensure its badge shows "ทบทวน"
                if (w != null && id in sessionCountedNewIds && w.state == "new") {
                    w.copy(state = "learning")
                } else {
                    w
                }
            }
        }
        FlashcardSessionScreen(
            words = sessionWords,
            initialIndex = activeSessionStartIndex,
            desiredRetention = desiredRetention.toDouble(),
            onRateWord = { word, rating, nextIndex ->
                val isFirstTimeNew = (word.state == "new") && (word.id !in sessionCountedNewIds)
                if (isFirstTimeNew) {
                    sessionCountedNewIds.add(word.id)
                }

                // 1. Record review & new-word quota immediately
                preferences.recordCardReviewed(isNewWord = isFirstTimeNew)

                // 2. If user tapped "จำไม่ได้" (rating == 1), re-queue at end of session (up to 2 retries)
                var updatedSessionIds = activeSessionWordIds
                if (rating == 1) {
                    val retriesSoFar = sessionRetries[word.id] ?: 0
                    if (retriesSoFar < 2) {
                        sessionRetries[word.id] = retriesSoFar + 1
                        updatedSessionIds = activeSessionWordIds + word.id
                        activeSessionWordIds = updatedSessionIds
                    }
                }

                preferences.saveSessionProgress(updatedSessionIds, nextIndex, activeSessionIsExtra)
                activeSessionStartIndex = nextIndex
                nowMillis = System.currentTimeMillis()

                // 3. Persist FSRS state to SQLite
                coroutineScope.launch {
                    val latestWord = wordMap[word.id] ?: word
                    val updated = fsrsEngine.review(latestWord.copy(), rating)
                    database.wordDao().updateWord(updated)
                }
            },
            onPlayAudio = { audioPlayer.speak(it) },
            onFinishSession = { completedAll ->
                if (completedAll) {
                    preferences.clearSavedSession()
                }
                nowMillis = System.currentTimeMillis()
                isInSession = false
            }
        )
        return
    }

    // Initial Seeding Loading State
    if (wordsList.isEmpty()) {
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
                    text = "กำลังเตรียมคลังคำศัพท์ JLPT N5 – N3...",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
        return
    }

    // Clean 4-tab layout (Today, Words, Stats, Settings)
    Scaffold(
        bottomBar = {
            NavigationBar(
                tonalElevation = 4.dp,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                listOf(
                    NavigationTab.Today,
                    NavigationTab.Words,
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
                        words = wordsList,
                        dueCount = dueWords.size,
                        newCount = newWords.size,
                        newDoneToday = effectiveNewDoneToday,
                        dailyNewLimit = dailyNewLimit,
                        streakDays = streakDays,
                        doneToday = doneToday,
                        learnedCount = learnedWords.size,
                        nextDueHint = nextDueHint,
                        selectedLevelFilter = selectedLevelFilter,
                        savedSession = savedSession,
                        themeMode = themeMode,
                        onSelectLevelFilter = { level ->
                            preferences.setNewWordLevelFilter(level)
                            // If a saved session hasn't started or has unstudied new words, clear so new filter applies cleanly
                            if (savedSession.isActive && !savedSession.isExtraReview) {
                                preferences.clearSavedSession()
                            }
                        },
                        onToggleTheme = { preferences.cycleThemeMode() },
                        onStartSession = {
                            // Strictly due reviews + today's remaining new-word quota ONLY
                            val queue = dueWords + newWords.shuffled()
                            if (queue.isNotEmpty()) {
                                val ids = queue.map { it.id }
                                sessionRetries.clear()
                                sessionCountedNewIds.clear()
                                activeSessionWordIds = ids
                                activeSessionStartIndex = 0
                                activeSessionIsExtra = false
                                preferences.saveSessionProgress(ids, 0, isExtraReview = false)
                                isInSession = true
                            }
                        },
                        onStartExtraReview = {
                            // Extra Review ONLY ever pulls ALREADY-LEARNED words (state != "new"), sorted by weakest/soonest due
                            val reviewOnlyQueue = learnedWords
                                .sortedWith(compareBy<WordEntity> { it.stability }.thenBy { it.nextReview })
                                .take(20)
                                .shuffled()
                            if (reviewOnlyQueue.isNotEmpty()) {
                                val ids = reviewOnlyQueue.map { it.id }
                                sessionRetries.clear()
                                sessionCountedNewIds.clear()
                                activeSessionWordIds = ids
                                activeSessionStartIndex = 0
                                activeSessionIsExtra = true
                                preferences.saveSessionProgress(ids, 0, isExtraReview = true)
                                isInSession = true
                            }
                        },
                        onResumeSession = {
                            if (savedSession.isActive) {
                                sessionRetries.clear()
                                sessionCountedNewIds.clear()
                                activeSessionWordIds = savedSession.wordIds
                                activeSessionStartIndex = savedSession.currentIndex
                                activeSessionIsExtra = savedSession.isExtraReview
                                isInSession = true
                            }
                        },
                        onNavigateWords = { currentTab = NavigationTab.Words }
                    )

                    NavigationTab.Words -> WordsScreen(
                        words = wordsList,
                        onSelectWord = { id -> selectedWordId = id }
                    )

                    NavigationTab.Stats -> StatsScreen(
                        words = wordsList,
                        doneToday = doneToday,
                        newDoneToday = effectiveNewDoneToday,
                        streakDays = streakDays
                    )

                    NavigationTab.Settings -> SettingsScreen(
                        updater = updater,
                        themeMode = themeMode,
                        dailyNewLimit = dailyNewLimit,
                        desiredRetention = desiredRetention,
                        speechRate = speechRate,
                        onChangeThemeMode = { preferences.setThemeMode(it) },
                        onChangeDailyNewLimit = { newLimit ->
                            preferences.setDailyNewLimit(newLimit)
                            // If a saved session had queued more new words than the updated limit allows, clear it so the new limit applies immediately
                            if (savedSession.isActive && !savedSession.isExtraReview) {
                                preferences.clearSavedSession()
                            }
                        },
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
                                KotobaDatabase.populateInitialData(context, database, force = true)
                            }
                        }
                    )
                }
            }
        }
    }
}
