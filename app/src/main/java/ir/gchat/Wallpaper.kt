package ir.gchat

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.time.Duration.Companion.milliseconds

data class Position(val x: Float, val y: Float)

enum class WallpaperMixBlendMode {
    Normal, Overlay, SoftLight, HardLight
}

private val POSITIONS = listOf(
    Position(0.80f, 0.10f),
    Position(0.60f, 0.20f),
    Position(0.35f, 0.25f),
    Position(0.25f, 0.60f),
    Position(0.20f, 0.90f),
    Position(0.40f, 0.80f),
    Position(0.65f, 0.75f),
    Position(0.75f, 0.40f)
)
private const val PHASES = 8
private const val TWO_PI = 2f * Math.PI.toFloat()

fun getPositions(
    shift: Int
): List<Position> {
    val list = POSITIONS.toMutableList()

    repeat(
        shift.mod(PHASES)
    ) {
        list.add(
            list.removeAt(0)
        )
    }

    return list.filterIndexed { index, _ ->
        index % 2 == 0
    }
}

fun currentPositions(
    angle: Float
): List<Position> {
    val normalizedAngle = angle.mod(TWO_PI)
    val phaseFloat = normalizedAngle / TWO_PI * PHASES
    val phase = phaseFloat.toInt().mod(PHASES)
    val fraction = phaseFloat - phase
    val current = getPositions(phase)
    val next = getPositions((phase + 1).mod(PHASES))

    return List(4) { i ->
        Position(
            x = current[i].x + (next[i].x - current[i].x) * fraction,
            y = current[i].y + (next[i].y - current[i].y) * fraction
        )
    }
}

// =========================================================
// Gradient Bitmap
// =========================================================
fun generateGradientBitmap(
    width: Int, height: Int, positions: List<Position>, colors: List<Color>
): ImageBitmap {
    val pixels = IntArray(width * height)

    for (y in 0 until height) {
        val directY = y / height.toFloat()
        val centerY = directY - 0.5f
        val centerY2 = centerY * centerY

        for (x in 0 until width) {
            val directX = x / width.toFloat()
            val centerX = directX - 0.5f
            val centerDist = sqrt(
                centerX * centerX + centerY2
            )
            val swirl = 0.35f * centerDist
            val theta = swirl * swirl * 0.8f * 8f
            val sinT = sin(theta)
            val cosT = cos(theta)
            val px = (0.5f + centerX * cosT - centerY * sinT).coerceIn(0f, 1f)
            val py = (0.5f + centerX * sinT + centerY * cosT).coerceIn(0f, 1f)
            var r = 0f
            var g = 0f
            var b = 0f
            var distSum = 0f

            for (i in colors.indices) {
                val dx = px - positions[i].x
                val dy = py - positions[i].y
                var dist = max(
                    0f, 0.9f - sqrt(
                        dx * dx + dy * dy
                    )
                )

                dist *= dist * dist * dist

                distSum += dist

                r += dist * colors[i].red
                g += dist * colors[i].green
                b += dist * colors[i].blue
            }
            val inv = if (distSum > 0f) {
                1f / distSum
            } else {
                0f
            }
            val rr = (r * inv * 255f).toInt().coerceIn(0, 255)
            val gg = (g * inv * 255f).toInt().coerceIn(0, 255)
            val bb = (b * inv * 255f).toInt().coerceIn(0, 255)

            pixels[y * width + x] = (0xFF shl 24) or (rr shl 16) or (gg shl 8) or bb
        }
    }

    return Bitmap.createBitmap(
        pixels, width, height, Bitmap.Config.ARGB_8888
    ).asImageBitmap()
}

// =========================================================
// BlendMode
// =========================================================
private fun WallpaperMixBlendMode.toComposeBlendMode(): BlendMode {
    return when (this) {
        WallpaperMixBlendMode.Normal -> BlendMode.SrcOver
        WallpaperMixBlendMode.Overlay -> BlendMode.Overlay
        WallpaperMixBlendMode.SoftLight -> BlendMode.Softlight
        WallpaperMixBlendMode.HardLight -> BlendMode.Hardlight
    }
}

