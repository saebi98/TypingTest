package com.typingtest.korean.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.ShortText
import androidx.compose.material.icons.filled.Spellcheck
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.typingtest.korean.data.DashboardSummary
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    summary: DashboardSummary,
    onNavigatePosition: () -> Unit,
    onNavigateWord: () -> Unit,
    onNavigateShortText: () -> Unit,
    onNavigateLongText: () -> Unit,
    onNavigateAcidRain: () -> Unit,
    onNavigateStats: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Keyboard,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "한글 타자 마스터",
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
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
            // 상단 내 타자 요약 히어로 카드
            HeroSummaryBanner(
                summary = summary,
                onClickStats = onNavigateStats
            )

            Text(
                text = "단계별 타자 연습",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp)
            )

            // 2x2 연습 모드 그리드
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ModeCard(
                    title = "자리 연습",
                    subtitle = "자음·모음 손가락 위치 기초",
                    badge = "4단계 과정",
                    icon = Icons.Default.Keyboard,
                    accentColor = Color(0xFF2563EB),
                    onClick = onNavigatePosition,
                    modifier = Modifier.weight(1f)
                )
                ModeCard(
                    title = "낱말 연습",
                    subtitle = "초·중·고급 어휘 및 겹받침",
                    badge = "120+ 단어",
                    icon = Icons.Default.Spellcheck,
                    accentColor = Color(0xFF059669),
                    onClick = onNavigateWord,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ModeCard(
                    title = "짧은 글 연습",
                    subtitle = "속담·명언·아름다운 우리말",
                    badge = "실전 문장",
                    icon = Icons.Default.ShortText,
                    accentColor = Color(0xFFD97706),
                    onClick = onNavigateShortText,
                    modifier = Modifier.weight(1f)
                )
                ModeCard(
                    title = "긴 글 연습",
                    subtitle = "별 헤는 밤·소나기 등 명작",
                    badge = "문학 5선",
                    icon = Icons.Default.MenuBook,
                    accentColor = Color(0xFF7C3AED),
                    onClick = onNavigateLongText,
                    modifier = Modifier.weight(1f)
                )
            }

            Text(
                text = "타자 게임 & 내 기록",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp)
            )

            // 산성비 타자 디펜스 게임 배너
            WideFeatureCard(
                title = "산성비 타자 디펜스 게임",
                subtitle = "하늘에서 내려오는 한글 단어를 바닥에 닿기 전에 빠르게 입력해 제거하세요! (특수 아이템 단어 포함)",
                badgeText = if (summary.bestAcidRainScore > 0) "최고 점수 ${summary.bestAcidRainScore}점" else "NEW 미니게임",
                icon = Icons.Default.SportsEsports,
                gradientColors = listOf(Color(0xFF0F766E), Color(0xFF0284C7)),
                onClick = onNavigateAcidRain
            )

            // 통계 및 히스토리 카드
            WideFeatureCard(
                title = "내 타자 통계 & 히스토리",
                subtitle = "모드별 평균 타수, 최고 타수 추이 그래프, 최근 연습 기록을 한눈에 확인합니다.",
                badgeText = "총 ${summary.totalSessions}회 기록",
                icon = Icons.Default.Analytics,
                gradientColors = listOf(Color(0xFF334155), Color(0xFF1E293B)),
                onClick = onNavigateStats
            )

            // 하단 입력 환경 안내 팁
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Article,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "스마트폰 터치 키보드(쿼티·천지인)와 외부 블루투스 물리 키보드를 모두 지원합니다. 자리 연습에서는 내장 두벌식 가상 키보드로 직접 터치 연습도 가능합니다.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun HeroSummaryBanner(
    summary: DashboardSummary,
    onClickStats: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF1E40AF), Color(0xFF3B82F6))
                )
            )
            .clickable(onClick = onClickStats)
            .padding(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "나의 한글 타자 실력 요약",
                        color = Color.White.copy(alpha = 0.85f),
                        style = MaterialTheme.typography.labelLarge
                    )
                    Text(
                        text = if (summary.overallAvgCpm > 0) "${summary.overallAvgCpm} 타/분" else "연습을 시작해보세요!",
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.2f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "통계 상세 보기 →",
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                HeroStatBox("최고 타수", "${summary.overallBestCpm}타")
                HeroStatBox("평균 정확도", if (summary.totalSessions > 0) "${summary.overallAvgAccuracy.roundToInt()}%" else "-")
                HeroStatBox("누적 연습", "${summary.totalSessions}회")
                HeroStatBox("산성비 최고점", "${summary.bestAcidRainScore}점")
            }
        }
    }
}

@Composable
private fun HeroStatBox(label: String, value: String) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.75f),
            style = MaterialTheme.typography.labelSmall
        )
        Text(
            text = value,
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ModeCard(
    title: String,
    subtitle: String,
    badge: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = accentColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(accentColor.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = badge,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                }
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                minLines = 2
            )
        }
    }
}

@Composable
private fun WideFeatureCard(
    title: String,
    subtitle: String,
    badgeText: String,
    icon: ImageVector,
    gradientColors: List<Color>,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.horizontalGradient(gradientColors))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = title,
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.White.copy(alpha = 0.22f))
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = badgeText,
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        text = subtitle,
                        color = Color.White.copy(alpha = 0.88f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
