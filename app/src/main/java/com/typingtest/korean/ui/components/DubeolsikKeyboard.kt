package com.typingtest.korean.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.typingtest.korean.engine.HangulEngine

private data class KeySpec(
    val normal: Char,
    val shifted: Char? = null,
    val isLeftHand: Boolean,
    val fingerLabel: String,
    val isHomeKey: Boolean = false
)

private val ROW_TOP = listOf(
    KeySpec('ㅂ', 'ㅃ', isLeftHand = true, fingerLabel = "왼소지"),
    KeySpec('ㅈ', 'ㅉ', isLeftHand = true, fingerLabel = "왼약지"),
    KeySpec('ㄷ', 'ㄸ', isLeftHand = true, fingerLabel = "왼중지"),
    KeySpec('ㄱ', 'ㄲ', isLeftHand = true, fingerLabel = "왼검지"),
    KeySpec('ㅅ', 'ㅆ', isLeftHand = true, fingerLabel = "왼검지"),
    KeySpec('ㅛ', null, isLeftHand = false, fingerLabel = "오른검지"),
    KeySpec('ㅕ', null, isLeftHand = false, fingerLabel = "오른검지"),
    KeySpec('ㅑ', null, isLeftHand = false, fingerLabel = "오른중지"),
    KeySpec('ㅐ', 'ㅒ', isLeftHand = false, fingerLabel = "오른약지"),
    KeySpec('ㅔ', 'ㅖ', isLeftHand = false, fingerLabel = "오른소지")
)

private val ROW_MIDDLE = listOf(
    KeySpec('ㅁ', null, isLeftHand = true, fingerLabel = "왼소지", isHomeKey = true),
    KeySpec('ㄴ', null, isLeftHand = true, fingerLabel = "왼약지", isHomeKey = true),
    KeySpec('ㅇ', null, isLeftHand = true, fingerLabel = "왼중지", isHomeKey = true),
    KeySpec('ㄹ', null, isLeftHand = true, fingerLabel = "왼검지", isHomeKey = true),
    KeySpec('ㅎ', null, isLeftHand = true, fingerLabel = "왼검지"),
    KeySpec('ㅗ', null, isLeftHand = false, fingerLabel = "오른검지"),
    KeySpec('ㅓ', null, isLeftHand = false, fingerLabel = "오른검지", isHomeKey = true),
    KeySpec('ㅏ', null, isLeftHand = false, fingerLabel = "오른중지", isHomeKey = true),
    KeySpec('ㅣ', null, isLeftHand = false, fingerLabel = "오른약지", isHomeKey = true)
)

private val ROW_BOTTOM = listOf(
    KeySpec('ㅋ', null, isLeftHand = true, fingerLabel = "왼소지"),
    KeySpec('ㅌ', null, isLeftHand = true, fingerLabel = "왼약지"),
    KeySpec('ㅊ', null, isLeftHand = true, fingerLabel = "왼중지"),
    KeySpec('ㅍ', null, isLeftHand = true, fingerLabel = "왼검지"),
    KeySpec('ㅠ', null, isLeftHand = false, fingerLabel = "오른검지"),
    KeySpec('ㅜ', null, isLeftHand = false, fingerLabel = "오른검지"),
    KeySpec('ㅡ', null, isLeftHand = false, fingerLabel = "오른중지")
)

/**
 * 두벌식 한글 가상 키보드 & 손가락 가이드 컴포넌트
 * - 터치 시 직접 자모 입력 가능 (`interactive = true`)
 * - 외부 블루투스 키보드 사용 시 다음 눌러야 할 자모와 손가락 위치 가이드 표시
 */
