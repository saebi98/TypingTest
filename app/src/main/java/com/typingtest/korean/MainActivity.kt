package com.typingtest.korean

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.typingtest.korean.data.TypingRecordStore
import com.typingtest.korean.ui.game.AcidRainGameScreen
import com.typingtest.korean.ui.home.HomeScreen
import com.typingtest.korean.ui.longtext.LongTextScreen
import com.typingtest.korean.ui.position.PositionPracticeScreen
import com.typingtest.korean.ui.shorttext.ShortSentenceScreen
import com.typingtest.korean.ui.stats.StatisticsScreen
import com.typingtest.korean.ui.theme.KoreanTypingTheme
import com.typingtest.korean.ui.word.WordPracticeScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KoreanTypingTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    KoreanTypingApp()
                }
            }
        }
    }
}

@Composable
fun KoreanTypingApp() {
    val context = LocalContext.current
    val recordStore = remember { TypingRecordStore.getInstance(context) }
    val summary by recordStore.summaryFlow.collectAsState()
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                summary = summary,
                onNavigatePosition = { navController.navigate("position") },
                onNavigateWord = { navController.navigate("word") },
                onNavigateShortText = { navController.navigate("short_text") },
                onNavigateLongText = { navController.navigate("long_text") },
                onNavigateAcidRain = { navController.navigate("acid_rain") },
                onNavigateStats = { navController.navigate("stats") }
            )
        }

        composable("position") {
            PositionPracticeScreen(
                onSaveRecord = { mode, title, avgCpm, maxCpm, acc, score, durSec ->
                    recordStore.insertRecord(mode, title, avgCpm, maxCpm, acc, score, durSec)
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable("word") {
            WordPracticeScreen(
                onSaveRecord = { mode, title, avgCpm, maxCpm, acc, score, durSec ->
                    recordStore.insertRecord(mode, title, avgCpm, maxCpm, acc, score, durSec)
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable("short_text") {
            ShortSentenceScreen(
                onSaveRecord = { mode, title, avgCpm, maxCpm, acc, score, durSec ->
                    recordStore.insertRecord(mode, title, avgCpm, maxCpm, acc, score, durSec)
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable("long_text") {
            LongTextScreen(
                onSaveRecord = { mode, title, avgCpm, maxCpm, acc, score, durSec ->
                    recordStore.insertRecord(mode, title, avgCpm, maxCpm, acc, score, durSec)
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable("acid_rain") {
            AcidRainGameScreen(
                onSaveRecord = { mode, title, avgCpm, maxCpm, acc, score, durSec ->
                    recordStore.insertRecord(mode, title, avgCpm, maxCpm, acc, score, durSec)
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable("stats") {
            StatisticsScreen(
                summary = summary,
                onClearRecords = { recordStore.clearAllRecords() },
                onBack = { navController.popBackStack() }
            )
        }
    }
}
