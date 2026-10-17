package com.typingtest.korean.ui.position

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PositionPracticeScreen(
    onSaveRecord: (TypingMode, String, Int, Int, Float, Int, Int) -> Unit,
    onBack: () -> Unit
) {
    val stages = ContentRepository.positionStages
    var selectedStageIdx by remember { mutableIntStateOf(0) }
    val currentStage = stages[selectedStageIdx]

    var targetItems by remember(selectedStageIdx) {
        mutableStateOf(currentStage.items.shuffled().take(25))
    }
    var currentIndex by remember(selectedStageIdx) { mutableIntStateOf(0) }
    var currentInput by remember(selectedStageIdx) { mutableStateOf("") }

    var startTimeMillis by remember(selectedStageIdx) { mutableLongStateOf(0L) }
    var elapsedMillis by remember(selectedStageIdx) { mutableLongStateOf(0L) }
    var completedStrokes by remember(selectedStageIdx) { mutableIntStateOf(0) }
    var totalTypedStrokes by remember(selectedStageIdx) { mutableIntStateOf(0) }
    var maxCpm by remember(selectedStageIdx) { mutableIntStateOf(0) }
    var showResultDialog by remember(selectedStageIdx) { mutableStateOf(false) }
    var useBuiltInKeyboard by remember { mutableStateOf(true) }

    val currentTarget = targetItems.getOrElse(currentIndex) { "" }

    LaunchedEffect(startTimeMillis, showResultDialog) {
        if (startTimeMillis > 0L && !showResultDialog) {
            while (true) {
                elapsedMillis = System.currentTimeMillis() - startTimeMillis
                delay(100L)
            }
        }
    }

    val evaluation = remember(currentTarget, currentInput, elapsedMillis, completedStrokes, totalTypedStrokes) {
        HangulEngine.evaluate(
            target = currentTarget,
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

    fun advanceOrFinish() {
        val targetStrokeCount = HangulEngine.countStrokes(currentTarget)
        val inputStrokeCount = maxOf(HangulEngine.countStrokes(currentInput), targetStrokeCount)
        val newCompletedStrokes = completedStrokes + evaluation.matchedStrokes
        val newTotalTyped = totalTypedStrokes + inputStrokeCount

        completedStrokes = newCompletedStrokes
        totalTypedStrokes = newTotalTyped

        if (currentIndex + 1 >= targetItems.size) {
            val finalEval = HangulEngine.evaluate(
                target = "",
                input = "",
                elapsedMillis = maxOf(elapsedMillis, 1000L),
                previouslyCompletedStrokes = newCompletedStrokes,
                previousTotalTypedStrokes = newTotalTyped
            )
            val finalMax = maxOf(maxCpm, finalEval.cpm)
            val durSec = (elapsedMillis / 1000L).toInt().coerceAtLeast(1)
            onSaveRecord(
                TypingMode.POSITION,
                currentStage.title,
                finalEval.cpm,
                finalMax,
                finalEval.accuracyPercent,
                targetItems.size,
                durSec
            )
            showResultDialog = true
        } else {
            currentIndex++
            currentInput = ""
        }
    }

    // 목표 글자와 정확히 일치하면 짧은 딜레이 후 자동으로 다음 글자로 이동
    LaunchedEffect(currentInput, currentTarget) {
        if (currentTarget.isNotEmpty() && currentInput == currentTarget && !showResultDialog) {
            delay(140L)
            advanceOrFinish()
        }
    }

    fun resetSession() {
        targetItems = currentStage.items.shuffled().take(25)
        currentIndex = 0
        currentInput = ""
        startTimeMillis = 0L
        elapsedMillis = 0L
        completedStrokes = 0
        totalTypedStrokes = 0
        maxCpm = 0
        showResultDialog = false
    }

    fun onInputChanged(newText: String) {
        if (startTimeMillis == 0L && newText.isNotEmpty()) {
            startTimeMillis = System.currentTimeMillis()
        }
        val clean = newText.replace("\n", "")
        currentInput = clean
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("자리 연습", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                },
                actions = {
                    IconButton(onClick = { resetSession() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "초기화")
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
            // 단계 선택 칩
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                stages.forEachIndexed { idx, stage ->
                    FilterChip(
                        selected = selectedStageIdx == idx,
                        onClick = { selectedStageIdx = idx },
                        label = { Text("${stage.id}단계") }
                    )
                }
            }

            Text(
                text = "${currentStage.title} (${currentStage.subtitle})",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )

            TypingTopMetricsBar(
                cpm = evaluation.cpm,
                maxCpm = maxCpm,
                accuracy = evaluation.accuracyPercent,
                elapsedSeconds = (elapsedMillis / 1000L).toInt(),
                progressText = "${currentIndex + 1}/${targetItems.size}",
                progressFraction = (currentIndex.toFloat()) / targetItems.size.coerceAtLeast(1)
            )

            // 메인 글자 디스플레이 카드
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
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 이전 / 현재 / 다음 글자 미리보기
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val prevChar = targetItems.getOrNull(currentIndex - 1) ?: "-"
                        val nextChar = targetItems.getOrNull(currentIndex + 1) ?: "-"

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 18.dp, vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = prevChar,
                                fontSize = 24.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f))
                                .padding(horizontal = 36.dp, vertical = 18.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            ColoredTargetText(
                                target = currentTarget,
                                statuses = evaluation.charStatuses,
                                fontSize = 54.sp,
                                textAlign = TextAlign.Center
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 18.dp, vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = nextChar,
                                fontSize = 24.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Text(
                        text = if (currentInput.isEmpty()) "아래 키보드를 누르거나 입력창에 타이핑하세요" else "입력 중: $currentInput",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 입력 방식 토글 & 외부/시스템 키보드 입력창
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "내장 두벌식 터치 키보드 & 손가락 가이드 표시",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Switch(
                    checked = useBuiltInKeyboard,
                    onCheckedChange = { useBuiltInKeyboard = it }
                )
            }

            OutlinedTextField(
                value = currentInput,
                onValueChange = { onInputChanged(it) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("블루투스 키보드 또는 스마트폰 키보드 입력창") },
                placeholder = { Text("여기를 눌러 스마트폰 키보드 또는 외부 키보드로 입력") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { advanceOrFinish() })
            )

            if (useBuiltInKeyboard) {
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
                        if (currentInput.isNotEmpty()) advanceOrFinish()
                    },
                    onEnterClick = {
                        if (currentInput.isNotEmpty()) advanceOrFinish()
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
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
            title = "자리 연습 완료!",
            subtitle = currentStage.title,
            avgCpm = finalEval.cpm,
            maxCpm = maxOf(maxCpm, finalEval.cpm),
            accuracy = finalEval.accuracyPercent,
            durationSec = (elapsedMillis / 1000L).toInt().coerceAtLeast(1),
            onRetry = { resetSession() },
            onDismissToHome = onBack
        )
    }
}
