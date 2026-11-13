package ai.pleos.playground.sample.vehicle.driveclub

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun DriveClubApp(onOpenSdk: () -> Unit) {
    val preferences = LocalContext.current.getSharedPreferences("driveclub-demo", Context.MODE_PRIVATE)
    var consent by remember { mutableStateOf(preferences.getBoolean("consent", false)) }
    var scenario by rememberSaveable { mutableStateOf(0) }
    var tab by rememberSaveable { mutableStateOf(0) }
    // This is a demo input, never a vehicle-derived safety signal.
    var parked by rememberSaveable { mutableStateOf(false) }
    var issued by remember { mutableStateOf(preferences.getBoolean("demo-issued", false)) }
    var draft by rememberSaveable { mutableStateOf("") }
    var posts by remember { mutableStateOf(preferences.getString("posts", "이번 주에는 출발 전에 여유 시간을 확보했어요.")!!.split("\n").filter { it.isNotBlank() }) }
    val result = remember(scenario) { SeasonEngine.evaluate(demoDays(scenario)) }
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(Modifier.safeDrawingPadding().verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("DRIVE CLUB", style = MaterialTheme.typography.headlineLarge)
            Text("높은 점수를 오래 유지하는 습관", style = MaterialTheme.typography.titleMedium)
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("개발 데모 · 모든 점수와 차량 프로필은 테스트 데이터")
                    Text("IONIQ 5 · 테스트 점수 제공사 · 4주 시즌 전체 이력")
                    Text("실제 점수 API·차량 기록 수집·쿠폰 발급은 연결되지 않았습니다.")
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Switch(checked = parked, onCheckedChange = { parked = it })
                Text(if (parked) "테스트 상태: 정차" else "테스트 상태: 주행/미확인 · 상세 조작 제한")
            }
            if (!parked) {
                Text("요약: 시즌 최고 ${result.longest}주 유지 · 상세 기능은 정차 테스트 상태에서 확인")
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Checkbox(checked = consent, onCheckedChange = {
                        consent = it
                        preferences.edit().putBoolean("consent", it).apply()
                    })
                    Text("이 기기에서 테스트 기록·게시글을 저장하는 데 동의")
                }
                if (consent) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("내 기록", "시즌 순위", "모임", "보상").forEachIndexed { index, label ->
                            FilterChip(selected = tab == index, onClick = { tab = index }, label = { Text(label) })
                        }
                    }
                    when (tab) {
                        0 -> {
                            Text("현재 ${result.current}주 / 시즌 최고 ${result.longest}주", style = MaterialTheme.typography.headlineMedium)
                            Text("원천 점수 평균: ${String.format(java.util.Locale.KOREA, "%.1f", result.average)}점 · 개인 운행거리: 미연동")
                            result.weeks.forEachIndexed { index, status ->
                                OutlinedCard(Modifier.fillMaxWidth()) {
                                    Text("${index + 1}주차     ${status.label}", Modifier.padding(16.dp))
                                }
                            }
                            Text("주 3일 이상 운행, 관측 점수 90점 이상. 결측·관찰 부족 주차는 연속 기록을 연결하지 않습니다.")
                        }
                        1 -> {
                            Text("테스트 리그 · 동일 차종 / 동일 점수 출처")
                            val ranking = listOf("나" to result, "여유로운출발" to SeasonEngine.evaluate(demoDays(0)), "천천히함께" to SeasonEngine.evaluate(demoDays(1)))
                                .sortedWith(compareByDescending<Pair<String, SeasonResult>> { it.second.longest }.thenByDescending { it.second.average ?: 0.0 })
                            ranking.forEach { (name, entry) ->
                                val rank = 1 + ranking.count { it.second.longest > entry.longest || (it.second.longest == entry.longest && (it.second.average ?: 0.0) > (entry.average ?: 0.0)) }
                                Text("${rank}위  $name · 최고 ${entry.longest}주 · ${entry.average?.toInt()}점${if (entry.weeks.contains(WeekStatus.PENDING)) " · 잠정" else ""}")
                            }
                            Text("공동순위 적용 · 거리와 운행시간은 순위에 사용하지 않습니다.")
                        }
                        2 -> {
                            Text("IONIQ 5 소모임 · 기기 내 게시판 데모")
                            Text("이번 주 유지에 도움이 된 습관을 공유해주세요.")
                            OutlinedTextField(value = draft, onValueChange = { draft = it.take(300).replace('\n', ' ') }, label = { Text("주간 회고 (300자)") }, modifier = Modifier.fillMaxWidth())
                            Button(enabled = draft.isNotBlank(), onClick = {
                                posts = posts + draft.trim()
                                preferences.edit().putString("posts", posts.joinToString("\n")).apply()
                                draft = ""
                            }) { Text("기기에 게시") }
                            posts.forEach { post -> OutlinedCard(Modifier.fillMaxWidth()) { Text(post, Modifier.padding(16.dp)) } }
                            Text("서버 공유·댓글·신고·차단은 후속 구현 대상입니다.")
                        }
                        3 -> {
                            Text(if (result.eligible) "4주 달성 · 데모 보상 자격 충족" else "보상 확정 불가 · 4주 유지 검증 필요", style = MaterialTheme.typography.titleLarge)
                            Text("달성 쿠폰 5,000원 · 실쿠폰 아님 / 관리자 승인·검증 기간 미구현")
                            Button(enabled = result.eligible && !issued, onClick = {
                                // Durable idempotency for this single demo participant and season only.
                                if (!preferences.getBoolean("demo-issued", false)) {
                                    preferences.edit().putBoolean("demo-issued", true).commit()
                                }
                                issued = preferences.getBoolean("demo-issued", false)
                            }) { Text(if (issued) "데모 원장에 이미 기록됨" else "보상 요청 시뮬레이션") }
                            Text("원장: ${if (issued) "demo-season-1 / me / 달성 5,000원 / 시뮬레이션 완료" else "기록 없음"}")
                            Text("재요청·앱 재시작으로 중복 추가되지 않습니다. 실제 지급에는 서버 원장이 필요합니다.")
                        }
                    }
                    HorizontalDivider()
                    Text("테스트 점수 이력 전환")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("정상", "3주차 88점", "2주차 결측").forEachIndexed { index, label ->
                            FilterChip(selected = scenario == index, onClick = { scenario = index }, label = { Text(label) })
                        }
                    }
                }
                OutlinedButton(onClick = onOpenSdk) { Text("Pleos SDK 진단 화면") }
                TextButton(onClick = {
                    preferences.edit().clear().commit()
                    consent = false; issued = false; posts = emptyList(); draft = ""
                }) { Text("동의 철회 및 기기 내 데모 데이터 삭제") }
            }
            Text("차량 주행 상태 자동 감지 미연결 · 테스트 스위치는 실제 주행 제한을 보증하지 않습니다.", style = MaterialTheme.typography.bodySmall)
        }
    }
}
