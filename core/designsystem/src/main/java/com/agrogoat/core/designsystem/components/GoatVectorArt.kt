package com.agrogoat.core.designsystem.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke

/**
 * Renders the cute white promo goat seen on the green banner using Canvas
 */
@Composable
fun CutePromoGoat(
    modifier: Modifier = Modifier,
    bodyColor: Color = Color.White,
    cheekColor: Color = Color(0xFFFFCDD2), // soft pink
    hornColor: Color = Color(0xFFEEEEEE),
    accentColor: Color = Color(0xFF1E6F38)
) {
    val leftHornPath = remember { Path() }
    val rightHornPath = remember { Path() }
    val leftEar = remember { Path() }

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Scale factors relative to box
        val cx = w * 0.52f
        val cy = h * 0.53f
        val rx = w * 0.32f
        val ry = h * 0.24f

        // Draw green background circle accents
        drawCircle(
            color = Color.White.copy(alpha = 0.12f),
            radius = w * 0.35f,
            center = Offset(w * 0.82f, h * 0.45f)
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.08f),
            radius = w * 0.22f,
            center = Offset(w * 0.15f, h * 0.72f)
        )

        // Draw 4 stick legs
        val legWidth = w * 0.016f
        val legLen = h * 0.16f
        // Back-left leg
        drawLine(
            color = bodyColor,
            start = Offset(cx - rx * 0.6f, cy + ry * 0.7f),
            end = Offset(cx - rx * 0.6f - w * 0.04f, cy + ry * 0.7f + legLen),
            strokeWidth = legWidth
        )
        // Back-right leg
        drawLine(
            color = bodyColor,
            start = Offset(cx - rx * 0.2f, cy + ry * 0.8f),
            end = Offset(cx - rx * 0.2f - w * 0.01f, cy + ry * 0.8f + legLen),
            strokeWidth = legWidth
        )
        // Front-left leg
        drawLine(
            color = bodyColor,
            start = Offset(cx + rx * 0.3f, cy + ry * 0.8f),
            end = Offset(cx + rx * 0.3f + w * 0.01f, cy + ry * 0.8f + legLen),
            strokeWidth = legWidth
        )
        // Front-right leg
        drawLine(
            color = bodyColor,
            start = Offset(cx + rx * 0.7f, cy + ry * 0.6f),
            end = Offset(cx + rx * 0.7f + w * 0.04f, cy + ry * 0.6f + legLen),
            strokeWidth = legWidth
        )

        // Draw oval Body
        drawOval(
            color = bodyColor,
            topLeft = Offset(cx - rx, cy - ry),
            size = Size(rx * 2f, ry * 2f)
        )

        // Draw Horns (Cute curved antennas/horns)
        leftHornPath.reset()
        leftHornPath.moveTo(cx + rx * 0.45f, cy - ry * 0.65f)
        leftHornPath.quadraticTo(
            cx + rx * 0.6f, cy - ry * 1.35f,
            cx + rx * 0.55f, cy - ry * 1.45f
        )

        rightHornPath.reset()
        rightHornPath.moveTo(cx + rx * 0.65f, cy - ry * 0.45f)
        rightHornPath.quadraticTo(
            cx + rx * 0.95f, cy - ry * 0.95f,
            cx + rx * 1.05f, cy - ry * 0.98f
        )
        drawPath(leftHornPath, color = hornColor, style = Stroke(width = w * 0.018f))
        drawPath(rightHornPath, color = hornColor, style = Stroke(width = w * 0.018f))

        // Draw Ears
        leftEar.reset()
        leftEar.moveTo(cx + rx * 0.32f, cy - ry * 0.35f)
        leftEar.quadraticTo(cx + rx * 0.15f, cy - ry * 0.65f, cx + rx * 0.22f, cy - ry * 0.72f)
        leftEar.close()
        drawPath(leftEar, color = bodyColor)

        // Draw Face Details (Eyes & Cheeks)
        // Cheek (pink circle)
        drawCircle(
            color = cheekColor,
            radius = w * 0.032f,
            center = Offset(cx + rx * 0.65f, cy + ry * 0.12f)
        )
        // Eye (black dot)
        drawCircle(
            color = Color(0xFF1E2F23),
            radius = w * 0.022f,
            center = Offset(cx + rx * 0.77f, cy - ry * 0.12f)
        )
    }
}

