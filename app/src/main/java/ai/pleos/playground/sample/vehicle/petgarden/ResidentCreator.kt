package ai.pleos.playground.sample.vehicle.petgarden

import ai.pleos.playground.sample.vehicle.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
internal fun ResidentPortrait(species: Species, badge: Badge, modifier: Modifier = Modifier) {
    val art = painterResource(when (species) {
        Species.RABBIT -> R.drawable.resident_rabbit
        Species.BEAR -> R.drawable.resident_bear
        Species.CAT -> R.drawable.resident_cat
    })
    Image(art, "${species.label} 주민 · ${badge.label} 배지", modifier.drawWithContent {
        drawContent()
        // Anchor the pin to the jacket after the same uniform Fit transform as the sprite.
        val fit = minOf(size.width / art.intrinsicSize.width, size.height / art.intrinsicSize.height)
        val w = art.intrinsicSize.width * fit
        val h = art.intrinsicSize.height * fit
        val chestY = if (species == Species.RABBIT) .684f else .642f
        val x = (size.width - w) / 2 + w * .66f
        val y = (size.height - h) / 2 + h * chestY
        val r = h * .039f
        val center = Offset(x, y)
        val enamel = when (badge) {
            Badge.LEAF -> Color(0xFF47755A)
            Badge.SUN -> Color(0xFFC17832)
            Badge.MOON -> Color(0xFF63588D)
        }
        val gold = Color(0xFFFFE8A9)
        drawCircle(Color(0x66382B1D), r * 1.09f, center + Offset(0f, .12f * r))
        drawCircle(gold, r, center)
        drawCircle(enamel, r * .85f, center)
        when (badge) {
            Badge.LEAF -> drawPath(Path().apply {
                moveTo(x - .44f * r, y + .47f * r)
                quadraticBezierTo(x - .67f * r, y - .26f * r, x + .46f * r, y - .49f * r)
                quadraticBezierTo(x + .53f * r, y + .48f * r, x - .44f * r, y + .47f * r)
                close()
            }, gold)
            Badge.SUN -> {
                drawCircle(gold, r * .33f, center)
                listOf(Offset(0f, .6f), Offset(0f, -.6f), Offset(.6f, 0f), Offset(-.6f, 0f)).forEach {
                    drawCircle(gold, r * .1f, center + it * r)
                }
            }
            Badge.MOON -> {
                drawCircle(gold, r * .53f, center)
                drawCircle(enamel, r * .43f, center + Offset(.23f * r, -.16f * r))
            }
        }
    })
}

@Composable
internal fun Plemong(modifier: Modifier = Modifier) {
    Image(painterResource(R.drawable.neighbor_plemong), "이웃 주민 플레몽", modifier)
}

@Composable
internal fun ResidentCreator(returningPet: String, onMoveIn: (String, Species, Badge) -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    var species by rememberSaveable { mutableStateOf(Species.RABBIT) }
    var badge by rememberSaveable { mutableStateOf(Badge.LEAF) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("내 주민 만들기", style = MaterialTheme.typography.headlineSmall)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Plemong(Modifier.size(56.dp))
            Column {
                Text("이웃 주민 · 플레몽", style = MaterialTheme.typography.labelLarge)
                Text(if (returningPet.isBlank()) "반가워! 마을에서 뭐라고 불러줄까?" else "${returningPet}도 기다리고 있어. 네 이름을 알려줘!")
            }
        }
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val preview: @Composable (Modifier) -> Unit = { modifier ->
                Box(modifier.background(Color(0xFFEDF0DB), RoundedCornerShape(24.dp)), contentAlignment = Alignment.Center) {
                    ResidentPortrait(species, badge, Modifier.fillMaxSize().padding(12.dp))
                }
            }
            val controls: @Composable (Modifier) -> Unit = { modifier ->
                Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = Resident.limitName(it) },
                        label = { Text("주민 이름 · 최대 12자") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Text("모습", style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Species.entries.forEach { choice ->
                            FilterChip(selected = choice == species, onClick = { species = choice }, label = { Text(choice.label) })
                        }
                    }
                    Text("옷깃 배지", style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Badge.entries.forEach { choice ->
                            FilterChip(selected = choice == badge, onClick = { badge = choice }, label = { Text(choice.label) })
                        }
                    }
                    Button(enabled = name.isNotBlank(), onClick = { onMoveIn(name, species, badge) }) { Text("이 모습으로 입주") }
                }
            }
            if (maxWidth >= 540.dp) {
                Row(horizontalArrangement = Arrangement.spacedBy(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    preview(Modifier.weight(.8f).height(246.dp))
                    controls(Modifier.weight(1.2f))
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    preview(Modifier.fillMaxWidth().height(172.dp))
                    controls(Modifier.fillMaxWidth())
                }
            }
        }
    }
}
