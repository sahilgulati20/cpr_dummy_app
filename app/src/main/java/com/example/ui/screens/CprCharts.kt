package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CprDistributionChart(
    slowCount: Int,
    goodCount: Int,
    fastCount: Int,
    title: String,
    labels: Triple<String, String, String>,
    colors: Triple<Color, Color, Color> = Triple(
        Color(0xFFFFB300), // Amber (slow/shallow)
        Color(0xFF2E7D32), // Emerald (good)
        Color(0xFFC62828)  // Crimson Red (fast/deep)
    )
) {
    val maxCount = (maxOf(slowCount, goodCount, fastCount, 1)).toFloat()
    
    // Animated progress states
    val animateSlow = animateFloatAsState(targetValue = slowCount / maxCount, animationSpec = tween(800), label = "")
    val animateGood = animateFloatAsState(targetValue = goodCount / maxCount, animationSpec = tween(800), label = "")
    val animateFast = animateFloatAsState(targetValue = fastCount / maxCount, animationSpec = tween(800), label = "")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(24.dp))
            .border(1.dp, Color(0xFFF4F2F4), RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = Color(0xFF1D1B1E),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            ChartBar(
                fraction = animateSlow.value,
                count = slowCount,
                label = labels.first,
                color = colors.first
            )
            ChartBar(
                fraction = animateGood.value,
                count = goodCount,
                label = labels.second,
                color = colors.second
            )
            ChartBar(
                fraction = animateFast.value,
                count = fastCount,
                label = labels.third,
                color = colors.third
            )
        }
    }
}

@Composable
fun ChartBar(
    fraction: Float,
    count: Int,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(72.dp)
    ) {
        Text(
            text = count.toString(),
            color = Color(0xFF1D1B1E),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        
        Box(
            modifier = Modifier
                .width(28.dp)
                .height(115.dp)
                .background(Color(0xFFF4F2F4), RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .align(Alignment.CenterHorizontally)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(fraction)
                    .background(color, RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                    .align(Alignment.BottomCenter)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            color = Color(0xFF49454F),
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun MetricGauge(
    pct: Double,
    title: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val animatedProgress = animateFloatAsState(targetValue = pct.toFloat() / 100f, animationSpec = tween(1000), label = "")

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .background(Color.White, RoundedCornerShape(24.dp))
            .border(1.dp, Color(0xFFF4F2F4), RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = Color(0xFF49454F),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(100.dp)
            ) {
                Canvas(modifier = Modifier.size(100.dp)) {
                    // Draw outer track
                    drawCircle(
                        color = Color(0xFFF4F2F4),
                        radius = size.minDimension / 2,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 12.dp.toPx())
                    )
                    // Draw progress arc
                    drawArc(
                        color = color,
                        startAngle = -90f,
                        sweepAngle = animatedProgress.value * 360f,
                        useCenter = false,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = 12.dp.toPx(),
                            cap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                    )
                }
                Text(
                    text = "${pct.toInt()}%",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(0xFF1D1B1E),
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}