// =========================================================
// TWallpaper
// =========================================================
@Composable
fun TWallpaper(
    modifier: Modifier = Modifier,
    colors: List<Color> = listOf(
        Color(0xFFDBDDBB), Color(0xFF6BA587), Color(0xFFD5D88D), Color(0xFF88B884)
    ),
    updateFps: Int = 60,
    angularSpeed: Float = Math.PI.toFloat() / 4f,
    animate: Boolean = true,
    patternAlpha: Float = 0.5f,
    pattern: Painter = painterResource(R.drawable.pattern),
    mixBlendMode: WallpaperMixBlendMode = WallpaperMixBlendMode.Overlay,
    patternTint: Color = Color.Black
) {
    var angle by remember { mutableFloatStateOf(0f) }
    // =====================================================
    // Animation Clock
    // =====================================================
    LaunchedEffect(
        animate, updateFps, angularSpeed
    ) {
        if (!animate) {
            return@LaunchedEffect
        }
        val frameTime = 1000L / updateFps.coerceAtLeast(1)
        var lastTime = System.nanoTime()

        while (true) {
            delay(frameTime.milliseconds)
            val now = System.nanoTime()
            val deltaSeconds = (now - lastTime) / 1_000_000_000f

            lastTime = now

            angle = (angle + angularSpeed * deltaSeconds).mod(TWO_PI)
        }
    }
    // =====================================================
    // Positions
    // =====================================================
    val positions = remember(angle) {
        currentPositions(
            angle = angle
        )
    }
    // =====================================================
    // Gradient
    // =====================================================
    val gradient = remember(positions, colors) {
        generateGradientBitmap(
            width = 50, height = 50, positions = positions, colors = colors
        )
    }
    // =====================================================
    // Draw
    // =====================================================
    Canvas(
        modifier = modifier.fillMaxSize()
    ) {
        // -------------------------------------------------
        // Gradient
        // -------------------------------------------------
        drawImage(
            image = gradient, dstSize = androidx.compose.ui.unit.IntSize(
                width = size.width.toInt(), height = size.height.toInt()
            ), filterQuality = FilterQuality.Low
        )
        // -------------------------------------------------
        // Pattern
        // -------------------------------------------------
        val intrinsic = pattern.intrinsicSize
        val intrinsicWidth = intrinsic.width
        val intrinsicHeight = intrinsic.height

        if (intrinsicWidth > 0f && intrinsicHeight > 0f) {
            val aspectRatio = intrinsicWidth / intrinsicHeight
            val tileHeight = size.height
            val tileWidth = tileHeight * aspectRatio

            if (tileWidth > 0f) {
                val blendMode = mixBlendMode.toComposeBlendMode()
                val alpha = patternAlpha.coerceIn(0f, 1f)
                val layerPaint = Paint().apply {
                    this.blendMode = blendMode
                }

                drawContext.canvas.saveLayer(
                    Rect(
                        left = 0f, top = 0f, right = size.width, bottom = size.height
                    ), layerPaint
                )
                // -------------------------------------------------
                // Pattern Tiles
                // -------------------------------------------------
                var x = 0f

                while (x < size.width) {
                    withTransform({
                        translate(
                            left = x, top = 0f
                        )
                    }) {
                        with(pattern) {
                            draw(
                                size = Size(
                                    width = tileWidth, height = tileHeight
                                ), alpha = alpha, colorFilter = ColorFilter.tint(
                                    patternTint
                                )
                            )
                        }
                    }

                    x += tileWidth
                }
                // -------------------------------------------------
                // Composite
                // -------------------------------------------------
                drawContext.canvas.restore()
            }
        }
    }
}