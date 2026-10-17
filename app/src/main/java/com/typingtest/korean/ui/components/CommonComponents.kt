package com.typingtest.korean.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.typingtest.korean.data.TypingLanguage
import com.typingtest.korean.engine.CharMatchStatus
import kotlin.math.roundToInt

@Composable
fun LanguageSelectorRow(
    selectedLanguage: TypingLanguage,
    onSelectLanguage: (TypingLanguage) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        TypingLanguage.entries.forEach { lang ->
            val isSelected = selectedLanguage == lang
            Button(
                onClick = { onSelectLanguage(lang) },
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp),
                shape = RoundedCornerShape(10.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text(
                    text = "${lang.flagEmoji} ${lang.label}",
                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun TypingTopMetricsBar(
    cpm: Int,
    maxCpm: Int,
    accuracy: Float,
    elapsedSeconds: Int,
    progressText: String,
    progressFraction: Float,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        )
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricItem(label = "현재 타수", value = "${cpm}타/분", highlight = true)
                MetricItem(label = "최고 타수", value = "${maxCpm}타")
                MetricItem(label = "정확도", value = "${accuracy.roundToInt()}%")
                MetricItem(label = "진행", value = progressText)
                MetricItem(label = "시간", value = formatDuration(elapsedSeconds))
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progressFraction.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
            )
        }
    }
}

@Composable
private fun MetricItem(
    label: String,
    value: String,
    highlight: Boolean = false
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = if (highlight) 15.sp else 14.sp
            ),
            color = if (highlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun ColoredTargetText(
    target: String,
    statuses: List<CharMatchStatus>,
    fontSize: TextUnit = 22.sp,
    textAlign: TextAlign = TextAlign.Start,
    modifier: Modifier = Modifier
) {
    val matchedColor = Color(0xFF059669)
    val composingColor = Color(0xFF2563EB)
    val mismatchColor = Color(0xFFDC2626)
    val pendingColor = MaterialTheme.colorScheme.onSurface

    val annotated = buildAnnotatedString {
        for (i in target.indices) {
            val ch = target[i]
            val status = statuses.getOrElse(i) { CharMatchStatus.PENDING }
            when (status) {
                CharMatchStatus.MATCHED -> {
                    withStyle(
                        SpanStyle(
                            color = matchedColor,
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append(ch)
                    }
                }
                CharMatchStatus.COMPOSING -> {
                    withStyle(
                        SpanStyle(
                            color = composingColor,
                            background = composingColor.copy(alpha = 0.15f),
                            fontWeight = FontWeight.ExtraBold,
                            textDecoration = TextDecoration.Underline
                        )
                    ) {
                        append(ch)
                    }
                }
                CharMatchStatus.MISMATCH -> {
                    withStyle(
                        SpanStyle(
                            color = mismatchColor,
                            background = mismatchColor.copy(alpha = 0.18f),
                            fontWeight = FontWeight.Bold,
                            textDecoration = TextDecoration.Underline
                        )
                    ) {
                        append(ch)
                    }
                }
                CharMatchStatus.PENDING -> {
                    withStyle(
                        SpanStyle(
                            color = pendingColor
                        )
                    ) {
                        append(ch)
                    }
                }
            }
        }
    }

    Text(
        text = annotated,
        fontSize = fontSize,
        lineHeight = fontSize * 1.4f,
        textAlign = textAlign,
        modifier = modifier
    )
}

@Composable
fun PracticeResultDialog(
    title: String,
    subtitle: String,
    avgCpm: Int,
    maxCpm: Int,
    accuracy: Float,
    durationSec: Int,
    extraScoreLabel: String? = null,
    extraScoreValue: String? = null,
    onRetry: () -> Unit,
    onDismissToHome: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissToHome,
        title = {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ResultStatRow("평균 타수", "${avgCpm} 타/분", highlight = true)
                ResultStatRow("최고 타수", "${maxCpm} 타/분")
                ResultStatRow("정확도", "${String.format("%.1f", accuracy)} %")
                ResultStatRow("소요 시간", formatDuration(durationSec))
                if (extraScoreLabel != null && extraScoreValue != null) {
                    ResultStatRow(extraScoreLabel, extraScoreValue, highlight = true)
                }
            }
        },
        confirmButton = {
            Button(onClick = onRetry) {
                Text("다시 연습하기")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismissToHome) {
                Text("홈으로")
            }
        }
    )
}

@Composable
private fun ResultStatRow(label: String, value: String, highlight: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (highlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

fun formatDuration(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%02d:%02d".format(m, s)
}
