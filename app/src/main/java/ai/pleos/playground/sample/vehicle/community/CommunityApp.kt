package ai.pleos.playground.sample.vehicle.community

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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

private fun distance(meters: Long): String =
    String.format(Locale.KOREA, "%,.1f km", meters / 1000.0)

@Composable
fun CommunityApp(onOpenSdk: () -> Unit) {
    val context = LocalContext.current
    val store = remember(context) { CommunityStore(context) }
    val loaded = remember(store) { store.load() }
    var state by remember { mutableStateOf(loaded.getOrDefault(CommunityState())) }
    var loadFailed by remember { mutableStateOf(loaded.isFailure) }
    var parked by remember { mutableStateOf(false) }
    var tab by rememberSaveable { mutableIntStateOf(0) }
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

    Surface(Modifier.fillMaxSize()) {
        Column(
            Modifier.safeDrawingPadding().verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("플레오마루", style = MaterialTheme.typography.headlineLarge)
            Text("오늘의 주행이 우리 동네의 기록으로", style = MaterialTheme.typography.titleMedium)
            Text("개발 데모 · 거리·점수·이웃 기록은 모두 테스트 데이터")
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Switch(parked, onCheckedChange = { parked = it })
                Text(if (parked) "개발용 정차 모드" else "주행 / 상태 미확인 · 상세 화면 숨김")
            }
            if (!parked) {
                Text("주차 후 내 기록과 우리 동네의 목표를 확인해 주세요.")
            } else if (loadFailed) {
                Text("저장된 기록을 읽지 못했습니다. 데이터 삭제 후 다시 시작할 수 있습니다.")
                OutlinedButton(onClick = { resetDialog = true }) { Text("기기 내 커뮤니티 기록 삭제") }
            } else if (state.region == null) {
                Text("어느 동네와 함께할까요?", style = MaterialTheme.typography.titleLarge)
                Text("시연용 지역을 선택해 주세요. 주행 장소에 관계없이 선택한 지역에 기여합니다.")
                CommunityEngine.regions.forEach { region ->
                    FilterChip(selectedRegion == region, onClick = { selectedRegion = region }, label = { Text(region) })
                }
                Text("동의하면 이 기기에 소속 지역과 모의 주행 기록을 저장합니다. 실제 차량 정보와 위치는 수집하지 않습니다.")
                Button(onClick = {
                    persist(CommunityEngine.enroll(state, selectedRegion, week), "지역 챌린지에 참여했어요.")
                }) { Text("동의하고 우리 동네 참여") }
            } else {
                Text(state.region!!, style = MaterialTheme.typography.titleLarge)
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("내 기록", "우리 동네", "지역 순위", "참여 설정").forEachIndexed { index, label ->
                        FilterChip(tab == index, onClick = { tab = index }, label = { Text(label) })
                    }
                }
                val personal = CommunityEngine.personal(state, week)
                val members = CommunityEngine.members(state, week)
                val total = members.sumOf { it.meters }
                val progress = (total.toDouble() / CommunityEngine.weeklyGoalMeters).coerceIn(0.0, 1.0)
                when (tab) {
                    0 -> {
                        Text("이번 주 나의 주행", style = MaterialTheme.typography.titleLarge)
                        Text(distance(personal.meters), style = MaterialTheme.typography.headlineLarge)
                        Text(CommunityEngine.days(state, week).toString() + "일 참여 · " + week + " 시작 주간")
                        val old = CommunityEngine.personal(state, previousWeek).score
                        Text(personal.score?.let { "테스트 안전운전 점수 " + String.format(Locale.KOREA, "%.1f", it) + "점" } ?: "안전운전 점수 확인 대기")
                        if (personal.score != null && old != null) {
                            Text("지난주 대비 " + String.format(Locale.KOREA, "%+.1f", personal.score - old) + "점")
                        } else Text("비교 가능한 이전 점수가 쌓이면 변화가 표시됩니다.")
                        Text("내비게이션을 사용한 주행과 안내 없이 운전한 주행 모두 집계하는 기획입니다.")
                        HorizontalDivider()
                        Text("개발용 주행 입력", style = MaterialTheme.typography.titleMedium)
                        Text("아래 버튼은 실제 차량 연동 대신 동일한 당일 모의 귀가 기록을 저장합니다.")
                        Button(onClick = {
                            val currentDay = koreanDay()
                            val currentWeek = koreanWeek()
                            week = currentWeek
                            previousWeek = koreanWeek(-1)
                            val trip = CommunityTrip("demo-return-" + currentDay, currentDay, currentWeek, state.region!!, 30_000L, 94)
                            val next = CommunityEngine.record(state, trip)
                            persist(next, if (next == state) "오늘의 테스트 기록은 이미 반영됐어요." else "테스트 귀가 기록이 동네 목표에 더해졌어요.")
                        }) { Text("테스트 귀가 기록 반영") }
                    }
                    1 -> {
                        Text("이번 주 함께 목표 채우기", style = MaterialTheme.typography.titleLarge)
                        Text(distance(total) + " / " + distance(CommunityEngine.weeklyGoalMeters))
                        LinearProgressIndicator(progress = { progress.toFloat() }, modifier = Modifier.fillMaxWidth())
                        Text("목표 달성률 " + String.format(Locale.KOREA, "%.1f", progress * 100) + "% · 나의 기여 " + distance(personal.meters))
                        if (progress >= 1.0) Text("우리 동네 목표 달성! 함께 만든 기록이에요.")
                        Text("이웃의 거리는 가상 데이터입니다. 실제 서버 공유·집계는 후속 개발입니다.")
                        HorizontalDivider()
                        Text("동네에 응원 남기기", style = MaterialTheme.typography.titleMedium)
                        OutlinedTextField(
                            draft, onValueChange = { draft = it.take(300) },
                            label = { Text("응원 메시지 · 이 기기에만 저장") }, modifier = Modifier.fillMaxWidth()
                        )
                        Button(enabled = draft.isNotBlank(), onClick = {
                            val next = state.copy(posts = (state.posts + (state.region + " · " + draft.trim())).takeLast(50))
                            if (store.save(next)) {
                                state = next
                                draft = ""
                                message = "이 기기에 응원을 남겼어요."
                            } else message = "저장하지 못했습니다. 다시 시도해 주세요."
                        }) { Text("기기에 응원 저장") }
                        state.posts.filter { it.startsWith(state.region + " · ") }.asReversed().forEach { Text(it) }
                    }
                    2 -> {
                        Text("우리 동네 주행거리 순위", style = MaterialTheme.typography.titleLarge)
                        CommunityEngine.distanceRanking(members).forEach { (rank, member) ->
                            Text(rank.toString() + "위 · " + member.name + " · " + distance(member.meters))
                        }
                        HorizontalDivider()
                        Text("우리 동네 안전운전 순위", style = MaterialTheme.typography.titleLarge)
                        CommunityEngine.safetyRanking(members).forEach { (rank, member) ->
                            Text(rank.toString() + "위 · " + member.name + " · " + String.format(Locale.KOREA, "%.1f", member.score) + "점")
                        }
                        Text("테스트 기록 점수 평균으로 시연합니다. 점수가 없는 참여자는 확인 대기로 표시합니다.")
                        members.filter { it.score == null }.forEach { Text(it.name + " · 점수 확인 대기") }
                    }
                    3 -> {
                        Text("나의 소속 지역", style = MaterialTheme.typography.titleLarge)
                        Text("이번 주 기여 기록이 있으면 소속 지역은 다음 주에 변경할 수 있습니다.")
                        CommunityEngine.regions.forEach { region ->
                            FilterChip(
                                state.region == region, enabled = CommunityEngine.trips(state, week).isEmpty(),
                                onClick = { persist(CommunityEngine.enroll(state, region, week), "소속 지역을 변경했어요.") },
                                label = { Text(region) }
                            )
                        }
                        Text("포인트 제휴 준비 중", style = MaterialTheme.typography.titleMedium)
                        Text("실제 포인트 적립·전환은 제휴와 서버 정산 체계 확보 후 제공할 계획입니다.")
                        OutlinedButton(onClick = onOpenSdk) { Text("Pleos SDK 진단") }
                        TextButton(onClick = { resetDialog = true }) { Text("동의 철회 및 커뮤니티 기록 삭제") }
                    }
                }
            }
            if (parked && message.isNotBlank()) Text(message)
            Text("차량 주행 상태 자동 감지 미연결 · 실제 차량에서는 CarUxRestrictions 연동과 주차 검증이 필요합니다.", style = MaterialTheme.typography.bodySmall)
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
