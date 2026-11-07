package ai.pleos.playground.sample.vehicle.petgarden

import ai.pleos.playground.sample.vehicle.driveclub.SeasonEngine
import ai.pleos.playground.sample.vehicle.driveclub.demoDays
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun PetGardenApp(onOpenSdk: () -> Unit) {
    val context = LocalContext.current
    val store = remember { GardenStore(context) }
    val loaded = remember { runCatching { store.load() } }
    var state by remember { mutableStateOf(loaded.getOrDefault(GardenState())) }
    var error by remember { mutableStateOf(if (loaded.isFailure) "저장 기록을 읽지 못했습니다. 초기화 전 앱 데이터를 백업해주세요." else "") }
    var storageReadable by remember { mutableStateOf(loaded.isSuccess) }
    var consent by remember { mutableStateOf(store.consent && loaded.isSuccess) }
    var parked by remember { mutableStateOf(false) }
    var name by rememberSaveable { mutableStateOf("") }
    var tab by rememberSaveable { mutableStateOf(0) }
    var scenario by rememberSaveable { mutableStateOf(0) }
    var message by rememberSaveable { mutableStateOf("오늘도 무사히 다녀왔나요? 잠깐 함께 쉬어요.") }
    var resetDialog by remember { mutableStateOf(false) }
    fun day(): String = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).apply {
        timeZone = TimeZone.getTimeZone("Asia/Seoul")
    }.format(Date())
    fun update(next: GardenState, feedback: String) {
        if (store.save(next)) { state = next; message = feedback; error = "" }
        else error = "저장하지 못했습니다. 다시 시도해주세요."
    }
    val palette = lightColorScheme(primary = Color(0xFF496B50), onPrimary = Color.White,
        background = Color(0xFFFAF7EF), surface = Color(0xFFFAF7EF),
        secondaryContainer = Color(0xFFE7EBD9), onSurface = Color(0xFF35443A))
    MaterialTheme(colorScheme = palette) {
        Surface(Modifier.fillMaxSize()) {
            Column(Modifier.safeDrawingPadding().verticalScroll(rememberScrollState()).padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("차 안의 작은 숲", style = MaterialTheme.typography.headlineLarge)
                Text("함께 다녀오고, 함께 자라는 우리", style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Switch(checked = parked, onCheckedChange = { parked = it })
                    Text(if (parked) "개발용 정차 모드" else "주행 / 상태 미확인 · 게임 숨김")
                }
                if (!parked) {
                    Text("주차 후 작은 숲에서 만나요.", style = MaterialTheme.typography.headlineSmall)
                    Text("이 스위치는 데모용입니다. 실제 차량 상태 감지와 자동 제한은 아직 연결되지 않았습니다.")
                } else {
                    if (error.isNotBlank()) Text(error, color = MaterialTheme.colorScheme.error)
                    if (!consent) {
                        Text("작은 양 한 마리와 나만의 정원을 만나보세요.")
                        Text("펫 이름·성장·꾸미기 기록을 이 기기에 저장합니다. 실제 위치나 차량 기록은 수집하지 않습니다.")
                        Button(enabled = storageReadable, onClick = {
                            if (store.consent(true)) consent = true else error = "동의를 저장하지 못했습니다."
                        }) { Text("동의하고 시작") }
                    } else if (state.name.isBlank()) {
                        GardenScene(GardenState())
                        Text("새로운 친구에게 이름을 지어주세요.")
                        OutlinedTextField(value = name, onValueChange = { name = it.take(12) },
                            label = { Text("펫 이름 · 최대 12자") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                        Button(enabled = name.isNotBlank(), onClick = {
                            update(GardenEngine.adopt(name), "${name.trim()}의 숲에 오신 것을 환영해요. 첫 들꽃을 심어볼까요?")
                        }) { Text("우리 함께 살자") }
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            listOf("나의 숲", "꾸미기", "여정", "마을").forEachIndexed { index, label ->
                                FilterChip(selected = tab == index, onClick = { tab = index }, label = { Text(label) })
                            }
                        }
                        Text("${state.name}  ·  Lv.${state.level}  ·  잎사귀 ${state.leaves}", style = MaterialTheme.typography.titleLarge)
                        Text("다음 성장까지 ${50 - state.xp % 50} 경험치")
                        if (tab != 2) GardenScene(state, village = tab == 3)
                        Card(Modifier.fillMaxWidth()) { Text(message, Modifier.padding(16.dp)) }
                        when (tab) {
                            0 -> {
                                Text("오늘의 작은 돌봄", style = MaterialTheme.typography.titleMedium)
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    listOf("pet" to "쓰다듬기", "feed" to "먹이 주기", "rest" to "함께 쉬기").forEach { (action, label) ->
                                        val done = state.day == day() && action in state.care
                                        OutlinedButton(enabled = !done, onClick = {
                                            update(GardenEngine.care(state, day(), action), when (action) {
                                                "pet" -> "${state.name}가 손에 볼을 비벼요."
                                                "feed" -> "작은 간식을 오물오물 먹고 있어요."
                                                else -> "아무것도 하지 않아도, 함께라서 좋아요."
                                            })
                                        }) { Text(if (done) "$label 완료" else label) }
                                    }
                                }
                                Text("돌봄마다 하루 한 번 경험치 5 · 잎사귀 3. 운전하지 않는 날에도 함께 자라요.")
                            }
                            1 -> {
                                Text("정원에 작은 추억을 놓아보세요.")
                                Decoration.entries.forEach { item ->
                                    val owned = item.name in state.owned
                                    OutlinedCard(Modifier.fillMaxWidth()) {
                                        Row(Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("${item.label} · ${if (owned) "보유 중" else "잎사귀 ${item.price}"}")
                                            Button(enabled = owned || state.leaves >= item.price, onClick = {
                                                update(if (owned) GardenEngine.toggle(state, item) else GardenEngine.buy(state, item), "정원의 ${item.label} 배치를 바꿨어요.")
                                            }) { Text(if (!owned) "놓기" else if (item.name in state.placed) "보관" else "배치") }
                                        }
                                    }
                                }
                            }
                            2 -> {
                                Text("테스트 여정 · 실제 차량 데이터 미연동", style = MaterialTheme.typography.titleMedium)
                                Text("거리 성장량은 하루 최대 20. 안전점수가 없거나 낮아도 펫은 아프거나 퇴화하지 않아요.")
                                val journeyId = "demo-${day()}-commute"
                                Button(enabled = journeyId !in state.journeys, onClick = {
                                    update(GardenEngine.journey(state, day(), journeyId, 12), "오늘의 테스트 여정 12km를 추억으로 남겼어요.")
                                }) { Text(if (journeyId in state.journeys) "오늘의 여정 반영 완료" else "테스트 귀가 12km 반영") }
                                Text("안전점수 테스트 이력")
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf("정상", "90점 미달", "결측").forEachIndexed { index, label ->
                                        FilterChip(selected = scenario == index, onClick = { scenario = index }, label = { Text(label) })
                                    }
                                }
                                val result = SeasonEngine.evaluate(demoDays(scenario))
                                Text("4주 유지: ${if (result.eligible) "검증 완료" else "조건 미충족 / 확인 대기"}")
                                val bonusId = "demo-season-1"
                                Button(enabled = result.eligible && bonusId !in state.bonuses, onClick = {
                                    update(GardenEngine.weeklyBonus(state, bonusId, result), "꾸준한 습관 선물 · 잎사귀 15를 받았어요.")
                                }) { Text(if (bonusId in state.bonuses) "시즌 선물 수령 완료" else "테스트 시즌 선물 받기") }
                                Text("동일 여정·시즌은 재실행 후에도 중복 보상하지 않습니다.")
                            }
                            3 -> {
                                Text("마을 미리보기 · 나와 NPC 이웃 2마리")
                                Text("구름이와 도토리가 쉬고 있어요. 실제 접속 이용자는 아니며 온라인 공유는 후속 단계입니다.")
                                OutlinedButton(onClick = { message = "${state.name}가 손을 흔들자, 구름이와 도토리도 반갑게 인사해요." }) { Text("이웃에게 인사") }
                            }
                        }
                    }
                    HorizontalDivider()
                    Text("로컬 개발 데모 · 거리·안전점수·이웃은 테스트입니다.", style = MaterialTheme.typography.bodySmall)
                    OutlinedButton(onClick = onOpenSdk) { Text("개발자 · Pleos SDK 진단") }
                    TextButton(onClick = { resetDialog = true }) { Text("동의 철회 및 작은 숲 삭제") }
                }
            }
        }
        if (resetDialog && parked) AlertDialog(onDismissRequest = { resetDialog = false },
            title = { Text("작은 숲을 삭제할까요?") },
            text = { Text("이 기기의 펫·정원·보상 기록과 동의가 삭제됩니다. 되돌릴 수 없습니다.") },
            confirmButton = { TextButton(onClick = {
                if (store.clear()) { state = GardenState(); storageReadable = true; consent = false; name = ""; error = ""; message = "새로운 친구를 기다리고 있어요."; tab = 0 }
                else error = "삭제하지 못했습니다. 다시 시도해주세요."
                resetDialog = false
            }) { Text("삭제") } },
            dismissButton = { TextButton(onClick = { resetDialog = false }) { Text("취소") } })
    }
}
