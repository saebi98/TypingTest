package com.typingtest.korean.ui.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.typingtest.korean.data.DashboardSummary
import com.typingtest.korean.data.TypingMode
import com.typingtest.korean.data.TypingRecord
import com.typingtest.korean.ui.components.formatDuration
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    summary: DashboardSummary,
    onClearRecords: () -> Unit,
    onBack: () -> Unit
) {
    var showConfirmReset by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("내 타자 통계 & 히스토리", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                },
                actions = {
                    if (summary.totalSessions > 0) {
                        IconButton(onClick = { showConfirmReset = true }) {
                            Icon(Icons.Default.DeleteForever, contentDescription = "기록 초기화")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 종합 요약 카드
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "종합 타자 실력 지표",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        SummaryMetric("평균 타수", "${summary.overallAvgCpm}타/분")
                        SummaryMetric("최고 타수", "${summary.overallBestCpm}타/분")
                        SummaryMetric(
                            "평균 정확도",
                            if (summary.totalSessions > 0) "${summary.overallAvgAccuracy.roundToInt()}%" else "-"
                        )
                        SummaryMetric("총 연습시간", formatDuration(summary.totalDurationSec))
                    }
                }
            }

            // 최근 타수 변화 추이 그래프
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "최근 연습 타수(타/분) 변화 추이",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    val chartRecords = summary.recentRecords.take(15).reversed()
                    if (chartRecords.size < 2) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "연습 기록이 2회 이상 쌓이면 타수 추이 그래프가 표시됩니다.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        CpmLineChart(
                            records = chartRecords,
                            lineColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                        )
                    }
                }
            }

            // 모드별 상세 통계
            Text(
                text = "모드별 상세 성적",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            TypingMode.entries.forEach { mode ->
                val modeStat = summary.modeSummaries[mode]
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = mode.label,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (modeStat != null) "총 ${modeStat.sessionCount}회 완료" else "기록 없음",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (modeStat != null) {
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                if (mode == TypingMode.ACID_RAIN) {
                                    SummaryMetric("최고 점수", "${modeStat.bestScore}점")
                                }
                                SummaryMetric("평균 타수", "${modeStat.avgCpm}타")
                                SummaryMetric("최고 타수", "${modeStat.bestCpm}타")
                            }
                        }
                    }
                }
            }

            // 최근 연습 기록 목록
            Text(
                text = "최근 연습 히스토리",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (summary.recentRecords.isEmpty()) {
                Text(
                    text = "아직 저장된 타자 연습 기록이 없습니다.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                summary.recentRecords.forEach { record ->
                    HistoryRecordRow(record)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showConfirmReset) {
        AlertDialog(
            onDismissRequest = { showConfirmReset = false },
            title = { Text("모든 기록 초기화", fontWeight = FontWeight.Bold) },
            text = { Text("저장된 모든 타자 연습 기록과 산성비 점수를 삭제하시겠습니까?") },
            confirmButton = {
                Button(
                    onClick = {
                        onClearRecords()
                        showConfirmReset = false
                    }
                ) {
                    Text("초기화")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showConfirmReset = false }) {
                    Text("취소")
                }
            }
        )
    }
}

@Composable
private fun SummaryMetric(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun CpmLineChart(
    records: List<TypingRecord>,
    lineColor: Color,
    modifier: Modifier = Modifier
) {
    val maxVal = (records.maxOfOrNull { it.avgCpm } ?: 300).coerceAtLeast(100).toFloat()
    val minVal = 0f

    Canvas(modifier = modifier.padding(horizontal = 8.dp, vertical = 12.dp)) {
        val w = size.width
        val h = size.height
        val stepX = if (records.size > 1) w / (records.size - 1) else w

        val points = records.mapIndexed { idx, rec ->
            val ratio = ((rec.avgCpm - minVal) / (maxVal - minVal)).coerceIn(0f, 1f)
            Offset(x = idx * stepX, y = h - (ratio * h))
        }

        val path = Path().apply {
            points.forEachIndexed { idx, pt ->
                if (idx == 0) moveTo(pt.x, pt.y) else lineTo(pt.x, pt.y)
            }
        }

        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 6f)
        )

        points.forEach { pt ->
            drawCircle(
                color = lineColor,
                radius = 9f,
                center = pt
            )
            drawCircle(
                color = Color.White,
                radius = 4f,
                center = pt
            )
        }
    }
}

@Composable
private fun HistoryRecordRow(record: TypingRecord) {
    val dateFormat = remember { SimpleDateFormat("MM/dd HH:mm", Locale.KOREA) }
    val dateStr = dateFormat.format(Date(record.timestampMillis))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "[${record.mode.label}] ${record.detailTitle}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "$dateStr · 소요시간 ${formatDuration(record.durationSec)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = if (record.mode == TypingMode.ACID_RAIN) {
                    "${record.score}점 (${record.avgCpm}타/분)"
                } else {
                    "${record.avgCpm} 타/분"
                },
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "최고 ${record.maxCpm}타 · 정확도 ${record.accuracy.roundToInt()}%",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
