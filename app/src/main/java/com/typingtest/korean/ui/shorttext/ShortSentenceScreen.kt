package com.typingtest.korean.ui.shorttext

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.typingtest.korean.data.ContentRepository
import com.typingtest.korean.data.TypingMode
import com.typingtest.korean.engine.HangulEngine
import com.typingtest.korean.ui.components.ColoredTargetText
import com.typingtest.korean.ui.components.DubeolsikKeyboard
import com.typingtest.korean.ui.components.PracticeResultDialog
import com.typingtest.korean.ui.components.TypingTopMetricsBar
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShortSentenceScreen(
    onSaveRecord: (TypingMode, String, Int, Int, Float, Int, Int) -> Unit,
    onBack: () -> Unit
) {
    val totalSetSize = 10
    var sentences by remember {
        mutableStateOf(ContentRepository.shortSentences.shuffled().take(totalSetSize))
    }
    var currentIndex by remember { mutableIntStateOf(0) }
    var currentInput by remember { mutableStateOf("") }

    var sessionStartMillis by remember { mutableLongStateOf(0L) }
    var elapsedMillis by remember { mutableLongStateOf(0L) }
    var completedStrokes by remember { mutableIntStateOf(0) }
    var totalTypedStrokes by remember { mutableIntStateOf(0) }
    var maxCpm by remember { mutableIntStateOf(0) }
    val sentenceCpmHistory = remember { mutableStateListOf<Int>() }
    var showResultDialog by remember { mutableStateOf(false) }
    var showGuideKeyboard by remember { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }
    val currentSentence = sentences.getOrElse(currentIndex) { "" }

    LaunchedEffect(Unit) {
        runCatching { focusRequester.requestFocus() }
    }

    LaunchedEffect(sessionStartMillis, showResultDialog) {
        if (sessionStartMillis > 0L && !showResultDialog) {
            while (true) {
                elapsedMillis = System.currentTimeMillis() - sessionStartMillis
                delay(100L)
            }
        }
    }

    val evaluation = remember(currentSentence, currentInput, elapsedMillis, completedStrokes, totalTypedStrokes) {
        HangulEngine.evaluate(
            target = currentSentence,
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

    fun submitCurrentSentence() {
        if (currentInput.isBlank()) return
        val targetStrokeCount = HangulEngine.countStrokes(currentSentence)
        val inputStrokeCount = maxOf(HangulEngine.countStrokes(currentInput), targetStrokeCount)
        val newCompleted = completedStrokes + evaluation.matchedStrokes
        val newTotalTyped = totalTypedStrokes + inputStrokeCount

        completedStrokes = newCompleted
        totalTypedStrokes = newTotalTyped
        sentenceCpmHistory.add(evaluation.cpm)

        if (currentIndex + 1 >= sentences.size) {
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
                TypingMode.SHORT_TEXT,
                "속담·명언 10문장",
                finalEval.cpm,
                finalMax,
                finalEval.accuracyPercent,
                sentences.size,
                durSec
            )
            showResultDialog = true
        } else {
            currentIndex++
            currentInput = ""
        }
    }

    // 목표 문장과 완전히 일치하면 자동 제출
    LaunchedEffect(currentInput, currentSentence) {
        if (currentSentence.isNotEmpty() && currentInput == currentSentence && !showResultDialog) {
            delay(180L)
            submitCurrentSentence()
        }
    }

    fun resetSession() {
        sentences = ContentRepository.shortSentences.shuffled().take(totalSetSize)
        currentIndex = 0
        currentInput = ""
        sessionStartMillis = 0L
        elapsedMillis = 0L
        completedStrokes = 0
        totalTypedStrokes = 0
        maxCpm = 0
        sentenceCpmHistory.clear()
        showResultDialog = false
    }

    fun onInputChanged(raw: String) {
        if (sessionStartMillis == 0L && raw.isNotEmpty()) {
            sessionStartMillis = System.currentTimeMillis()
        }
        if (raw.contains("\n")) {
            val cleaned = raw.replace("\n", "")
            currentInput = cleaned
            if (cleaned.length >= (currentSentence.length * 0.7).toInt()) {
                submitCurrentSentence()
            }
            return
        }
        currentInput = raw
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("짧은 글 연습", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                },
                actions = {
                    IconButton(onClick = { resetSession() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "새 문장 세트")
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
            TypingTopMetricsBar(
                cpm = evaluation.cpm,
                maxCpm = maxCpm,
                accuracy = evaluation.accuracyPercent,
                elapsedSeconds = (elapsedMillis / 1000L).toInt(),
                progressText = "${currentIndex + 1}/${sentences.size}",
                progressFraction = currentIndex.toFloat() / sentences.size.coerceAtLeast(1)
            )

            // 현재 문장 & 입력 카드
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "목표 문장 (${currentIndex + 1} / ${sentences.size})",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                            .padding(16.dp)
                    ) {
                        ColoredTargetText(
                            target = currentSentence,
                            statuses = evaluation.charStatuses,
                            fontSize = 22.sp
                        )
                    }

                    OutlinedTextField(
                        value = currentInput,
                        onValueChange = { onInputChanged(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        singleLine = true,
                        label = { Text("위 문장을 따라 입력하세요 (완성 또는 Enter 시 다음 문장)") },
                        trailingIcon = {
                            IconButton(
                                onClick = { submitCurrentSentence() },
                                enabled = currentInput.isNotBlank()
                            ) {
                                Icon(Icons.Default.Check, contentDescription = "다음 문장")
                            }
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { submitCurrentSentence() })
                    )

                    val nextSentence = sentences.getOrNull(currentIndex + 1)
                    if (nextSentence != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "다음 문장: $nextSentence",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "두벌식 자판 가이드 표시",
                    style = MaterialTheme.typography.bodyMedium
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Switch(
                        checked = showGuideKeyboard,
                        onCheckedChange = { showGuideKeyboard = it }
                    )
                    Button(
                        onClick = { submitCurrentSentence() },
                        enabled = currentInput.isNotBlank()
                    ) {
                        Text("다음 문장 ↵")
                    }
                }
            }

            if (showGuideKeyboard) {
                DubeolsikKeyboard(
                    nextRequiredJamo = evaluation.nextRequiredJamo,
                    requiresShift = evaluation.requiresShift,
                    interactive = true,
                    showBottomActionRow = true,
                    onJamoClick = { jamo ->
                        val updated = HangulEngine.appendJamo(currentInput, jamo)
                        onInputChanged(updated)
                    },
                    onBackspaceClick = {
                        currentInput = HangulEngine.backspaceJamo(currentInput)
                    },
                    onSpaceClick = {
                        onInputChanged("$currentInput ")
                    },
                    onEnterClick = {
                        submitCurrentSentence()
                    }
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
            title = "짧은 글 연습 완료!",
            subtitle = "속담·명언 10문장 완주",
            avgCpm = finalEval.cpm,
            maxCpm = maxOf(maxCpm, finalEval.cpm),
            accuracy = finalEval.accuracyPercent,
            durationSec = (elapsedMillis / 1000L).toInt().coerceAtLeast(1),
            onRetry = { resetSession() },
            onDismissToHome = onBack
        )
    }
}
