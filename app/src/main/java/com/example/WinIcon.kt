package com.example

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun WinIconWindows(sizeDp: Dp = 24.dp) {
    Canvas(modifier = Modifier.size(sizeDp)) {
        val w = size.width
        val h = size.height
        val gap = w * 0.08f
        val halfW = w / 2 - gap / 2
        val halfH = h / 2 - gap / 2

        val blueLight = Color(0xFF00ADEF)
        val blueDark = Color(0xFF0078D7)
        val gradient = Brush.linearGradient(
            colors = listOf(blueLight, blueDark),
            start = Offset(0f, 0f),
            end = Offset(w, h)
        )

        // Top-left square
        drawRoundRect(
            brush = gradient,
            topLeft = Offset(0f, 0f),
            size = Size(halfW, halfH),
            cornerRadius = CornerRadius(w * 0.05f)
        )
        // Top-right square
        drawRoundRect(
            brush = gradient,
            topLeft = Offset(halfW + gap, 0f),
            size = Size(halfW, halfH),
            cornerRadius = CornerRadius(w * 0.05f)
        )
        // Bottom-left square
        drawRoundRect(
            brush = gradient,
            topLeft = Offset(0f, halfH + gap),
            size = Size(halfW, halfH),
            cornerRadius = CornerRadius(w * 0.05f)
        )
        // Bottom-right square
        drawRoundRect(
            brush = gradient,
            topLeft = Offset(halfW + gap, halfH + gap),
            size = Size(halfW, halfH),
            cornerRadius = CornerRadius(w * 0.05f)
        )
    }
}

@Composable
fun WinIconFolder(sizeDp: Dp = 48.dp) {
    Canvas(modifier = Modifier.size(sizeDp)) {
        val w = size.width
        val h = size.height

        // Folder back (Yellow-Orange solid)
        val backPath = Path().apply {
            moveTo(w * 0.05f, h * 0.85f)
            lineTo(w * 0.05f, h * 0.25f)
            lineTo(w * 0.35f, h * 0.25f)
            lineTo(w * 0.45f, h * 0.35f)
            lineTo(w * 0.95f, h * 0.35f)
            lineTo(w * 0.95f, h * 0.85f)
            close()
        }
        drawPath(
            path = backPath,
            brush = Brush.verticalGradient(listOf(Color(0xFFFDD835), Color(0xFFF57F17)))
        )

        // Inside document sheet (White mockup peek)
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(w * 0.15f, h * 0.3f),
            size = Size(w * 0.7f, h * 0.35f),
            cornerRadius = CornerRadius(w * 0.03f)
        )

        // Folder front flap (Semi-transparent overlay to look gorgeous)
        val frontPath = Path().apply {
            moveTo(w * 0.05f, h * 0.85f)
            lineTo(w * 0.05f, h * 0.4f)
            lineTo(w * 0.95f, h * 0.4f)
            lineTo(w * 0.95f, h * 0.85f)
            close()
        }
        drawPath(
            path = frontPath,
            brush = Brush.verticalGradient(listOf(Color(0xFFFFEE58), Color(0xFFF57C00)))
        )
    }
}

@Composable
fun WinIconThisPC(sizeDp: Dp = 48.dp) {
    Canvas(modifier = Modifier.size(sizeDp)) {
        val w = size.width
        val h = size.height

        // Monitor outer bezel (Glassy slate gradient)
        drawRoundRect(
            brush = Brush.verticalGradient(listOf(Color(0xFF50729B), Color(0xFF1E3550))),
            topLeft = Offset(w * 0.05f, h * 0.1f),
            size = Size(w * 0.9f, h * 0.55f),
            cornerRadius = CornerRadius(w * 0.06f)
        )

        // Screen (Vibrant sky blue)
        drawRoundRect(
            brush = Brush.verticalGradient(listOf(Color(0xFF00ADEF), Color(0xFF005691))),
            topLeft = Offset(w * 0.1f, h * 0.15f),
            size = Size(w * 0.8f, h * 0.45f),
            cornerRadius = CornerRadius(w * 0.03f)
        )

        // Stand support (Metal silver)
        drawRect(
            color = Color(0xFFB0BEC5),
            topLeft = Offset(w * 0.42f, h * 0.65f),
            size = Size(w * 0.16f, h * 0.15f)
        )

        // Base foot
        drawRoundRect(
            color = Color(0xFF78909C),
            topLeft = Offset(w * 0.25f, h * 0.78f),
            size = Size(w * 0.5f, h * 0.08f),
            cornerRadius = CornerRadius(w * 0.02f)
        )
    }
}

@Composable
fun WinIconRecycleBin(sizeDp: Dp = 48.dp, isEmpty: Boolean = true) {
    Canvas(modifier = Modifier.size(sizeDp)) {
        val w = size.width
        val h = size.height

        // Basket container (Acrylic glass frame)
        val mainPath = Path().apply {
            moveTo(w * 0.2f, h * 0.25f)
            lineTo(w * 0.25f, h * 0.85f)
            lineTo(w * 0.75f, h * 0.85f)
            lineTo(w * 0.8f, h * 0.25f)
            close()
        }
        
        // Solid light slate/cyan background
        drawPath(
            path = mainPath,
            brush = Brush.verticalGradient(listOf(Color(0x66B0BEC5), Color(0xBB78909C)))
        )

        // Grid lines to look like metal wastebasket
        for (i in 1..4) {
            val step = w * 0.2f + i * (w * 0.6f / 5f)
            drawLine(
                color = Color(0x44FFFFFF),
                start = Offset(step, h * 0.25f),
                end = Offset(step + (if (step > w * 0.5f) -w * 0.04f else w * 0.04f), h * 0.85f),
                strokeWidth = w * 0.03f
            )
        }

        // Lid top rim
        drawRoundRect(
            color = Color(0xFFCFD8DC),
            topLeft = Offset(w * 0.15f, h * 0.18f),
            size = Size(w * 0.7f, h * 0.08f),
            cornerRadius = CornerRadius(w * 0.02f)
        )

        // Contents (Crumpled mock paper sheets peek)
        if (!isEmpty) {
            drawCircle(
                color = Color.White,
                center = Offset(w * 0.4f, h * 0.42f),
                radius = w * 0.12f
            )
            drawCircle(
                color = Color(0xFFB0BEC5),
                center = Offset(w * 0.62f, h * 0.48f),
                radius = w * 0.09f
            )
            drawCircle(
                color = Color(0xFF90CAF9),
                center = Offset(w * 0.5f, h * 0.35f),
                radius = w * 0.11f
            )
        }
    }
}

