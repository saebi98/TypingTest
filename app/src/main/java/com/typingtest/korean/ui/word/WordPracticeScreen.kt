package com.typingtest.korean.ui.word

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.typingtest.korean.data.ContentRepository
import com.typingtest.korean.data.TypingLanguage
import com.typingtest.korean.data.TypingMode
import com.typingtest.korean.data.WordDifficulty
import com.typingtest.korean.engine.HangulEngine
import com.typingtest.korean.ui.components.ColoredTargetText
import com.typingtest.korean.ui.components.DubeolsikKeyboard
import com.typingtest.korean.ui.components.LanguageSelectorRow
import com.typingtest.korean.ui.components.PracticeResultDialog
import com.typingtest.korean.ui.components.TypingTopMetricsBar
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordPracticeScreen(
    initialLanguage: TypingLanguage = TypingLanguage.KOREAN,
    onLanguageChange: (TypingLanguage) -> Unit = {},
    onSaveRecord: (TypingMode, String, Int, Int, Float, Int, Int) -> Unit,
    onBack: () -> Unit
) {
    var language by remember { mutableStateOf(initialLanguage) }
    var difficulty by remember { mutableStateOf(WordDifficulty.BEGINNER) }
    var words by remember(language, difficulty) {
        mutableStateOf(ContentRepository.getWordsByDifficulty(difficulty, language).take(20))
    }
    var currentIndex by remember(language, difficulty) { mutableIntStateOf(0) }
    var currentInput by remember(language, difficulty) { mutableStateOf("") }

    var startTimeMillis by remember(language, difficulty) { mutableLongStateOf(0L) }
    var elapsedMillis by remember(language, difficulty) { mutableLongStateOf(0L) }
    var completedStrokes by remember(language, difficulty) { mutableIntStateOf(0) }
    var totalTypedStrokes by remember(language, difficulty) { mutableIntStateOf(0) }
    var maxCpm by remember(language, difficulty) { mutableIntStateOf(0) }
    var showResultDialog by remember(language, difficulty) { mutableStateOf(false) }
    var showGuideKeyboard by remember { mutableStateOf(true) }

    val focusRequester = remember { FocusRequester() }
    val currentWord = words.getOrElse(currentIndex) { "" }

    LaunchedEffect(Unit) {
        runCatching { focusRequester.requestFocus() }
    }

    LaunchedEffect(startTimeMillis, showResultDialog) {
        if (startTimeMillis > 0L && !showResultDialog) {
            while (true) {
                elapsedMillis = System.currentTimeMillis() - startTimeMillis
                delay(100L)
            }
        }
    }

    val evaluation = remember(currentWord, currentInput, elapsedMillis, completedStrokes, totalTypedStrokes) {
        HangulEngine.evaluate(
            target = currentWord,
            input = currentInput,
            elapsedMillis = elapsedMillis,
            previouslyCompletedStrokes = completedStrokes,
            previousTotalTypedStrokes = totalTypedStrokes
        )
    }

    LaunchedEffect(evaluation.cpm) {
        if (evaluation.cpm > maxCpm) {
            maxCpm = evaluation.cpm
        }
    }

    fun submitCurrentWord() {
        if (currentInput.isBlank()) return
        val targetStrokeCount = HangulEngine.countStrokes(currentWord)
        val inputStrokeCount = maxOf(HangulEngine.countStrokes(currentInput), targetStrokeCount)
        val newCompleted = completedStrokes + evaluation.matchedStrokes
        val newTotalTyped = totalTypedStrokes + inputStrokeCount

        completedStrokes = newCompleted
        totalTypedStrokes = newTotalTyped

        if (currentIndex + 1 >= words.size) {
            val finalEval = HangulEngine.evaluate(
                target = "",
                input = "",
                elapsedMillis = maxOf(elapsedMillis, 1000L),
                previouslyCompletedStrokes = newCompleted,
                previousTotalTypedStrokes = newTotalTyped
            )
            val finalMax = maxOf(maxCpm, finalEval.cpm)
            val durSec = (elapsedMillis / 1000L).toInt().coerceAtLeast(1)
            onSaveRecord(
                TypingMode.WORD,
                "[${language.shortLabel}] ${difficulty.label}",
                finalEval.cpm,
                finalMax,
                finalEval.accuracyPercent,
                words.size,
                durSec
            )
            showResultDialog = true
        } else {
            currentIndex++
            currentInput = ""
        }
    }

    LaunchedEffect(currentInput, currentWord) {
        if (currentWord.isNotEmpty() && currentInput == currentWord && !showResultDialog) {
            delay(160L)
            submitCurrentWord()
        }
    }

    fun resetSession() {
        words = ContentRepository.getWordsByDifficulty(difficulty, language).take(20)
        currentIndex = 0
        currentInput = ""
        startTimeMillis = 0L
        elapsedMillis = 0L
        completedStrokes = 0
        totalTypedStrokes = 0
        maxCpm = 0
        showResultDialog = false
    }

    fun onInputChanged(raw: String) {
        if (startTimeMillis == 0L && raw.isNotEmpty()) {
            startTimeMillis = System.currentTimeMillis()
        }
        if (raw.endsWith("\n") || raw.endsWith(" ")) {
            val trimmed = raw.trim()
            if (trimmed.isNotEmpty()) {
                currentInput = trimmed
                submitCurrentWord()
                return
            }
        }
        currentInput = raw
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("낱말 연습 (${language.shortLabel})", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                },
                actions = {
                    IconButton(onClick = { resetSession() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "새 단어 묶음")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LanguageSelectorRow(
                selectedLanguage = language,
                onSelectLanguage = {
                    language = it
                    onLanguageChange(it)
                }
            )

            // 난이도 선택 칩
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WordDifficulty.entries.forEach { diff ->
                    FilterChip(
                        selected = difficulty == diff,
                        onClick = { difficulty = diff },
                        label = { Text(diff.label) }
                    )
                }
            }

            Text(
                text = difficulty.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            TypingTopMetricsBar(
                cpm = evaluation.cpm,
                maxCpm = maxCpm,
                accuracy = evaluation.accuracyPercent,
                elapsedSeconds = (elapsedMillis / 1000L).toInt(),
                progressText = "${currentIndex + 1}/${words.size}",
                progressFraction = currentIndex.toFloat() / words.size.coerceAtLeast(1)
            )

            // 3단 낱말 표시 카드
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 22.dp, horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val prevWord = words.getOrNull(currentIndex - 1) ?: "시작"
                        val nextWord = words.getOrNull(currentIndex + 1) ?: "마지막"
                        Text(
                            text = "이전: $prevWord",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "다음: $nextWord",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f))
                            .padding(vertical = 22.dp, horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        ColoredTargetText(
                            target = currentWord,
                            statuses = evaluation.charStatuses,
                            fontSize = if (currentWord.length <= 8) 38.sp else 30.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // 입력창 및 다음 버튼
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = currentInput,
                    onValueChange = { onInputChanged(it) },
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester),
                    singleLine = true,
                    label = { Text("단어 입력 (완성 시 자동 전환 / Space·Enter)") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { submitCurrentWord() })
                )

                Button(
                    onClick = { submitCurrentWord() },
                    modifier = Modifier.height(56.dp)
                ) {
                    Icon(Icons.Default.SkipNext, contentDescription = "다음")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (language == TypingLanguage.KOREAN) {
                        "두벌식 자판 가이드 & 터치 키패드 표시"
                    } else {
                        "QWERTY 자판 가이드 & 터치 키패드 표시"
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
                Switch(
                    checked = showGuideKeyboard,
                    onCheckedChange = { showGuideKeyboard = it }
                )
            }

            if (showGuideKeyboard) {
                DubeolsikKeyboard(
                    nextRequiredJamo = evaluation.nextRequiredJamo,
                    requiresShift = evaluation.requiresShift,
                    language = language,
                    interactive = true,
                    showBottomActionRow = true,
                    onJamoClick = { jamo ->
                        val updated = HangulEngine.appendJamo(currentInput, jamo)
                        onInputChanged(updated)
                    },
                    onBackspaceClick = {
                        currentInput = HangulEngine.backspaceJamo(currentInput)
                    },
                    onSpaceClick = { submitCurrentWord() },
                    onEnterClick = { submitCurrentWord() }
                )
            }
        }
    }

    if (showResultDialog) {
        val finalEval = HangulEngine.evaluate(
            target = "",
            input = "",
            elapsedMillis = maxOf(elapsedMillis, 1000L),
            previouslyCompletedStrokes = completedStrokes,
            previousTotalTypedStrokes = totalTypedStrokes
        )
        PracticeResultDialog(
            title = "낱말 연습 완주!",
            subtitle = "[${language.shortLabel}] ${difficulty.label} (20단어)",
            avgCpm = finalEval.cpm,
            maxCpm = maxOf(maxCpm, finalEval.cpm),
            accuracy = finalEval.accuracyPercent,
            durationSec = (elapsedMillis / 1000L).toInt().coerceAtLeast(1),
            onRetry = { resetSession() },
            onDismissToHome = onBack
        )
    }
}
