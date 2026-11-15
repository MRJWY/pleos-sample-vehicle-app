package ai.pleos.playground.sample.vehicle.community

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

internal val MaruInk = Color(0xFF183632)
internal val MaruMuted = Color(0xFF6C7C76)
internal val MaruMint = Color(0xFFB6F36B)
internal val MaruPaper = Color(0xFFF4F5EF)
internal val MaruLine = Color(0xFFE0E7DC)

@Composable
internal fun CommunityTheme(content: @Composable () -> Unit) {
    val base = MaterialTheme.typography
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = MaruInk, onPrimary = Color.White, secondary = Color(0xFF46754C),
            background = MaruPaper, surface = Color.White, onSurface = MaruInk,
            onSurfaceVariant = MaruMuted, outline = MaruLine
        ),
        typography = base.copy(
            headlineLarge = base.headlineLarge.copy(fontSize = 36.sp, lineHeight = 43.sp, fontWeight = FontWeight.Bold),
            headlineMedium = base.headlineMedium.copy(fontSize = 28.sp, lineHeight = 36.sp, fontWeight = FontWeight.Bold),
            titleLarge = base.titleLarge.copy(fontSize = 21.sp, lineHeight = 28.sp, fontWeight = FontWeight.Bold),
            titleMedium = base.titleMedium.copy(fontSize = 17.sp, lineHeight = 24.sp, fontWeight = FontWeight.SemiBold),
            bodyLarge = base.bodyLarge.copy(fontSize = 17.sp, lineHeight = 26.sp),
            bodyMedium = base.bodyMedium.copy(fontSize = 15.sp, lineHeight = 23.sp),
            bodySmall = base.bodySmall.copy(fontSize = 13.sp, lineHeight = 20.sp),
            labelLarge = base.labelLarge.copy(fontSize = 15.sp, lineHeight = 22.sp, fontWeight = FontWeight.SemiBold),
            labelSmall = base.labelSmall.copy(fontSize = 12.sp, lineHeight = 18.sp)
        ),
        content = content
    )
}

@Composable
internal fun MaruPanel(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier, shape = RoundedCornerShape(24.dp), color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaruLine)
    ) {
        Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp), content = content)
    }
}

@Composable
internal fun MaruHeading(title: String, detail: String? = null) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.titleLarge)
        if (detail != null) Text(detail, color = MaruMuted, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
internal fun MaruBrand(region: String?) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(Modifier.size(44.dp).clip(RoundedCornerShape(15.dp)).background(MaruInk), contentAlignment = Alignment.Center) {
            Text("p", color = MaruMint, fontSize = 32.sp, fontWeight = FontWeight.Bold)
        }
        Column {
            Text("플레오마루", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaruInk)
            Text(region ?: "우리 동네 드라이빙", style = MaterialTheme.typography.bodySmall, color = MaruMuted)
        }
    }
}

