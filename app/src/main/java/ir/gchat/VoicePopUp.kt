package ir.gchat

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun VoicePopUp(
    modifier: Modifier,
    width: Dp,
    height: Dp,
    chord: Dp,
    isExpanded: Boolean,
    close: () -> Unit,
    ratioX: Float,
    offsetX: Dp = 0.dp,
    ratioY: Float,
    offsetY: Dp = 0.dp,
    position: Alignment,
    shadow: Boolean = true,
    shadowShape: Shape = RoundedCornerShape(2.dp),
    hasBackgroundCover: Boolean = true,
    extruderContent: @Composable () -> Unit = {},
    content: @Composable () -> Unit
) {
    val sizeBtn by animateDpAsState(
        targetValue = if (isExpanded) chord * 2 else 48.dp, animationSpec = tween(
            durationMillis = 200, easing = FastOutSlowInEasing
        ), label = "circle_size"
    )

    val surface = MenuDefaults.containerColor

    val expandProgress = if (chord == 24.dp) {
        1f
    } else {
        ((sizeBtn - 48.dp).value / (chord.value * 2f - 48f)).coerceIn(0f, 1f)
    }

    val showContent = expandProgress > 0.05f

    BackHandler(enabled = isExpanded) {
        close()
    }

    val interactionSource = remember {
        MutableInteractionSource()
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        // ============================================================
        // کل محدوده‌ای که modifier اصلی روی آن اعمال می‌شود
        // ============================================================

        Box(
            modifier = Modifier.fillMaxSize()
        ) {

            // --------------------------------------------------------
            // BACKGROUND COVER
            // --------------------------------------------------------

            if (showContent && hasBackgroundCover) {
                Spacer(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Color.Black.copy(
                                alpha = expandProgress * 0.25f
                            )
                        )
                        .clickable(
                            indication = null, interactionSource = interactionSource
                        ) {
                            close()
                        })
            }

            // --------------------------------------------------------
            // MENU
            // --------------------------------------------------------

            Box(
                modifier = modifier
                    .padding(8.dp)
                    .size(width, height)
                    .align(position)
                    .shadow(
                        elevation = if (expandProgress > 0.99f && isExpanded && shadow) {
                            8.dp
                        } else {
                            0.dp
                        }, shape = shadowShape, clip = false
                    )
            ) {

                // ----------------------------------------------------
                // CIRCLE
                // ----------------------------------------------------

                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(
                            if (sizeBtn != 48.dp) {
                            Modifier.clickable(
                                indication = null, interactionSource = interactionSource
                            ) {}
                        } else {
                            Modifier
                        })) {
                    val radius = sizeBtn.toPx() / 2f

                    val center = Offset(
                        x = size.width * ratioX - offsetX.toPx(),

                        y = size.height * ratioY - offsetY.toPx()
                    )

                    if (sizeBtn != 48.dp) {
                        drawCircle(
                            color = surface, radius = radius, center = center
                        )
                    }
                }

                // ----------------------------------------------------
                // CONTENT
                // ----------------------------------------------------

                if (showContent) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .alpha(expandProgress)
                            .drawWithCache {

                                val radius = sizeBtn.toPx() / 2f

                                val center = Offset(
                                    x = size.width * ratioX - offsetX.toPx(),

                                    y = size.height * ratioY - offsetY.toPx()
                                )

                                val path = Path().apply {
                                    addOval(
                                        Rect(
                                            left = center.x - radius,
                                            top = center.y - radius,
                                            right = center.x + radius,
                                            bottom = center.y + radius
                                        )
                                    )
                                }

                                onDrawWithContent {
                                    clipPath(path) {
                                        this@onDrawWithContent.drawContent()
                                    }
                                }
                            }) {
                        content()
                    }
                }
            }
        }

        // ============================================================
        // EXTRUDER
        // ============================================================
        //
        // نکته:
        // این Box عمداً sibling منوی اصلی است.
        //
        // اما modifier اصلی روی آن اعمال نمی‌شود.
        //
        // اندازه‌اش کل صفحه است تا هیچ clipping ناشی از
        // 192dp منو ایجاد نشود.
        // ============================================================

        if (showContent) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(expandProgress)
            ) {
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .size(width, height)
                        .align(position)
                ) {
                    extruderContent()
                }
            }
        }
    }
}

class Quadrant2CircleShape(private val cornerRadius: Dp = 0.dp) : Shape {

    override fun createOutline(
        size: Size, layoutDirection: LayoutDirection, density: Density
    ): Outline {

        val radius = minOf(size.width, size.height)

        with(density) {
            val corner = cornerRadius.toPx().coerceIn(0f, radius / 2f)

            val center = Offset(radius, radius)

            val path = Path().apply {

                // ─────────────
                // مرکز
                // ─────────────
                moveTo(center.x - corner, center.y)

                // ضلع چپ تا گوشه
                lineTo(corner, center.y)

                // گوشهٔ پایین-چپ
                quadraticTo(
                    0f, center.y, 0f, center.y - corner
                )

                // ─────────────
                // ربع دایره
                // از کمی بعد از گوشهٔ چپ
                // تا کمی قبل از گوشهٔ بالا
                // ─────────────

                val angleOffset = Math.toDegrees(
                    asin(
                        corner / radius
                    ).toDouble()
                ).toFloat()

                arcTo(
                    rect = Rect(
                        left = 0f, top = 0f, right = radius * 2f, bottom = radius * 2f
                    ),
                    startAngleDegrees = 180f + angleOffset,
                    sweepAngleDegrees = 90f - 2f * angleOffset,
                    forceMoveTo = false
                )

                // گوشهٔ بالا
                quadraticTo(
                    radius, 0f, radius, corner
                )

                // ضلع راست تا مرکز
                lineTo(
                    center.x, center.y - corner
                )

                // گوشهٔ مرکز
                quadraticTo(
                    center.x, center.y, center.x - corner, center.y
                )

                close()
            }

            return Outline.Generic(path)
        }
    }
}

@Composable
fun WobblyRecordingCircle(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    scale: Float = 1f,
    wobbleAmount: Float = 0.01f,
    wobbleSpeed: Int = 1200
) {
    val infiniteTransition = rememberInfiniteTransition(
        label = "wobble"
    )

    val phase by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 2f * PI.toFloat(), animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = wobbleSpeed, easing = LinearEasing
            ), repeatMode = RepeatMode.Restart
        ), label = "phase"
    )

    Canvas(
        modifier = modifier
    ) {
        val center = this.center

        val radius = size.minDimension / 2f * scale

        val path = Path()
        val points = 64

        for (i in 0..points) {
            val angle = i.toFloat() / points * 2f * PI.toFloat()

            val wobble =
                1f + wobbleAmount * sin(angle * 3f + phase) + wobbleAmount * 0.6f * sin(angle * 5f - phase) + wobbleAmount * 0.3f * sin(
                    angle * 7f + phase
                )

            val r = radius * wobble

            val x = center.x + cos(angle) * r
            val y = center.y + sin(angle) * r

            if (i == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        path.close()

        drawPath(
            path = path, color = color
        )
    }
}