package ir.gchat

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.draw
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.time.Duration.Companion.milliseconds

data class Position(val x: Float, val y: Float)
data class Rgb(val r: Float, val g: Float, val b: Float)

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

private val PHASES = POSITIONS.size // 8

fun hexToRgb(hex: String): Rgb {
    val clean = hex.removePrefix("#")
    val full = if (clean.length == 3) {
        clean.map { "$it$it" }.joinToString("")
    } else clean
    return Rgb(
        r = full.substring(0, 2).toInt(16) / 255f,
        g = full.substring(2, 4).toInt(16) / 255f,
        b = full.substring(4, 6).toInt(16) / 255f
    )
}

fun getPositions(shift: Int): List<Position> {
    val list = POSITIONS.toMutableList()
    repeat(shift % PHASES) {
        list.add(list.removeAt(0))
    }
    return list.filterIndexed { i, _ -> i % 2 == 0 }
}

fun currentPositions(phase: Int, tail: Float, tails: Int): List<Position> {
    val pos = getPositions(phase % PHASES)
    if (tail == 0f) return pos

    val next = getPositions((phase + 1) % PHASES)
    val t = tail / tails

    return List(4) { i ->
        Position(
            x = pos[i].x + (next[i].x - pos[i].x) * t,
            y = pos[i].y + (next[i].y - pos[i].y) * t
        )
    }
}

fun generateGradientBitmap(
    width: Int = 50,
    height: Int = 50,
    positions: List<Position>,
    colors: List<Rgb>
): ImageBitmap {
    val pixels = IntArray(width * height)

    for (y in 0 until height) {
        val directY = y / height.toFloat()
        val centerY = directY - 0.5f
        val centerY2 = centerY * centerY

        for (x in 0 until width) {
            val directX = x / width.toFloat()
            val centerX = directX - 0.5f
            val centerDist = sqrt(centerX * centerX + centerY2)

            // swirl (چرخش مرکزی)
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
                var dist = max(0f, 0.9f - sqrt(dx * dx + dy * dy))
                dist *= dist * dist * dist // ^4
                distSum += dist

                r += dist * colors[i].r
                g += dist * colors[i].g
                b += dist * colors[i].b
            }

            val inv = if (distSum > 0f) 1f / distSum else 0f
            val rr = (r * inv * 255).toInt().coerceIn(0, 255)
            val gg = (g * inv * 255).toInt().coerceIn(0, 255)
            val bb = (b * inv * 255).toInt().coerceIn(0, 255)

            pixels[y * width + x] = (0xFF shl 24) or (rr shl 16) or (gg shl 8) or bb
        }
    }

    val androidBitmap = Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)
    return androidBitmap.asImageBitmap()
}

@Composable
fun TWallpaper(
    modifier: Modifier = Modifier,
    colors: List<String> = listOf("#dbddbb", "#6ba587", "#d5d88d", "#88b884"),
    fps: Int = 24,
    tails: Int = 6,
    animate: Boolean = true,
    patternAlpha: Float = 0.3f,
    pattern: Painter = painterResource(R.drawable.pattern)
) {
    val rgbColors = remember(colors) { colors.take(4).map { hexToRgb(it) } }

    var phase by remember { mutableIntStateOf(0) }
    var tail by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(animate, fps, tails) {
        if (!animate) return@LaunchedEffect
        val frameTime = 1000L / fps
        while (true) {
            delay(frameTime.milliseconds)
            tail += 1f
            if (tail >= tails) {
                tail = 0f
                phase = (phase + 1) % PHASES
            }
        }
    }

    val positions = remember(phase, tail) {
        currentPositions(phase, tail, tails)
    }

    val bitmap = remember(positions, rgbColors) {
        generateGradientBitmap(50, 50, positions, rgbColors)
    }

    Image(
        bitmap = bitmap,
        contentDescription = null,
        modifier = modifier.fillMaxSize(),
        contentScale = ContentScale.Crop,
        filterQuality = FilterQuality.Low
    )
    
    TelegramPatternOverlay(
        pattern = pattern,
        alpha = patternAlpha,
        modifier = modifier.fillMaxSize()
    )
}

@Composable
private fun TelegramPatternOverlay(
    modifier: Modifier = Modifier,
    alpha: Float = 0.15f,
    pattern: Painter
) {
    Canvas(
        modifier = modifier
            .clipToBounds()
            .alpha(alpha)
    ) {
        val intrinsic = pattern.intrinsicSize

        if (intrinsic.width <= 0f || intrinsic.height <= 0f) {
            return@Canvas
        }

        // نسبت واقعی VectorDrawable
        val aspectRatio = intrinsic.width / intrinsic.height

        // ارتفاع Pattern دقیقاً برابر ارتفاع Canvas
        val tileHeight = size.height

        // عرض متناسب با نسبت Pattern
        val tileWidth = tileHeight * aspectRatio

        if (tileWidth <= 0f) {
            return@Canvas
        }

        var x = 0f

        while (x < size.width) {
            withTransform({
                translate(left = x, top = 0f)
            }) {
                with(pattern) {
                    draw(
                        size = Size(
                            width = tileWidth,
                            height = tileHeight
                        )
                    )
                }
            }

            x += tileWidth
        }
    }
}