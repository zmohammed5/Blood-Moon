package com.bloodmoon.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.bloodmoon.ui.theme.*
import kotlin.math.sin

enum class MoonyVariant {
    DEFAULT,
    HORNS,
    CORPSE_PAINT
}

enum class MoonyAnimation {
    FLOAT,
    HEAD_BOB,
    HORNS_UP,
    ANGRY_SHAKE
}

@Composable
fun Moony(
    variant: MoonyVariant = MoonyVariant.DEFAULT,
    animation: MoonyAnimation = MoonyAnimation.FLOAT,
    isPlaying: Boolean = false,
    onTap: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "moony")

    // Floating animation
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )

    // Head bob animation (for music)
    val bobOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (isPlaying) 10f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(300, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bob"
    )

    // Horns up animation
    val hornsScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "horns"
    )

    val offsetY = when (animation) {
        MoonyAnimation.FLOAT -> floatOffset
        MoonyAnimation.HEAD_BOB -> bobOffset
        else -> 0f
    }

    Canvas(
        modifier = modifier
            .size(64.dp)
            .offset(y = offsetY.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onTap() }
    ) {
        when (variant) {
            MoonyVariant.DEFAULT -> drawMoonyDefault()
            MoonyVariant.HORNS -> drawMoonyHorns()
            MoonyVariant.CORPSE_PAINT -> drawMoonyCorpsePaint()
        }
    }
}

/**
 * Draw default Moony - cute pixel moon ghost
 */
private fun DrawScope.drawMoonyDefault() {
    val centerX = size.width / 2
    val centerY = size.height / 2
    val radius = size.width / 3

    // Main body (circular moon shape)
    drawCircle(
        color = PaleLilac,
        radius = radius,
        center = Offset(centerX, centerY)
    )

    // Subtle inner shadow for depth
    drawCircle(
        color = VampyPurple.copy(alpha = 0.1f),
        radius = radius * 0.9f,
        center = Offset(centerX + 2, centerY + 2)
    )

    // Glow outline
    drawCircle(
        color = ElectricPink,
        radius = radius,
        center = Offset(centerX, centerY),
        style = Stroke(width = 2f)
    )

    // Outer glow
    drawCircle(
        color = ElectricPink.copy(alpha = 0.3f),
        radius = radius + 2,
        center = Offset(centerX, centerY),
        style = Stroke(width = 4f)
    )

    // Rosy cheeks
    val cheekRadius = 5f
    val cheekOffsetX = radius / 2
    val cheekOffsetY = radius / 6

    // Left cheek
    drawCircle(
        color = ElectricPink.copy(alpha = 0.4f),
        radius = cheekRadius,
        center = Offset(centerX - cheekOffsetX, centerY + cheekOffsetY)
    )

    // Right cheek
    drawCircle(
        color = ElectricPink.copy(alpha = 0.4f),
        radius = cheekRadius,
        center = Offset(centerX + cheekOffsetX, centerY + cheekOffsetY)
    )

    // Eyes (cute dots with shine)
    val eyeRadius = 4f
    val eyeOffsetX = radius / 3
    val eyeOffsetY = radius / 4

    // Left eye
    drawCircle(
        color = DeepBlack,
        radius = eyeRadius,
        center = Offset(centerX - eyeOffsetX, centerY - eyeOffsetY)
    )
    // Left eye shine
    drawCircle(
        color = PaleLilac,
        radius = 1.5f,
        center = Offset(centerX - eyeOffsetX - 1, centerY - eyeOffsetY - 1)
    )

    // Right eye
    drawCircle(
        color = DeepBlack,
        radius = eyeRadius,
        center = Offset(centerX + eyeOffsetX, centerY - eyeOffsetY)
    )
    // Right eye shine
    drawCircle(
        color = PaleLilac,
        radius = 1.5f,
        center = Offset(centerX + eyeOffsetX - 1, centerY - eyeOffsetY - 1)
    )

    // Smile (small arc)
    val smilePath = Path().apply {
        moveTo(centerX - radius / 3, centerY + radius / 4)
        quadraticBezierTo(
            centerX, centerY + radius / 2,
            centerX + radius / 3, centerY + radius / 4
        )
    }
    drawPath(
        path = smilePath,
        color = DeepBlack,
        style = Stroke(width = 2.5f)
    )

    // Top highlight for 3D effect
    drawCircle(
        color = Color.White.copy(alpha = 0.3f),
        radius = radius / 4,
        center = Offset(centerX - radius / 4, centerY - radius / 3)
    )
}

/**
 * Draw Moony with devil horns
 */