/**
 * Standard minimalist Black Goat silhouette used in the category & list cards
 */
@Composable
fun GoatSilhouette(
    modifier: Modifier = Modifier,
    tint: Color = Color.Black
) {
    val horn = remember { Path() }
    val ear = remember { Path() }
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val cx = w * 0.48f
        val cy = h * 0.52f
        val rx = w * 0.28f
        val ry = h * 0.21f

        // Legs
        val legW = w * 0.024f
        val legH = h * 0.15f
        // Back legs
        drawLine(tint, Offset(cx - rx * 0.5f, cy + ry * 0.7f), Offset(cx - rx * 0.5f, cy + ry * 0.7f + legH), legW)
        drawLine(tint, Offset(cx - rx * 0.1f, cy + ry * 0.8f), Offset(cx - rx * 0.1f, cy + ry * 0.8f + legH), legW)
        // Front legs
        drawLine(tint, Offset(cx + rx * 0.3f, cy + ry * 0.8f), Offset(cx + rx * 0.3f, cy + ry * 0.8f + legH), legW)
        drawLine(tint, Offset(cx + rx * 0.6f, cy + ry * 0.6f), Offset(cx + rx * 0.6f, cy + ry * 0.6f + legH), legW)

        // Body
        drawOval(tint, Offset(cx - rx, cy - ry), Size(rx * 2f, ry * 2f))

        // Curved Horns
        horn.reset()
        horn.moveTo(cx + rx * 0.45f, cy - ry * 0.6f)
        horn.quadraticTo(cx + rx * 0.55f, cy - ry * 1.15f, cx + rx * 0.52f, cy - ry * 1.25f)
        drawPath(horn, color = tint, style = Stroke(width = w * 0.024f))

        // Ear
        ear.reset()
        ear.moveTo(cx + rx * 0.3f, cy - ry * 0.3f)
        ear.quadraticTo(cx + rx * 0.1f, cy - ry * 0.55f, cx + rx * 0.17f, cy - ry * 0.6f)
        ear.close()
        drawPath(ear, color = tint)

        // Eye (light dot)
        drawCircle(
            color = Color.White.copy(alpha = 0.85f),
            radius = w * 0.025f,
            center = Offset(cx + rx * 0.7f, cy - ry * 0.1f)
        )
    }
}

/**
 * Draw custom Filter Icon (to match the right button on search)
 */
@Composable
fun CustomFilterIcon(
    modifier: Modifier = Modifier,
    tint: Color = Color.White
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val strokeWidth = w * 0.08f

        // Top line
        drawLine(
            color = tint,
            start = Offset(w * 0.15f, h * 0.28f),
            end = Offset(w * 0.85f, h * 0.28f),
            strokeWidth = strokeWidth
        )
        // Middle line
        drawLine(
            color = tint,
            start = Offset(w * 0.3f, h * 0.5f),
            end = Offset(w * 0.7f, h * 0.5f),
            strokeWidth = strokeWidth
        )
        // Bottom line
        drawLine(
            color = tint,
            start = Offset(w * 0.42f, h * 0.72f),
            end = Offset(w * 0.58f, h * 0.72f),
            strokeWidth = strokeWidth
        )
    }
}

/**
 * Custom modern Location / Map Pin Icon
 */
@Composable
fun CustomLocationPinIcon(
    modifier: Modifier = Modifier,
    tint: Color = Color.Black
) {
    val path = remember { Path() }
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val cx = w * 0.5f
        val cy = h * 0.4f
        val r = w * 0.28f

        // Path of drop shape
        path.reset()
        path.moveTo(cx, h * 0.95f)
        path.cubicTo(
            cx - r * 1.3f, cy + r * 0.5f,
            cx - r, cy - r,
            cx, cy - r
        )
        path.cubicTo(
            cx + r, cy - r,
            cx + r * 1.3f, cy + r * 0.5f,
            cx, h * 0.95f
        )
        path.close()

        drawPath(path, color = tint)

        // inner hole
        drawCircle(
            color = Color.White,
            radius = r * 0.42f,
            center = Offset(cx, cy)
        )
    }
}