@Composable
fun WinIconNotepad(sizeDp: Dp = 48.dp) {
    Canvas(modifier = Modifier.size(sizeDp)) {
        val w = size.width
        val h = size.height

        // Under layer pad body (Deep sky-blue spine)
        drawRoundRect(
            color = Color(0xFF0078D7),
            topLeft = Offset(w * 0.1f, h * 0.15f),
            size = Size(w * 0.8f, h * 0.75f),
            cornerRadius = CornerRadius(w * 0.05f)
        )

        // Paper sheet (White)
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(w * 0.18f, h * 0.2f),
            size = Size(w * 0.7f, h * 0.65f),
            cornerRadius = CornerRadius(w * 0.02f)
        )

        // Blue writing lines
        drawLine(
            color = Color(0x440078D7),
            start = Offset(w * 0.28f, h * 0.35f),
            end = Offset(w * 0.78f, h * 0.35f),
            strokeWidth = 2.dp.toPx()
        )
        drawLine(
            color = Color(0x440078D7),
            start = Offset(w * 0.28f, h * 0.5f),
            end = Offset(w * 0.78f, h * 0.5f),
            strokeWidth = 2.dp.toPx()
        )
        drawLine(
            color = Color(0x440078D7),
            start = Offset(w * 0.28f, h * 0.65f),
            end = Offset(w * 0.68f, h * 0.65f),
            strokeWidth = 2.dp.toPx()
        )

        // Pen diagonal (Orange/Yellow)
        val penPath = Path().apply {
            moveTo(w * 0.65f, h * 0.8f)
            lineTo(w * 0.85f, h * 0.6f)
            lineTo(w * 0.9f, h * 0.65f)
            lineTo(w * 0.7f, h * 0.85f)
            close()
        }
        drawPath(
            path = penPath,
            brush = Brush.linearGradient(listOf(Color(0xFFFF9800), Color(0xFFFF5722)))
        )
    }
}

@Composable
fun WinIconCalculator(sizeDp: Dp = 24.dp) {
    Canvas(modifier = Modifier.size(sizeDp)) {
        val w = size.width
        val h = size.height

        // Outer body (Teal accent slate)
        drawRoundRect(
            color = Color(0xFF1E2A38),
            topLeft = Offset(0f, 0f),
            size = Size(w, h),
            cornerRadius = CornerRadius(w * 0.08f)
        )

        // Screen (Cyan neon top area)
        drawRoundRect(
            color = Color(0xFF0D9488),
            topLeft = Offset(w * 0.12f, h * 0.12f),
            size = Size(w * 0.76f, h * 0.22f),
            cornerRadius = CornerRadius(w * 0.04f)
        )

        // Keypad buttons
        val rows = 3
        val cols = 3
        val keyGap = w * 0.06f
        val keyW = (w * 0.76f - (keyGap * 2)) / cols
        val keyH = (h * 0.45f - (keyGap * 2)) / rows

        for (r in 0 until rows) {
            for (c in 0 until cols) {
                drawRoundRect(
                    color = Color(0x55FFFFFF),
                    topLeft = Offset(w * 0.12f + c * (keyW + keyGap), h * 0.42f + r * (keyH + keyGap)),
                    size = Size(keyW, keyH),
                    cornerRadius = CornerRadius(w * 0.02f)
                )
            }
        }
    }
}

@Composable
fun WinIconSettings(sizeDp: Dp = 24.dp) {
    Canvas(modifier = Modifier.size(sizeDp)) {
        val w = size.width
        val h = size.height
        val cx = w / 2
        val cy = h / 2
        val rOuter = w * 0.35f
        val rInner = w * 0.15f

        // Draw gear circle
        drawCircle(
            color = Color(0xFF607D8B),
            center = Offset(cx, cy),
            radius = rOuter
        )

        // Draw teeth
        val teethCount = 8
        val strokeW = w * 0.12f
        val teethLength = w * 0.1f
        for (i in 0 until teethCount) {
            val angle = i * (360f / teethCount)
            val rad = Math.toRadians(angle.toDouble())
            val startX = cx + (rOuter - w * 0.05f) * Math.cos(rad).toFloat()
            val startY = cy + (rOuter - w * 0.05f) * Math.sin(rad).toFloat()
            val endX = cx + (rOuter + teethLength) * Math.cos(rad).toFloat()
            val endY = cy + (rOuter + teethLength) * Math.sin(rad).toFloat()

            drawLine(
                color = Color(0xFF607D8B),
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = strokeW
            )
        }

        // Inner circle cutout hole
        drawCircle(
            color = Color(0x00FFFFFF),
            center = Offset(cx, cy),
            radius = rInner,
            blendMode = BlendMode.Clear
        )
        // Re-draw inner rim
        drawCircle(
            color = Color(0xFF37474F),
            center = Offset(cx, cy),
            radius = rInner,
            style = Stroke(width = w * 0.04f)
        )
    }
}
