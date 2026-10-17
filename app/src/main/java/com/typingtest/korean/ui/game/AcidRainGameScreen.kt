package com.typingtest.korean.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.typingtest.korean.data.ContentRepository
import com.typingtest.korean.data.TypingMode
import com.typingtest.korean.engine.HangulEngine
import com.typingtest.korean.ui.components.PracticeResultDialog
import kotlinx.coroutines.delay
import kotlin.math.roundToInt
import kotlin.random.Random

private enum class RainItemType(val badge: String, val bgColor: Color, val description: String) {
    NORMAL("", Color(0xFF1E293B), "일반 단어"),
    CLEAR_ALL("🧹전체청소", Color(0xFF7C3AED), "화면 모든 단어 제거"),
    SLOW_DOWN("❄️감속", Color(0xFF0284C7), "6초간 낙하 속도 50% 감소"),
    HEAL("❤️회복", Color(0xFF059669), "생명력 +1 회복")
}

private data class FallingWord(
    val id: Long,
    val text: String,
    val xFraction: Float, // 0.05f .. 0.72f
    val yFraction: Float, // 0.0f .. 1.0f
    val speedMultiplier: Float,
    val itemType: RainItemType
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcidRainGameScreen(
    onSaveRecord: (TypingMode, String, Int, Int, Float, Int, Int) -> Unit,
    onBack: () -> Unit
) {
    val wordPool = remember { ContentRepository.acidRainWordPool }
    val fallingWords = remember { mutableStateListOf<FallingWord>() }

    var isPlaying by remember { mutableStateOf(true) }
    var isGameOver by remember { mutableStateOf(false) }

    var score by remember { mutableIntStateOf(0) }
    var clearedCount by remember { mutableIntStateOf(0) }
    var lives by remember { mutableIntStateOf(5) }
    var level by remember { mutableIntStateOf(1) }

    var currentInput by remember { mutableStateOf("") }
    var statusBanner by remember { mutableStateOf("하늘에서 떨어지는 단어를 입력해 산성비를 막으세요!") }
    var slowExpireMillis by remember { mutableLongStateOf(0L) }

    var totalMatchedStrokes by remember { mutableIntStateOf(0) }
    var elapsedMillis by remember { mutableLongStateOf(0L) }
    var maxCpm by remember { mutableIntStateOf(0) }
    var nextWordId by remember { mutableLongStateOf(1L) }

    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        runCatching { focusRequester.requestFocus() }
    }

    val currentCpm = remember(totalMatchedStrokes, elapsedMillis) {
        if (elapsedMillis < 1000L || totalMatchedStrokes == 0) 0
        else ((totalMatchedStrokes * 60_000.0) / elapsedMillis.toDouble()).roundToInt().coerceAtMost(1500)
    }

    LaunchedEffect(currentCpm) {
        if (currentCpm > maxCpm) maxCpm = currentCpm
    }

    fun triggerGameOver() {
        if (isGameOver) return
        isGameOver = true
        isPlaying = false
        val durSec = (elapsedMillis / 1000L).toInt().coerceAtLeast(1)
        onSaveRecord(
            TypingMode.ACID_RAIN,
            "산성비 Lv.$level (${clearedCount}단어)",
            currentCpm,
            maxOf(maxCpm, currentCpm),
            100f,
            score,
            durSec
        )
    }

    fun resetGame() {
        fallingWords.clear()
        score = 0
        clearedCount = 0
        lives = 5
        level = 1
        currentInput = ""
        slowExpireMillis = 0L
        totalMatchedStrokes = 0
        elapsedMillis = 0L
        maxCpm = 0
        isGameOver = false
        isPlaying = true
        statusBanner = "산성비 방어 작전을 시작합니다!"
    }

    // 메인 게임 루프 (40ms 틱 = 25 FPS)
    LaunchedEffect(isPlaying, isGameOver) {
        var spawnTimerMs = 1200L
        while (isPlaying && !isGameOver) {
            val tickMs = 40L
            delay(tickMs)
            elapsedMillis += tickMs
            spawnTimerMs += tickMs

            val now = System.currentTimeMillis()
            val isSlowActive = now < slowExpireMillis
            val baseFallStep = (0.0024f + (level - 1) * 0.00045f) * (if (isSlowActive) 0.45f else 1.0f)

            // 1. 기존 단어들 낙하 이동 및 바닥 충돌 체크
            val updated = mutableListOf<FallingWord>()
            var lostLifeCount = 0
            for (fw in fallingWords) {
                val nextY = fw.yFraction + baseFallStep * fw.speedMultiplier
                if (nextY >= 0.96f) {
                    lostLifeCount++
                } else {
                    updated.add(fw.copy(yFraction = nextY))
                }
            }
            fallingWords.clear()
            fallingWords.addAll(updated)

            if (lostLifeCount > 0) {
                lives = (lives - lostLifeCount).coerceAtLeast(0)
                statusBanner = "⚠️ 단어가 산성 호수에 빠졌습니다! (남은 생명: $lives)"
                if (lives <= 0) {
                    triggerGameOver()
                    break
                }
            }

            // 2. 신규 단어 스폰
            val spawnIntervalMs = (1950L - (level - 1) * 140L).coerceAtLeast(700L)
            if (spawnTimerMs >= spawnIntervalMs && fallingWords.size < 9) {
                spawnTimerMs = 0L
                val activeTexts = fallingWords.map { it.text }.toSet()
                val candidate = wordPool.filterNot { activeTexts.contains(it) }.randomOrNull() ?: wordPool.random()

                val roll = Random.nextInt(100)
                val itemType = when {
                    roll < 7 -> RainItemType.CLEAR_ALL
                    roll < 16 -> RainItemType.SLOW_DOWN
                    roll < 24 -> RainItemType.HEAL
                    else -> RainItemType.NORMAL
                }

                fallingWords.add(
                    FallingWord(
                        id = nextWordId++,
                        text = candidate,
                        xFraction = Random.nextFloat() * 0.64f + 0.04f,
                        yFraction = 0.02f,
                        speedMultiplier = Random.nextFloat() * 0.3f + 0.85f,
                        itemType = itemType
                    )
                )
            }
        }
    }

    fun tryMatchWord(candidateInput: String): Boolean {
        val trimmed = candidateInput.trim()
        if (trimmed.isEmpty()) return false

        // 가장 아래쪽에 있는 일치 단어를 우선 타격
        val targetWord = fallingWords
            .filter { it.text == trimmed }
            .maxByOrNull { it.yFraction } ?: return false

        fallingWords.remove(targetWord)
        val strokes = HangulEngine.countStrokes(targetWord.text)
        totalMatchedStrokes += strokes
        clearedCount++
        score += (strokes * 15) + (level * 20)
        level = (1 + clearedCount / 8).coerceAtMost(10)

        when (targetWord.itemType) {
            RainItemType.NORMAL -> {
                statusBanner = "🎯 '${targetWord.text}' 격추! (+${strokes * 15 + level * 20}점)"
            }
            RainItemType.CLEAR_ALL -> {
                val bonusCount = fallingWords.size
                fallingWords.clear()
                score += bonusCount * 35
                statusBanner = "🧹 특수 아이템 발동! 화면의 단어 ${bonusCount}개 전체 청소!"
            }
            RainItemType.SLOW_DOWN -> {
                slowExpireMillis = System.currentTimeMillis() + 6000L
                statusBanner = "❄️ 특수 아이템 발동! 6초간 산성비 낙하 속도 50% 감속!"
            }
            RainItemType.HEAL -> {
                lives = (lives + 1).coerceAtMost(5)
                statusBanner = "❤️ 특수 아이템 발동! 생명력 +1 회복 (현재 $lives)"
            }
        }
        return true
    }

    fun onInputChanged(raw: String) {
        val cleaned = raw.replace("\n", "").trimStart()
        currentInput = cleaned
        if (tryMatchWord(cleaned)) {
            currentInput = ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("산성비 타자 디펜스", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                },
                actions = {
                    IconButton(onClick = { isPlaying = !isPlaying }) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "일시정지/재개"
                        )
                    }
                    IconButton(onClick = { resetGame() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "다시 시작")
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
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 상단 게임 상태 바
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Lv.$level",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "점수: ${score}점",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${currentCpm}타/분",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "❤️".repeat(lives) + "🖤".repeat((5 - lives).coerceAtLeast(0)),
                    fontSize = 14.sp
                )
            }

            // 이벤트 배너
            Text(
                text = statusBanner,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            // 산성비 낙하 게임 월드 (BoxWithConstraints로 반응형 좌표 매핑)
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF134E4A))
                        )
                    )
                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(18.dp))
            ) {
                val arenaWidth = maxWidth
                val arenaHeight = maxHeight

                // 떨어지는 단어 렌더링
                fallingWords.forEach { fw ->
                    val xOffset = arenaWidth * fw.xFraction
                    val yOffset = (arenaHeight - 36.dp) * fw.yFraction
                    val isPartiallyMatching = currentInput.isNotBlank() && fw.text.startsWith(currentInput.trim())

                    Box(
                        modifier = Modifier
                            .offset(x = xOffset, y = yOffset)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isPartiallyMatching) Color(0xFFF59E0B)
                                else fw.itemType.bgColor.copy(alpha = 0.92f)
                            )
                            .border(
                                width = if (isPartiallyMatching || fw.itemType != RainItemType.NORMAL) 1.5.dp else 1.dp,
                                color = if (isPartiallyMatching) Color.White else Color(0xFF64748B),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (fw.itemType.badge.isNotEmpty()) {
                                Text(
                                    text = "${fw.itemType.badge} ",
                                    fontSize = 11.sp,
                                    color = Color(0xFFFDE047),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = fw.text,
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // 하단 산성 호수 경계선
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(26.dp)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFF059669), Color(0xFF10B981), Color(0xFF047857))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "〰️ 산성 호수 위험 구역 (단어가 닿으면 생명력이 감소합니다) 〰️",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // 하단 입력 바 (일치하는 순간 즉시 자동 격추 + 지우기 버튼)
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
                    label = { Text("단어 입력 (일치 시 자동 격추 / Enter)") },
                    trailingIcon = {
                        if (currentInput.isNotEmpty()) {
                            IconButton(onClick = { currentInput = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "입력 지우기")
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            tryMatchWord(currentInput)
                            currentInput = ""
                        }
                    )
                )

                Button(
                    onClick = {
                        tryMatchWord(currentInput)
                        currentInput = ""
                    },
                    modifier = Modifier.height(56.dp)
                ) {
                    Text("격추")
                }
            }
        }
    }

    if (isGameOver) {
        PracticeResultDialog(
            title = "산성비 게임 종료!",
            subtitle = "도달 레벨: Lv.$level · 제거한 단어: ${clearedCount}개",
            avgCpm = currentCpm,
            maxCpm = maxOf(maxCpm, currentCpm),
            accuracy = 100f,
            durationSec = (elapsedMillis / 1000L).toInt().coerceAtLeast(1),
            extraScoreLabel = "최종 획득 점수",
            extraScoreValue = "${score}점",
            onRetry = { resetGame() },
            onDismissToHome = onBack
        )
    }
}
