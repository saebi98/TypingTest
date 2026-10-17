package com.typingtest.korean.ui.longtext

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.typingtest.korean.data.ContentRepository
import com.typingtest.korean.data.TypingMode
import com.typingtest.korean.engine.HangulEngine
import com.typingtest.korean.ui.components.ColoredTargetText
import com.typingtest.korean.ui.components.PracticeResultDialog
import com.typingtest.korean.ui.components.TypingTopMetricsBar
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LongTextScreen(
    onSaveRecord: (TypingMode, String, Int, Int, Float, Int, Int) -> Unit,
    onBack: () -> Unit
) {
    val works = ContentRepository.longTextWorks
    var selectedWorkIdx by remember { mutableIntStateOf(0) }
    val currentWork = works[selectedWorkIdx]

    var currentLineIdx by remember(selectedWorkIdx) { mutableIntStateOf(0) }
    var currentInput by remember(selectedWorkIdx) { mutableStateOf("") }

    var sessionStartMillis by remember(selectedWorkIdx) { mutableLongStateOf(0L) }
    var elapsedMillis by remember(selectedWorkIdx) { mutableLongStateOf(0L) }
    var completedStrokes by remember(selectedWorkIdx) { mutableIntStateOf(0) }
    var totalTypedStrokes by remember(selectedWorkIdx) { mutableIntStateOf(0) }
    var maxCpm by remember(selectedWorkIdx) { mutableIntStateOf(0) }
    var showResultDialog by remember(selectedWorkIdx) { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }
    val currentLine = currentWork.lines.getOrElse(currentLineIdx) { "" }

    LaunchedEffect(selectedWorkIdx) {
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

    val evaluation = remember(currentLine, currentInput, elapsedMillis, completedStrokes, totalTypedStrokes) {
        HangulEngine.evaluate(
            target = currentLine,
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

    fun finishAndSaveSession() {
        val finalCompleted = completedStrokes + evaluation.matchedStrokes
        val finalTyped = totalTypedStrokes + evaluation.totalTypedStrokes
        val finalEval = HangulEngine.evaluate(
            target = "",
            input = "",
            elapsedMillis = maxOf(elapsedMillis, 1000L),
            previouslyCompletedStrokes = finalCompleted,
            previousTotalTypedStrokes = finalTyped
        )
        completedStrokes = finalCompleted
        totalTypedStrokes = finalTyped
        val finalMax = maxOf(maxCpm, finalEval.cpm)
        val durSec = (elapsedMillis / 1000L).toInt().coerceAtLeast(1)
        onSaveRecord(
            TypingMode.LONG_TEXT,
            "${currentWork.title} (${currentWork.author})",
            finalEval.cpm,
            finalMax,
            finalEval.accuracyPercent,
            currentLineIdx + 1,
            durSec
        )
        showResultDialog = true
    }

    fun submitCurrentLine() {
        if (currentInput.isBlank()) return
        val targetStrokeCount = HangulEngine.countStrokes(currentLine)
        val inputStrokeCount = maxOf(HangulEngine.countStrokes(currentInput), targetStrokeCount)
        completedStrokes += evaluation.matchedStrokes
        totalTypedStrokes += inputStrokeCount

        if (currentLineIdx + 1 >= currentWork.lines.size) {
            currentInput = ""
            finishAndSaveSession()
        } else {
            currentLineIdx++
            currentInput = ""
        }
    }

    // 현재 줄이 정확히 일치하면 자동으로 다음 줄로 이동
    LaunchedEffect(currentInput, currentLine) {
        if (currentLine.isNotEmpty() && currentInput == currentLine && !showResultDialog) {
            delay(180L)
            submitCurrentLine()
        }
    }

    fun resetSession() {
        currentLineIdx = 0
        currentInput = ""
        sessionStartMillis = 0L
        elapsedMillis = 0L
        completedStrokes = 0
        totalTypedStrokes = 0
        maxCpm = 0
        showResultDialog = false
    }

    fun onInputChanged(raw: String) {
        if (sessionStartMillis == 0L && raw.isNotEmpty()) {
            sessionStartMillis = System.currentTimeMillis()
        }
        if (raw.contains("\n")) {
            val cleaned = raw.replace("\n", "")
            currentInput = cleaned
            if (cleaned.length >= (currentLine.length * 0.6).toInt()) {
                submitCurrentLine()
            }
            return
        }
        currentInput = raw
    }

    val pageSize = 4
    val currentPage = currentLineIdx / pageSize
    val totalPages = (currentWork.lines.size + pageSize - 1) / pageSize
    val pageStartIdx = currentPage * pageSize
    val pageEndIdx = minOf(pageStartIdx + pageSize, currentWork.lines.size)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("긴 글 연습", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                },
                actions = {
                    if (completedStrokes > 0 || currentInput.isNotEmpty()) {
                        IconButton(onClick = { finishAndSaveSession() }) {
                            Icon(Icons.Default.DoneAll, contentDescription = "현재까지 결과 저장")
                        }
                    }
                    IconButton(onClick = { resetSession() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "처음부터")
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
            // 문학 작품 선택 가로 스크롤 칩
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                works.forEachIndexed { idx, work ->
                    FilterChip(
                        selected = selectedWorkIdx == idx,
                        onClick = { selectedWorkIdx = idx },
                        label = { Text("${work.title} · ${work.author}") }
                    )
                }
            }

            TypingTopMetricsBar(
                cpm = evaluation.cpm,
                maxCpm = maxCpm,
                accuracy = evaluation.accuracyPercent,
                elapsedSeconds = (elapsedMillis / 1000L).toInt(),
                progressText = "${currentLineIdx + 1}/${currentWork.lines.size}줄",
                progressFraction = currentLineIdx.toFloat() / currentWork.lines.size.coerceAtLeast(1)
            )

            // 원고지/페이지 뷰 카드
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "『${currentWork.title}』 — ${currentWork.author}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "페이지 ${currentPage + 1} / $totalPages",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // 현재 페이지의 줄 표시
                    for (lineIdx in pageStartIdx until pageEndIdx) {
                        val lineText = currentWork.lines[lineIdx]
                        when {
                            lineIdx < currentLineIdx -> {
                                // 이미 완료한 줄
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFF10B981).copy(alpha = 0.1f))
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = "✓ $lineText",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFF059669)
                                    )
                                }
                            }
                            lineIdx == currentLineIdx -> {
                                // 현재 입력 중인 줄 + 바로 아래 입력창
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f))
                                        .padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    ColoredTargetText(
                                        target = lineText,
                                        statuses = evaluation.charStatuses,
                                        fontSize = 19.sp
                                    )

                                    OutlinedTextField(
                                        value = currentInput,
                                        onValueChange = { onInputChanged(it) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .focusRequester(focusRequester),
                                        singleLine = true,
                                        label = { Text("현재 줄 입력 (완성 또는 Enter 시 다음 줄)") },
                                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                        keyboardActions = KeyboardActions(onNext = { submitCurrentLine() })
                                    )
                                }
                            }
                            else -> {
                                // 아직 입력 전인 다음 줄
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = lineText,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedButton(
                            onClick = { finishAndSaveSession() },
                            enabled = completedStrokes > 0 || currentInput.isNotBlank()
                        ) {
                            Text("여기까지 기록 저장")
                        }

                        Button(
                            onClick = { submitCurrentLine() },
                            enabled = currentInput.isNotBlank()
                        ) {
                            Text("다음 줄로 넘어가기 ↵")
                        }
                    }
                }
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
            title = "긴 글 연습 결과",
            subtitle = "『${currentWork.title}』 (${currentWork.author})",
            avgCpm = finalEval.cpm,
            maxCpm = maxOf(maxCpm, finalEval.cpm),
            accuracy = finalEval.accuracyPercent,
            durationSec = (elapsedMillis / 1000L).toInt().coerceAtLeast(1),
            onRetry = { resetSession() },
            onDismissToHome = onBack
        )
    }
}
