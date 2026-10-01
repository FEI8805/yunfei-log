package com.yunfei.log.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yunfei.log.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 启动页：竖排「云飞日志」大字，流光只在文字内部扫过。
 * 字体：汉仪雪君体繁（res/font/xuejunti.ttf），需授权的商用字体，已随工程打包。
 */
private val XueJunTi = FontFamily(Font(R.font.xuejunti))

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    var visible by remember { mutableStateOf(true) }
    var finished by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun finish() {
        if (finished) return
        finished = true
        visible = false
        scope.launch {
            delay(600)
            onFinished()
        }
    }

    LaunchedEffect(Unit) {
        delay(2600)
        finish()
    }

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(600),
        label = "splashAlpha"
    )
    val transition = rememberInfiniteTransition(label = "splash")
    val ring1 by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(18000, easing = LinearEasing)),
        label = "ring1"
    )
    val ring2 by transition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(26000, easing = LinearEasing)),
        label = "ring2"
    )
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2800, easing = LinearEasing)),
        label = "phase"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0910))
            .alpha(alpha)
            .pointerInput(Unit) { detectTapGestures { finish() } },
        contentAlignment = Alignment.Center
    ) {
        // 背景网格
        Canvas(modifier = Modifier.fillMaxSize()) {
            val step = 34.dp.toPx()
            var x = 0f
            while (x < size.width) {
                drawLine(Color(0x1A78AAFF), Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
                x += step
            }
            var y = 0f
            while (y < size.height) {
                drawLine(Color(0x1A78AAFF), Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
                y += step
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 文字与光环放在同一个容器里，保证光环对齐文字
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(330.dp)
                        .background(
                            Brush.radialGradient(
                                listOf(Color(0x995B3DF5), Color(0x005B3DF5))
                            ),
                            shape = CircleShape
                        )
                )
                Canvas(
                    modifier = Modifier
                        .size(272.dp)
                        .graphicsLayer { rotationZ = ring1 }
                ) {
                    drawCircle(
                        color = Color(0x4078AAFF),
                        radius = size.minDimension / 2f - 1f,
                        style = Stroke(width = 1.5f)
                    )
                }
                Canvas(
                    modifier = Modifier
                        .size(364.dp)
                        .graphicsLayer { rotationZ = ring2 }
                ) {
                    drawCircle(
                        color = Color(0x3378AAFF),
                        radius = size.minDimension / 2f - 1f,
                        style = Stroke(
                            width = 2f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 12f))
                        )
                    )
                }

                Text(
                    text = "云\n飞\n日\n志",
                    color = Color.White,
                    fontFamily = XueJunTi,
                    fontSize = 84.sp,
                    lineHeight = 98.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
                        .drawWithContent {
                            drawContent()
                            val h = size.height
                            val band = h * 0.5f
                            val y = -band + (h + band * 2f) * phase
                            drawRect(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFFE8F6FF), Color(0xFF7FD0FF), Color.White,
                                        Color(0xFFC7E4FF), Color(0xFF5E9BE8), Color.White,
                                        Color(0xFFA9CDFA)
                                    ),
                                    start = Offset(size.width / 2f, y - band / 2f),
                                    end = Offset(size.width / 2f, y + band / 2f)
                                ),
                                blendMode = BlendMode.SrcIn
                            )
                        }
                )
            }

            Spacer(Modifier.height(26.dp))
            Text(
                text = "记 录 每 一 天",
                color = Color(0xFF9FB4E8),
                fontSize = 12.sp,
                letterSpacing = 6.sp
            )
        }

        Text(
            text = "轻触屏幕跳过",
            color = Color(0xFF6E7CA8),
            fontSize = 11.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 34.dp)
        )
    }
}