@Composable
fun DubeolsikKeyboard(
    nextRequiredJamo: Char?,
    requiresShift: Boolean,
    interactive: Boolean = true,
    showBottomActionRow: Boolean = true,
    onJamoClick: (Char) -> Unit = {},
    onBackspaceClick: () -> Unit = {},
    onSpaceClick: () -> Unit = {},
    onEnterClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var manualShift by remember { mutableStateOf(false) }
    val isShifted = manualShift || requiresShift
    val baseTargetKey = nextRequiredJamo?.let { HangulEngine.getBaseKeyForJamo(it) }

    val targetKeySpec = (ROW_TOP + ROW_MIDDLE + ROW_BOTTOM).find { it.normal == baseTargetKey }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f),
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // 상단 손가락 가이드 배너
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (nextRequiredJamo != null) {
                        buildString {
                            append("다음 입력 키: [ ")
                            if (requiresShift) append("Shift + ")
                            append(nextRequiredJamo)
                            append(" ]")
                        }
                    } else {
                        "두벌식 표준 자판 가이드"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                if (targetKeySpec != null) {
                    Text(
                        text = "추천 손가락: ${targetKeySpec.fingerLabel}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (targetKeySpec.isLeftHand) Color(0xFF2563EB) else Color(0xFF059669)
                    )
                }
            }

            // 1열 (ㅂ ㅈ ㄷ ㄱ ㅅ ㅛ ㅕ ㅑ ㅐ ㅔ)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ROW_TOP.forEach { spec ->
                    val displayChar = if (isShifted && spec.shifted != null) spec.shifted else spec.normal
                    val isHighlighted = baseTargetKey == spec.normal
                    KeyButton(
                        mainChar = displayChar,
                        subChar = if (!isShifted) spec.shifted else spec.normal,
                        isHighlighted = isHighlighted,
                        isLeftHand = spec.isLeftHand,
                        isHomeKey = spec.isHomeKey,
                        interactive = interactive,
                        onClick = {
                            onJamoClick(displayChar)
                            manualShift = false
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 2열 (ㅁ ㄴ ㅇ ㄹ ㅎ ㅗ ㅓ ㅏ ㅣ)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ROW_MIDDLE.forEach { spec ->
                    val isHighlighted = baseTargetKey == spec.normal
                    KeyButton(
                        mainChar = spec.normal,
                        subChar = null,
                        isHighlighted = isHighlighted,
                        isLeftHand = spec.isLeftHand,
                        isHomeKey = spec.isHomeKey,
                        interactive = interactive,
                        onClick = {
                            onJamoClick(spec.normal)
                            manualShift = false
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 3열 (Shift + ㅋ ㅌ ㅊ ㅍ ㅠ ㅜ ㅡ + ⌫)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ActionKeyButton(
                    label = "⇧ 쌍자음",
                    isHighlighted = requiresShift || manualShift,
                    interactive = interactive,
                    onClick = { manualShift = !manualShift },
                    modifier = Modifier.weight(1.45f)
                )
                ROW_BOTTOM.forEach { spec ->
                    val isHighlighted = baseTargetKey == spec.normal
                    KeyButton(
                        mainChar = spec.normal,
                        subChar = null,
                        isHighlighted = isHighlighted,
                        isLeftHand = spec.isLeftHand,
                        isHomeKey = spec.isHomeKey,
                        interactive = interactive,
                        onClick = {
                            onJamoClick(spec.normal)
                            manualShift = false
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
                ActionKeyButton(
                    label = "⌫",
                    isHighlighted = false,
                    interactive = interactive,
                    onClick = onBackspaceClick,
                    modifier = Modifier.weight(1.35f)
                )
            }

            if (showBottomActionRow) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ActionKeyButton(
                        label = "간격 (Space)",
                        isHighlighted = nextRequiredJamo == ' ',
                        interactive = interactive,
                        onClick = onSpaceClick,
                        modifier = Modifier.weight(3f)
                    )
                    ActionKeyButton(
                        label = "다음 / 완료 ↵",
                        isHighlighted = false,
                        interactive = interactive,
                        onClick = onEnterClick,
                        modifier = Modifier.weight(1.4f)
                    )
                }
            }
        }
    }
}

@Composable
private fun KeyButton(
    mainChar: Char,
    subChar: Char?,
    isHighlighted: Boolean,
    isLeftHand: Boolean,
    isHomeKey: Boolean,
    interactive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = when {
        isHighlighted -> Color(0xFFF59E0B)
        isLeftHand -> MaterialTheme.colorScheme.surface
        else -> MaterialTheme.colorScheme.surface
    }
    val borderColor = when {
        isHighlighted -> Color(0xFFD97706)
        isLeftHand -> Color(0xFF3B82F6).copy(alpha = 0.35f)
        else -> Color(0xFF10B981).copy(alpha = 0.35f)
    }
    val textColor = if (isHighlighted) Color.White else MaterialTheme.colorScheme.onSurface

    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(width = if (isHighlighted) 2.dp else 1.dp, color = borderColor, shape = RoundedCornerShape(8.dp))
            .then(if (interactive) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = 3.dp, horizontal = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        if (subChar != null) {
            Text(
                text = subChar.toString(),
                fontSize = 9.sp,
                color = if (isHighlighted) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 3.dp)
            )
        }

        Text(
            text = mainChar.toString(),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )

        if (isHomeKey) {
            Spacer(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(if (isHighlighted) Color.White else MaterialTheme.colorScheme.primary)
            )
        }
    }
}

@Composable
private fun ActionKeyButton(
    label: String,
    isHighlighted: Boolean,
    interactive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = if (isHighlighted) {
        Color(0xFFF59E0B)
    } else {
        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
    }
    val textColor = if (isHighlighted) {
        Color.White
    } else {
        MaterialTheme.colorScheme.onSecondaryContainer
    }

    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .then(if (interactive) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}