private fun DrawScope.drawMoonyHorns() {
    val centerX = size.width / 2
    val centerY = size.height / 2
    val radius = size.width / 3

    // Draw devil horns
    val hornHeight = radius / 2
    val hornWidth = radius / 4

    // Left horn
    val leftHornPath = Path().apply {
        moveTo(centerX - radius / 2, centerY - radius)
        lineTo(centerX - radius / 2 - hornWidth, centerY - radius - hornHeight)
        lineTo(centerX - radius / 2 + hornWidth / 2, centerY - radius)
    }
    drawPath(leftHornPath, color = MetalCrimson, style = Fill)
    drawPath(leftHornPath, color = ElectricPink, style = Stroke(width = 2f))

    // Right horn
    val rightHornPath = Path().apply {
        moveTo(centerX + radius / 2, centerY - radius)
        lineTo(centerX + radius / 2 + hornWidth, centerY - radius - hornHeight)
        lineTo(centerX + radius / 2 - hornWidth / 2, centerY - radius)
    }
    drawPath(rightHornPath, color = MetalCrimson, style = Fill)
    drawPath(rightHornPath, color = ElectricPink, style = Stroke(width = 2f))

    // Main body
    drawCircle(
        color = PaleLilac,
        radius = radius,
        center = Offset(centerX, centerY)
    )

    // Glow outline (more intense for metal mode)
    drawCircle(
        color = MetalHellPink,
        radius = radius,
        center = Offset(centerX, centerY),
        style = Stroke(width = 3f)
    )

    // Eyes (slightly more intense)
    val eyeRadius = 4f
    val eyeOffsetX = radius / 3
    val eyeOffsetY = radius / 4

    drawCircle(
        color = MetalCrimson,
        radius = eyeRadius,
        center = Offset(centerX - eyeOffsetX, centerY - eyeOffsetY)
    )

    drawCircle(
        color = MetalCrimson,
        radius = eyeRadius,
        center = Offset(centerX + eyeOffsetX, centerY - eyeOffsetY)
    )

    // Mischievous smile
    val smilePath = Path().apply {
        moveTo(centerX - radius / 3, centerY + radius / 4)
        quadraticBezierTo(
            centerX, centerY + radius / 2 + 5,
            centerX + radius / 3, centerY + radius / 4
        )
    }
    drawPath(
        path = smilePath,
        color = MetalCrimson,
        style = Stroke(width = 3f)
    )
}

/**
 * Draw Moony with corpse paint (black metal style face paint)
 */
private fun DrawScope.drawMoonyCorpsePaint() {
    val centerX = size.width / 2
    val centerY = size.height / 2
    val radius = size.width / 3

    // Main body
    drawCircle(
        color = PaleLilac,
        radius = radius,
        center = Offset(centerX, centerY)
    )

    // Glow outline
    drawCircle(
        color = ElectricPink,
        radius = radius,
        center = Offset(centerX, centerY),
        style = Stroke(width = 2f)
    )

    // Corpse paint - black streaks around eyes
    val eyeOffsetX = radius / 3
    val eyeOffsetY = radius / 4

    // Left eye black paint
    drawCircle(
        color = DeepBlack,
        radius = 8f,
        center = Offset(centerX - eyeOffsetX, centerY - eyeOffsetY)
    )
    // Left eye white dot
    drawCircle(
        color = PaleLilac,
        radius = 3f,
        center = Offset(centerX - eyeOffsetX, centerY - eyeOffsetY)
    )

    // Right eye black paint
    drawCircle(
        color = DeepBlack,
        radius = 8f,
        center = Offset(centerX + eyeOffsetX, centerY - eyeOffsetY)
    )
    // Right eye white dot
    drawCircle(
        color = PaleLilac,
        radius = 3f,
        center = Offset(centerX + eyeOffsetX, centerY - eyeOffsetY)
    )

    // Vertical black streaks (corpse paint style)
    drawLine(
        color = DeepBlack,
        start = Offset(centerX - eyeOffsetX, centerY - eyeOffsetY + 8),
        end = Offset(centerX - eyeOffsetX, centerY + radius / 2),
        strokeWidth = 2f
    )
    drawLine(
        color = DeepBlack,
        start = Offset(centerX + eyeOffsetX, centerY - eyeOffsetY + 8),
        end = Offset(centerX + eyeOffsetX, centerY + radius / 2),
        strokeWidth = 2f
    )

    // Black lipstick smile
    val smilePath = Path().apply {
        moveTo(centerX - radius / 3, centerY + radius / 4)
        quadraticBezierTo(
            centerX, centerY + radius / 2,
            centerX + radius / 3, centerY + radius / 4
        )
    }
    drawPath(
        path = smilePath,
        color = DeepBlack,
        style = Stroke(width = 4f)
    )
}

/**
 * Horns up gesture 🤘
 */
@Composable
fun HornsUpGesture(
    modifier: Modifier = Modifier
) {
    val scale by rememberInfiniteTransition(label = "horns").animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Canvas(modifier = modifier.size(32.dp)) {
        val centerX = size.width / 2
        val centerY = size.height / 2

        // Draw 🤘 symbol
        drawLine(
            color = MetalHellPink,
            start = Offset(centerX - 10, centerY - 15 * scale),
            end = Offset(centerX - 10, centerY + 5 * scale),
            strokeWidth = 4f
        )
        drawLine(
            color = MetalHellPink,
            start = Offset(centerX + 10, centerY - 15 * scale),
            end = Offset(centerX + 10, centerY + 5 * scale),
            strokeWidth = 4f
        )
    }
}