@Composable
internal fun MaruNavigation(tab: Int, vertical: Boolean, onTab: (Int) -> Unit) {
    val titles = listOf("내 주행수첩", "우리 동네", "지역 순위", "참여 설정")
    val shortTitles = listOf("수첩", "동네", "순위", "설정")
    val symbols = listOf("↗", "⌂", "≋", "⋯")
    @Composable fun item(index: Int, modifier: Modifier = Modifier) {
        val selected = tab == index
        Surface(
            onClick = { onTab(index) },
            color = if (selected) MaruInk else Color.Transparent,
            contentColor = if (selected) Color.White else MaruMuted,
            shape = RoundedCornerShape(16.dp),
            modifier = modifier.semantics { contentDescription = titles[index] }
        ) {
            if (vertical) {
                Row(Modifier.padding(horizontal = 18.dp, vertical = 15.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(symbols[index], fontSize = 20.sp)
                    Text(titles[index], fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium)
                }
            } else {
                Column(
                    Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(symbols[index], fontSize = 20.sp)
                    Text(shortTitles[index], fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
    if (vertical) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) { titles.indices.forEach { item(it, Modifier.fillMaxWidth()) } }
    } else {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            titles.indices.forEach { item(it, Modifier.weight(1f)) }
        }
    }
}

@Composable
internal fun RegionOptions(
    selected: String, enabled: Boolean = true, onSelect: (String) -> Unit
) {
    CommunityEngine.regions.forEach { region ->
        val chosen = selected == region
        Surface(
            modifier = Modifier.fillMaxWidth(),
            onClick = { onSelect(region) },
            enabled = enabled,
            shape = RoundedCornerShape(18.dp),
            color = if (chosen) Color(0xFFEDF6E7) else Color.White,
            border = androidx.compose.foundation.BorderStroke(if (chosen) 2.dp else 1.dp, if (chosen) MaruInk else MaruLine)
        ) {
            Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(region, modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                RadioButton(selected = chosen, onClick = null, enabled = enabled)
            }
        }
    }
}

@Composable
internal fun GoalHero(total: Long, contribution: Long) {
    val target = CommunityEngine.weeklyGoalMeters
    val progress by animateFloatAsState(
        (total.toFloat() / target).coerceIn(0f, 1f), tween(700), label = "neighborhood-progress"
    )
    Surface(shape = RoundedCornerShape(28.dp), color = MaruInk, contentColor = Color.White) {
        BoxWithConstraints(Modifier.fillMaxWidth().padding(28.dp)) {
            val wide = maxWidth >= 540.dp
            @Composable fun description(modifier: Modifier) {
                Column(modifier, verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("이번 주 공동 목표", color = MaruMint, style = MaterialTheme.typography.labelLarge)
                    Text(if (total >= target) "우리 동네,\n함께 해냈어요." else "같은 동네,\n함께 쌓는 주행.", style = MaterialTheme.typography.headlineLarge)
                    Text(formatDistance(total) + " / " + formatDistance(target), color = Color(0xFFD2E0D5))
                    Surface(color = Color.White.copy(alpha = .09f), shape = RoundedCornerShape(14.dp)) {
                        Text("나의 기여  " + formatDistance(contribution), Modifier.padding(horizontal = 16.dp, vertical = 12.dp), color = MaruMint, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            if (wide) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    description(Modifier.weight(1f))
                    GoalRing(progress, Modifier.size(230.dp))
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                    description(Modifier)
                    GoalRing(progress, Modifier.size(190.dp).align(Alignment.CenterHorizontally))
                }
            }
        }
    }
}

@Composable
private fun GoalRing(progress: Float, modifier: Modifier) {
    Box(modifier.semantics { contentDescription = "공동 목표 달성률 " + (progress * 100).toInt() + "퍼센트" }, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize().padding(12.dp)) {
            val thickness = 13.dp.toPx()
            drawArc(Color.White.copy(alpha = .12f), 140f, 260f, false, style = Stroke(thickness, cap = StrokeCap.Round))
            drawArc(MaruMint, 140f, 260f * progress, false, style = Stroke(thickness, cap = StrokeCap.Round))
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text((progress * 100).toInt().toString() + "%", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text("우리 동네 달성률", color = Color(0xFFD2E0D5), style = MaterialTheme.typography.bodySmall)
        }
    }
}

internal fun formatDistance(meters: Long): String =
    String.format(Locale.KOREA, "%,.1f km", meters / 1000.0)

@Composable
internal fun MetricTile(label: String, value: String, detail: String, modifier: Modifier = Modifier) {
    MaruPanel(modifier) {
        Text(label, color = MaruMuted, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.headlineMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(detail, color = MaruMuted, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
internal fun PersonalMetrics(personal: CommunityMember, days: Int, previous: Double?) {
    val delta = if (personal.score != null && previous != null) String.format(Locale.KOREA, "지난주 대비 %+.1f점", personal.score - previous) else "비교 이력이 쌓이면 변화가 표시돼요"
    BoxWithConstraints {
        if (maxWidth >= 650.dp) {
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                MetricTile("이번 주 주행거리", formatDistance(personal.meters), "우리 동네 공동 목표에 기여한 거리", Modifier.weight(1f))
                MetricTile("안전운전 점수", personal.score?.let { String.format(Locale.KOREA, "%.1f", it) } ?: "확인 대기", delta, Modifier.weight(1f))
                MetricTile("주행 참여일", days.toString() + "일", "주행이 있는 날을 하루씩 기록해요", Modifier.weight(1f))
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricTile("이번 주 주행거리", formatDistance(personal.meters), "평소의 주행이 동네의 기록으로", Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MetricTile("안전운전 점수", personal.score?.let { String.format(Locale.KOREA, "%.1f", it) } ?: "대기", delta, Modifier.weight(1f))
                    MetricTile("주행 참여일", days.toString() + "일", "이번 주 함께한 날", Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
internal fun RankingPanel(members: List<CommunityMember>, safety: Boolean, modifier: Modifier = Modifier) {
    MaruPanel(modifier) {
        MaruHeading(if (safety) "안전운전 순위" else "주행거리 순위", "같은 동네, 각자의 멋진 기록")
        val entries = if (safety) CommunityEngine.safetyRanking(members) else CommunityEngine.distanceRanking(members)
        entries.forEach { (rank, member) ->
            val me = member.name == "나"
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                    .background(if (me) Color(0xFFEDF6E7) else Color.Transparent).padding(vertical = 12.dp, horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(rank.toString().padStart(2, '0'), color = MaruMuted, modifier = Modifier.width(26.dp), style = MaterialTheme.typography.labelLarge)
                Box(
                    Modifier.size(38.dp).clip(CircleShape).background(if (me) MaruInk else MaruPaper),
                    contentAlignment = Alignment.Center
                ) { Text(member.name.take(1), color = if (me) MaruMint else MaruInk, fontWeight = FontWeight.Bold) }
                Text(member.name, Modifier.weight(1f), fontWeight = if (me) FontWeight.Bold else FontWeight.Medium)
                Text(if (safety) String.format(Locale.KOREA, "%.1f", member.score) + "점" else formatDistance(member.meters), fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
            }
        }
        if (safety) {
            members.filter { it.score == null }.forEach {
                Text(it.name + " · 점수 확인 대기", style = MaterialTheme.typography.bodySmall, color = MaruMuted)
            }
        }
    }
}

@Composable
internal fun JourneyChart(state: CommunityState, week: String) {
    val totals = CommunityEngine.trips(state, week).groupBy { it.day }.mapValues { (_, trips) -> trips.sumOf { it.meters } }
    val start = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).apply { timeZone = java.util.TimeZone.getTimeZone("Asia/Seoul") }.parse(week) ?: return
    val calendar = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("Asia/Seoul")).apply { time = start }
    val formatter = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).apply { timeZone = calendar.timeZone }
    val daily = (0..6).map { val value = totals[formatter.format(calendar.time)] ?: 0L; calendar.add(java.util.Calendar.DAY_OF_MONTH, 1); value }
    val max = daily.maxOrNull()?.coerceAtLeast(1L) ?: 1L
    MaruPanel(Modifier.fillMaxWidth()) {
        MaruHeading("이번 주 주행 리듬", "기록된 테스트 주행거리 · " + week + " 시작 주간")
        Row(Modifier.fillMaxWidth().height(150.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            daily.forEachIndexed { index, meters ->
                Column(Modifier.weight(1f).fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.BottomCenter) {
                        Box(Modifier.fillMaxWidth(.65f).fillMaxHeight(if (meters == 0L) .03f else (meters.toFloat() / max).coerceIn(.03f, 1f)).clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)).background(if (meters > 0) MaruInk else MaruLine))
                    }
                    Text(listOf("월", "화", "수", "목", "금", "토", "일")[index], style = MaterialTheme.typography.bodySmall, color = MaruMuted)
                }
            }
        }
        Text(daily.mapIndexed { index, meters -> listOf("월", "화", "수", "목", "금", "토", "일")[index] + " " + formatDistance(meters) }.joinToString(" · "), style = MaterialTheme.typography.bodySmall, color = MaruMuted)
    }
}
