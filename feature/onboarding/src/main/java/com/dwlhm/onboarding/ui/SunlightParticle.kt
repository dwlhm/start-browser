package com.dwlhm.onboarding.ui

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun SoftGlowParticles(
    modifier: Modifier = Modifier,
    particleCount: Int = 9
) {
    val particles = remember {
        List(particleCount) {
            GlowParticleAlive(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                baseRadius = Random.nextInt(180, 380).toFloat(),

                // Pergerakan dasar (lebih cepat & terasa)
                speedX = Random.nextFloat() * 0.03f + 0.006f,
                speedY = Random.nextFloat() * 0.03f + 0.006f,

                // Warp sinusoidal
                waveX = Random.nextFloat() * 40f + 30f,
                waveY = Random.nextFloat() * 40f + 30f,
                waveSpeed = Random.nextFloat() * 2f + 0.5f,

                color = Color(
                    red = 0.9f + Random.nextFloat() * 0.1f,
                    green = 0.8f + Random.nextFloat() * 0.2f,
                    blue = 0.4f + Random.nextFloat() * 0.2f,
                    alpha = 1f
                )
            )
        }
    }

    val infinite = rememberInfiniteTransition(label = "")

    // TIME 0 → 1 → repeat
    val t by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(4500, easing = LinearOutSlowInEasing)
        ),
        label = ""
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        particles.forEachIndexed { index, p ->

            // ---------------------
            // 1) Drift Linear
            // ---------------------
            val dx = ((p.x + p.speedX * t) % 1f) * w
            val dy = ((p.y + p.speedY * t) % 1f) * h

            // ---------------------
            // 2) Wave Movement (sinusoidal)
            // ---------------------
            val waveT = t * p.waveSpeed
            val wx = sin(waveT * 6.28f + index) * p.waveX
            val wy = cos(waveT * 6.28f + index) * p.waveY

            val finalX = dx + wx
            val finalY = dy + wy

            // ---------------------
            // 3) Breathing Radius
            // ---------------------
            val radius = p.baseRadius * (0.85f + sin(waveT * 3f) * 0.15f)

            // ---------------------
            // 4) Multi-layer Glow
            // ---------------------

            // Outer soft halo
            val outer = Brush.radialGradient(
                colors = listOf(
                    p.color.copy(alpha = 0.28f),
                    Color.Transparent
                ),
                center = Offset(finalX, finalY),
                radius = radius * 2.2f
            )

            drawCircle(
                brush = outer,
                radius = radius * 2.2f,
                center = Offset(finalX, finalY)
            )

            // Inner glow
            val inner = Brush.radialGradient(
                colors = listOf(
                    p.color.copy(alpha = 0.55f),
                    Color.Transparent
                ),
                center = Offset(finalX, finalY),
                radius = radius
            )

            drawCircle(
                brush = inner,
                radius = radius,
                center = Offset(finalX, finalY)
            )
        }
    }
}


data class GlowParticleAlive(
    val x: Float,
    val y: Float,

    val baseRadius: Float,   // radius dasar sebelum breathing
    val speedX: Float,
    val speedY: Float,

    val waveX: Float,
    val waveY: Float,
    val waveSpeed: Float,

    val color: Color
)
