package com.masrofy.app

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** شاشة التحميل: أنيميشن بسيط ثم ينتقل للتطبيق. */
@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val scale = remember { Animatable(0.4f) }
    val ring = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }
    val bar = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        scale.animateTo(
            1f,
            spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
        )
    }
    LaunchedEffect(Unit) {
        ring.animateTo(1f, tween(durationMillis = 1500, easing = FastOutSlowInEasing))
    }
    LaunchedEffect(Unit) {
        textAlpha.animateTo(1f, tween(durationMillis = 700, delayMillis = 500))
    }
    LaunchedEffect(Unit) {
        bar.animateTo(1f, tween(durationMillis = 2000, easing = LinearEasing))
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF12B573), Color(0xFF0B6B4B)))),
        contentAlignment = Alignment.Center
    ) {
        Skyline(modifier = Modifier.align(Alignment.BottomCenter))
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(bottom = 60.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .graphicsLayer {
                        scaleX = scale.value
                        scaleY = scale.value
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Ic.Wallet,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(118.dp)
                )
                Icon(
                    Ic.Sparkle,
                    contentDescription = null,
                    tint = Color(0xFFB6F23A),
                    modifier = Modifier
                        .size(60.dp)
                        .align(Alignment.TopEnd)
                        .alpha(ring.value)
                )
            }
            Text(
                "مصروفي",
                color = Color.White,
                fontSize = 46.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.alpha(textAlpha.value)
            )
            Text(
                "تابع فلوسك بسهولة",
                color = Color.White.copy(alpha = 0.92f),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.alpha(textAlpha.value)
            )
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(3.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFB6F23A).copy(alpha = 0.9f * textAlpha.value))
            )
        }
        LinearProgressIndicator(
            progress = { bar.value },
            color = Color.White,
            trackColor = Color.White.copy(alpha = 0.2f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 24.dp)
                .width(140.dp)
        )
    }
}

/** خط أفق مصري: أهرامات ومئذنة ونخيل. */
@Composable
private fun Skyline(modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(170.dp)
    ) {
        val w = size.width
        val h = size.height
        val back = Color(0xFF0B6B4B).copy(alpha = 0.55f)
        val front = Color(0xFF0A5A3C).copy(alpha = 0.8f)
        // أهرامات
        val p1 = androidx.compose.ui.graphics.Path().apply {
            moveTo(w * 0.52f, h); lineTo(w * 0.70f, h * 0.38f); lineTo(w * 0.88f, h); close()
        }
        val p2 = androidx.compose.ui.graphics.Path().apply {
            moveTo(w * 0.72f, h); lineTo(w * 0.86f, h * 0.55f); lineTo(w * 1.0f, h); close()
        }
        drawPath(p1, back)
        drawPath(p2, back)
        // مئذنة وقبة
        drawRect(front, Offset(w * 0.34f, h * 0.30f), Size(w * 0.025f, h * 0.70f))
        drawCircle(front, w * 0.03f, Offset(w * 0.3425f, h * 0.28f))
        drawRect(front, Offset(w * 0.20f, h * 0.62f), Size(w * 0.16f, h * 0.38f))
        drawArc(front, 180f, 180f, true, Offset(w * 0.22f, h * 0.42f), Size(w * 0.12f, h * 0.40f))
        // مباني
        drawRect(front, Offset(0f, h * 0.78f), Size(w * 0.20f, h * 0.22f))
        drawRect(front, Offset(w * 0.40f, h * 0.74f), Size(w * 0.10f, h * 0.26f))
        // نخيل
        listOf(w * 0.08f to 0.55f, w * 0.14f to 0.66f, w * 0.60f to 0.62f, w * 0.94f to 0.6f).forEach { (x, top) ->
            drawLine(front, Offset(x, h), Offset(x, h * top), strokeWidth = w * 0.008f)
            for (a in listOf(-50f, -20f, 20f, 50f)) {
                val rad = Math.toRadians((a - 90).toDouble())
                val len = w * 0.045f
                drawLine(
                    front,
                    Offset(x, h * top),
                    Offset(x + (len * Math.cos(rad)).toFloat(), h * top + (len * Math.sin(rad)).toFloat() + len * 0.5f),
                    strokeWidth = w * 0.006f
                )
            }
        }
        drawRect(front, Offset(0f, h * 0.94f), Size(w, h * 0.06f))
    }
}
