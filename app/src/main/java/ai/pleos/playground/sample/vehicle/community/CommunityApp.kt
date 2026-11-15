package ai.pleos.playground.sample.vehicle.community

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private fun koreanDay(): String =
    SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).apply {
        timeZone = TimeZone.getTimeZone("Asia/Seoul")
    }.format(Date())

private fun koreanWeek(offset: Int = 0): String {
    val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul"), Locale.KOREA)
    val elapsed = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7
    calendar.add(Calendar.DAY_OF_MONTH, -elapsed + offset * 7)
    return SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).apply {
        timeZone = calendar.timeZone
    }.format(calendar.time)
}

@Composable
fun CommunityApp(onOpenSdk: () -> Unit) {
    val context = LocalContext.current
    val store = remember(context) { CommunityStore(context) }
    val loaded = remember(store) { store.load() }
    var state by remember { mutableStateOf(loaded.getOrDefault(CommunityState())) }
    var loadFailed by remember { mutableStateOf(loaded.isFailure) }
    var parked by remember { mutableStateOf(false) }
    var tab by rememberSaveable { mutableIntStateOf(1) }
    var selectedRegion by rememberSaveable { mutableStateOf(CommunityEngine.regions.first()) }
    var message by remember { mutableStateOf("") }
    var draft by rememberSaveable { mutableStateOf("") }
    var resetDialog by remember { mutableStateOf(false) }
    var week by remember { mutableStateOf(koreanWeek()) }
    var previousWeek by remember { mutableStateOf(koreanWeek(-1)) }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) parked = false
            if (event == Lifecycle.Event.ON_RESUME) {
                week = koreanWeek()
                previousWeek = koreanWeek(-1)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    fun persist(next: CommunityState, success: String) {
        if (store.save(next)) {
            state = next
            message = success
        } else message = "저장하지 못했습니다. 다시 시도해 주세요."
    }

    fun recordDemo() {
        val currentDay = koreanDay()
        val currentWeek = koreanWeek()
        week = currentWeek
        previousWeek = koreanWeek(-1)
        val trip = CommunityTrip("demo-return-" + currentDay, currentDay, currentWeek, state.region!!, 30_000L, 94)
        val next = CommunityEngine.record(state, trip)
        persist(next, if (next == state) "오늘의 테스트 기록은 이미 반영됐어요." else "오늘의 테스트 여정이 동네 목표에 더해졌어요.")
    }

    CommunityTheme {
        Surface(Modifier.fillMaxSize(), color = MaruPaper) {
            BoxWithConstraints(Modifier.safeDrawingPadding()) {
                val wide = maxWidth >= 1000.dp
                Row(Modifier.fillMaxSize()) {
                    if (wide) {
                        Column(
                            Modifier.width(230.dp).fillMaxHeight().padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(36.dp)
                        ) {
                            MaruBrand(null)
                            if (parked && state.region != null && !loadFailed) {
                                MaruNavigation(tab, true) { tab = it }
                            }
                            Spacer(Modifier.weight(1f))
                            Text("시연용 커뮤니티\n가상 이웃과 테스트 주행 기록", color = MaruMuted, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    Column(
                        Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState())
                            .padding(if (wide) 32.dp else 20.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            if (!wide) MaruBrand(if (parked) state.region else null)
                            else Column {
                                Text(if (parked) state.region ?: "우리 동네에 오신 걸 환영해요" else "운전을 마치고 만나요", style = MaterialTheme.typography.headlineMedium)
                                Text("평소의 주행이 함께 만든 기록으로", color = MaruMuted, style = MaterialTheme.typography.bodySmall)
                            }
                            Spacer(Modifier.weight(1f))
                            if (parked && state.region != null) {
                                TextButton(onClick = { tab = 3 }) { Text("설정") }
                            }
                        }
                        if (!parked) {
                            MaruPanel(Modifier.fillMaxWidth()) {
                                Text("주차 후,\n우리 동네에서 만나요.", style = MaterialTheme.typography.headlineLarge)
                                Text("운전을 마치면 나의 기록과 이웃들이 함께 채운 목표를 확인할 수 있어요.", color = MaruMuted)
                                HorizontalDivider(color = MaruLine)
                                Text("개발용 정차 모드", fontWeight = FontWeight.SemiBold)
                                Text("실제 주차 신호를 대신하는 시연용 스위치입니다.", color = MaruMuted, style = MaterialTheme.typography.bodySmall)
                                Switch(checked = false, onCheckedChange = { parked = it })
                            }
                        } else if (loadFailed) {
                            MaruPanel(Modifier.fillMaxWidth()) {
                                MaruHeading("기록을 읽지 못했어요", "기기 내 커뮤니티 데이터를 삭제하고 다시 시작할 수 있습니다.")
                                Button(onClick = { resetDialog = true }) { Text("저장 기록 삭제") }
                            }
                        } else if (state.region == null) {
                            MaruPanel(Modifier.fillMaxWidth()) {
                                Text("어느 동네와\n함께 달릴까요?", style = MaterialTheme.typography.headlineLarge)
                                Text("주행 장소와 관계없이 선택한 거주 지역에 기여해요. 아래는 시연용 지역입니다.", color = MaruMuted)
                                RegionOptions(selectedRegion, onSelect = { selectedRegion = it })
                                Text("참여하면 소속 지역과 테스트 기록을 이 기기에 저장합니다. 실제 차량 데이터는 수집하지 않습니다.", color = MaruMuted, style = MaterialTheme.typography.bodySmall)
                                Button(
                                    onClick = { persist(CommunityEngine.enroll(state, selectedRegion, week), "우리 동네 챌린지에 참여했어요.") },
                                    modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp),
                                    shape = RoundedCornerShape(16.dp)
                                ) { Text("동의하고 우리 동네 참여") }
                            }
                        } else {
                            if (!wide) MaruNavigation(tab, false) { tab = it }
                            val personal = CommunityEngine.personal(state, week)
                            val members = CommunityEngine.members(state, week)
                            val total = members.sumOf { it.meters }
                            val previous = CommunityEngine.personal(state, previousWeek).score
                            val days = CommunityEngine.days(state, week)
                            when (tab) {
                                0 -> {
                                    MaruHeading("나의 주행수첩", "출퇴근부터 주말 나들이까지, 함께한 기록")
                                    PersonalMetrics(personal, days, previous)
                                    JourneyChart(state, week)
                                    MaruPanel(Modifier.fillMaxWidth()) {
                                        MaruHeading("오늘의 테스트 여정", "모의 귀가 기록을 반영하면 내 기록과 동네 목표가 함께 바뀝니다.")
                                        Button(onClick = { recordDemo() }, shape = RoundedCornerShape(14.dp)) {
                                            Text("테스트 귀가 기록 반영")
                                        }
                                    }
                                }
                                1 -> {
                                    GoalHero(total, personal.meters)
                                    PersonalMetrics(personal, days, previous)
                                    MaruPanel(Modifier.fillMaxWidth()) {
                                        MaruHeading("오늘의 여정", "테스트 귀가를 반영하고 우리 동네 기여도를 확인해요.")
                                        Button(onClick = { recordDemo() }, shape = RoundedCornerShape(14.dp)) { Text("테스트 귀가 기록 반영") }
                                    }
                                    MaruPanel(Modifier.fillMaxWidth()) {
                                        MaruHeading("우리 동네 응원", "이 기기에 남기는 작은 응원")
                                        OutlinedTextField(
                                            draft, onValueChange = { draft = it.take(300) },
                                            modifier = Modifier.fillMaxWidth(),
                                            label = { Text("이웃에게 응원 한마디") },
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                        Button(enabled = draft.isNotBlank(), onClick = {
                                            val next = state.copy(posts = (state.posts + (state.region + " · " + draft.trim())).takeLast(50))
                                            if (store.save(next)) {
                                                state = next
                                                draft = ""
                                                message = "이 기기에 응원을 남겼어요."
                                            } else message = "저장하지 못했습니다. 다시 시도해 주세요."
                                        }, shape = RoundedCornerShape(14.dp)) { Text("응원 남기기") }
                                        val localPosts = state.posts.filter { it.startsWith(state.region + " · ") }.asReversed()
                                        if (localPosts.isEmpty()) Text("첫 응원을 남겨 주세요.", color = MaruMuted, style = MaterialTheme.typography.bodySmall)
                                        localPosts.forEach { post ->
                                            Surface(color = MaruPaper, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
                                                Text(post.substringAfter(" · "), Modifier.padding(18.dp), style = MaterialTheme.typography.bodyMedium)
                                            }
                                        }
                                    }
                                }
                                2 -> {
                                    MaruHeading("함께 달리는 이웃들", "거리와 안전운전, 서로 다른 멋진 기록 · 시연용 순위")
                                    BoxWithConstraints {
                                        if (maxWidth >= 760.dp) {
                                            Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                                                RankingPanel(members, false, Modifier.weight(1f))
                                                RankingPanel(members, true, Modifier.weight(1f))
                                            }
                                        } else {
                                            Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                                                RankingPanel(members, false, Modifier.fillMaxWidth())
                                                RankingPanel(members, true, Modifier.fillMaxWidth())
                                            }
                                        }
                                    }
                                }
                                3 -> {
                                    MaruHeading("참여 설정", "내가 속한 동네와 기록 관리")
                                    MaruPanel(Modifier.fillMaxWidth()) {
                                        MaruHeading("나의 소속 지역", "이번 주 기여 기록이 있으면 다음 주에 변경할 수 있어요.")
                                        RegionOptions(
                                            state.region!!, enabled = CommunityEngine.trips(state, week).isEmpty(),
                                            onSelect = { persist(CommunityEngine.enroll(state, it, week), "소속 지역을 변경했어요.") }
                                        )
                                    }
                                    MaruPanel(Modifier.fillMaxWidth()) {
                                        MaruHeading("생활 속 혜택으로", "포인트 제휴 준비 중")
                                        Text("주유·쇼핑에 쓸 수 있는 보상은 제휴와 서버 정산 체계 확보 후 제공할 계획입니다.", color = MaruMuted)
                                    }
                                    MaruPanel(Modifier.fillMaxWidth()) {
                                        MaruHeading("개발용 설정", "실제 차량 수집과 서버 공유는 아직 연결되지 않았습니다.")
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("정차 모의 상태", Modifier.weight(1f))
                                            Switch(parked, onCheckedChange = { parked = it })
                                        }
                                        OutlinedButton(onClick = onOpenSdk) { Text("Pleos SDK 진단") }
                                        TextButton(onClick = { resetDialog = true }) { Text("동의 철회 및 커뮤니티 기록 삭제") }
                                    }
                                }
                            }
                        }
                        if (parked && message.isNotBlank()) {
                            Surface(color = Color(0xFFE7F1DD), shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
                                Text(message, Modifier.padding(18.dp), color = MaruInk)
                            }
                        }
                        Text(
                            "시연용 앱 · 테스트 거리·점수·가상 이웃\n실제 주차 신호·차량 수집·서버 공유·포인트 지급은 후속 연동 대상입니다.",
                            style = MaterialTheme.typography.bodySmall, color = MaruMuted
                        )
                        Spacer(Modifier.height(12.dp))
                    }
                }
            }
        }
        if (resetDialog && parked) {
            AlertDialog(
                onDismissRequest = { resetDialog = false },
                title = { Text("커뮤니티 기록을 삭제할까요?") },
                text = { Text("소속 지역, 테스트 주행 기록, 이 기기에 남긴 응원과 저장 동의를 삭제합니다.") },
                confirmButton = {
                    TextButton(onClick = {
                        if (store.clear()) {
                            state = CommunityState()
                            loadFailed = false
                            draft = ""
                            message = ""
                            resetDialog = false
                        } else message = "삭제하지 못했습니다. 다시 시도해 주세요."
                    }) { Text("삭제") }
                },
                dismissButton = { TextButton(onClick = { resetDialog = false }) { Text("취소") } }
            )
        }
    }
}
