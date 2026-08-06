package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object MoodColors {
    fun getMoodHue(mood: String): Float {
        return when (mood.lowercase()) {
            "joy", "neşeli" -> 38f
            "warm", "sıcak" -> 340f
            "sad", "hüzünlü" -> 250f
            "tense", "gergin" -> 8f
            "curious", "meraklı" -> 275f
            else -> 200f // calm / sakin
        }
    }

    fun getMoodLabel(mood: String): String {
        return when (mood.lowercase()) {
            "joy" -> "neşeli"
            "warm" -> "sıcak"
            "sad" -> "hüzünlü"
            "tense" -> "gergin"
            "curious" -> "meraklı"
            else -> "sakin"
        }
    }
}

@Composable
fun OrbView(
    hue: Float,
    size: Dp = 44.dp,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orbPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val coreColor = Color.hsl(hue = hue, saturation = 0.70f, lightness = 0.72f)
    val outerColor = Color.hsl(hue = hue, saturation = 0.55f, lightness = 0.42f)
    val glowColor = Color.hsl(hue = hue, saturation = 0.70f, lightness = 0.60f, alpha = 0.35f)

    Canvas(modifier = modifier.size(size)) {
        val radius = (this.size.minDimension / 2f) * pulseScale
        val center = Offset(this.size.width / 2f, this.size.height / 2f)

        // Outer glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(glowColor, Color.Transparent),
                center = center,
                radius = radius * 1.5f
            ),
            radius = radius * 1.5f,
            center = center
        )

        // Core orb
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(coreColor, outerColor),
                center = Offset(center.x * 0.85f, center.y * 0.75f),
                radius = radius
            ),
            radius = radius,
            center = center
        )
    }
}
