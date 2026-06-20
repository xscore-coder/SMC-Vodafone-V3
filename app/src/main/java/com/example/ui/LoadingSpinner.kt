package com.vodafone.smc.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

enum class SMCSpinnerSize {
    SMALL, MEDIUM, LARGE
}

/**
 * A highly polished, custom-designed double-concentric animated progress spinner.
 * Standard CircularProgressIndicator is replaced with this bespoke design to match
 * the corporate identity.
 */
@Composable
fun SMCLoadingSpinner(
    modifier: Modifier = Modifier,
    size: SMCSpinnerSize = SMCSpinnerSize.MEDIUM,
    color: Color = Color(0xFFE60000), // Corporate red accent
    secondaryColor: Color = Color(0xFFED1C24).copy(alpha = 0.25f),
    strokeWidth: Dp = if (size == SMCSpinnerSize.SMALL) 2.5.dp else 4.dp
) {
    val sizePx = when (size) {
        SMCSpinnerSize.SMALL -> 22.dp
        SMCSpinnerSize.MEDIUM -> 48.dp
        SMCSpinnerSize.LARGE -> 72.dp
    }

    val infiniteTransition = rememberInfiniteTransition(label = "smc_spinner")
    
    // Outer ring rotation
    val angleOuter by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "outer_angle"
    )

    // Inner reverse ring rotation for complex premium tech aesthetic
    val angleInner by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "inner_angle"
    )

    Box(
        modifier = modifier.size(sizePx),
        contentAlignment = Alignment.Center
    ) {
        // Outer concentric animated arc
        Canvas(modifier = Modifier.fillMaxSize().rotate(angleOuter)) {
            val strokeWidthPx = strokeWidth.toPx()
            val canvasSize = this.size
            val diameter = canvasSize.width - strokeWidthPx
            
            // Background track
            drawCircle(
                color = secondaryColor,
                radius = diameter / 2f,
                center = center,
                style = Stroke(width = strokeWidthPx)
            )

            // Primary active loading sweep
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 260f,
                useCenter = false,
                topLeft = Offset(strokeWidthPx / 2f, strokeWidthPx / 2f),
                size = Size(diameter, diameter),
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
            )
        }

        // Inner concentric arc moving in reversed direction if size allows it
        if (size != SMCSpinnerSize.SMALL) {
            val innerPadding = when (size) {
                SMCSpinnerSize.MEDIUM -> 10.dp
                SMCSpinnerSize.LARGE -> 16.dp
                else -> 8.dp
            }
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .rotate(angleInner)
            ) {
                val strokeWidthInnerPx = (strokeWidth.toPx() * 0.75f).coerceAtLeast(4f)
                val canvasSize = this.size
                val diameter = canvasSize.width - strokeWidthInnerPx
                
                drawArc(
                    color = color.copy(alpha = 0.65f),
                    startAngle = 90f,
                    sweepAngle = 140f,
                    useCenter = false,
                    topLeft = Offset(strokeWidthInnerPx / 2f, strokeWidthInnerPx / 2f),
                    size = Size(diameter, diameter),
                    style = Stroke(width = strokeWidthInnerPx, cap = StrokeCap.Round)
                )
            }
        }
    }
}

/**
 * A beautiful composite state view displaying the custom spinner, an animated status subtitle,
 * and elegant layout. It's designed to replace boring traditional circular indicator layouts.
 */
@Composable
fun SMCSpinnerWithStatus(
    status: String,
    modifier: Modifier = Modifier,
    statusSubtitle: String? = null,
    size: SMCSpinnerSize = SMCSpinnerSize.MEDIUM
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        SMCLoadingSpinner(size = size)
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = status,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )
        if (statusSubtitle != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = statusSubtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                maxLines = 1
            )
        }
    }
}

/**
 * A reusable overlay component that darkens/dims the content and displays a beautiful,
 * responsive loading dialogue layout. Best for blocking UI during massive synchronous tasks.
 */
@Composable
fun SMCOverlaySpinner(
    isVisible: Boolean,
    status: String = "Syncing with cloud network...",
    statusSubtitle: String? = "Decrypting local database sector...",
    onDismiss: (() -> Unit)? = null
) {
    if (isVisible) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.58f))
                .clickable(enabled = onDismiss != null) { onDismiss?.invoke() },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .widthIn(max = 310.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                        RoundedCornerShape(24.dp)
                    )
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                SMCSpinnerWithStatus(
                    status = status,
                    statusSubtitle = statusSubtitle,
                    size = SMCSpinnerSize.LARGE
                )
            }
        }
    }
}
