package ai.pleos.playground.sample.vehicle.petgarden

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp

/** World positions follow the clearing. Portraits retain their intrinsic aspect ratios. */
@Composable
fun GardenScene(state: GardenState, village: Boolean = false) {
    BoxWithConstraints(Modifier.fillMaxWidth().height(300.dp).clipToBounds()) {
        Clearing(state, Modifier.fillMaxSize())
        state.resident?.let { resident ->
            ResidentPortrait(resident.species, resident.badge, Modifier.align(Alignment.BottomCenter)
                .offset(x = maxWidth * -.19f, y = -42.dp)
                .width(maxWidth * .23f).height(150.dp))
        }
        if (village) Plemong(Modifier.align(Alignment.BottomCenter)
            .offset(x = maxWidth * .25f, y = -46.dp)
            .width(maxWidth * .23f).height(160.dp))
    }
}

@Composable
private fun Clearing(state: GardenState, modifier: Modifier) {
    Canvas(modifier) {
        val sx = size.width / 900f
        val sy = size.height / 340f
        fun oval(color: Color, x: Float, y: Float, w: Float, h: Float) =
            drawOval(color, Offset(x * sx, y * sy), Size(w * sx, h * sy))
        fun rect(color: Color, x: Float, y: Float, w: Float, h: Float) =
            drawRect(color, Offset(x * sx, y * sy), Size(w * sx, h * sy))
        drawRect(Color(0xFFE8F1DD))
        oval(Color(0xFFFFE9A5), 700f, 22f, 75f, 75f)
        oval(Color(0xFFCBDDB4), -100f, 115f, 700f, 260f)
        oval(Color(0xFFBAD3A0), 400f, 120f, 700f, 260f)
        oval(Color(0xFFE7D5AE), 230f, 225f, 450f, 100f)
        listOf(65f, 155f, 760f, 840f).forEach { x ->
            rect(Color(0xFF977653), x, 110f, 15f, 130f)
            oval(Color(0xFF648C67), x - 48, 45f, 110f, 145f)
            oval(Color(0xFF789C71), x - 35, 40f, 85f, 95f)
        }
        rect(Color(0xFFF8EDD5), 270f, 120f, 150f, 110f)
        val roof = Path().apply { moveTo(245 * sx, 125 * sy); lineTo(345 * sx, 50 * sy); lineTo(445 * sx, 125 * sy); close() }
        drawPath(roof, Color(0xFFB57D65))
        oval(Color(0xFF9E785D), 320f, 160f, 45f, 75f)
        rect(Color(0xFFB3D7D1), 285f, 150f, 25f, 28f)
        fun sheep(x: Float, y: Float, tint: Color) {
            oval(Color(0x33917D5C), x - 38, y + 55, 110f, 23f)
            rect(Color(0xFF856E5D), x - 14, y + 42, 12f, 25f)
            rect(Color(0xFF856E5D), x + 38, y + 42, 12f, 25f)
            listOf(-25f to 8f, -5f to -6f, 20f to -8f, 40f to 8f, 20f to 28f, -5f to 28f).forEach { (dx, dy) -> oval(tint, x + dx - 12, y + dy, 49f, 48f) }
            oval(Color(0xFFE4CDB3), x + 3, y + 8, 43f, 43f)
            oval(Color(0xFFE4CDB3), x - 9, y + 5, 20f, 13f)
            oval(Color(0xFFE4CDB3), x + 36, y + 5, 20f, 13f)
            oval(Color(0xFF494D42), x + 13, y + 24, 4f, 6f)
            oval(Color(0xFF494D42), x + 31, y + 24, 4f, 6f)
            oval(Color(0xFFE3A69A), x + 8, y + 33, 9f, 5f)
            oval(Color(0xFFE3A69A), x + 33, y + 33, 9f, 5f)
        }
        sheep(475f, 202f, Color(0xFFFFFCED))
        if (Decoration.FLOWERS.name in state.placed) {
            listOf(400f, 430f, 460f).forEach { x ->
                rect(Color(0xFF789758), x, 275f, 4f, 25f)
                oval(Color(0xFFE8AFA6), x - 9, 265f, 23f, 20f)
                oval(Color(0xFFFFE7A6), x - 2, 271f, 9f, 9f)
            }
        }
        if (Decoration.LAMP.name in state.placed) {
            rect(Color(0xFF8A745A), 590f, 153f, 8f, 95f)
            oval(Color(0xFFFFF2B6), 573f, 140f, 42f, 40f)
        }
        if (Decoration.PICNIC.name in state.placed) {
            oval(Color(0xFFD99982), 630f, 275f, 120f, 35f)
            oval(Color(0xFFF7E6C6), 663f, 278f, 45f, 20f)
        }
    }
}
