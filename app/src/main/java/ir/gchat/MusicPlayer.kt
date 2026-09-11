package ir.gchat

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import kotlin.math.*
import androidx.core.graphics.scale
import androidx.core.graphics.get

fun extractFourColors(bitmap: Bitmap): List<Color> {
    val size = 64
    val small = bitmap.scale(size, size)

    data class Pixel(
        val r: Float, val g: Float, val b: Float, val h: Float, val s: Float, val l: Float
    )

    data class Cluster(
        var r: Float,
        var g: Float,
        var b: Float,
        var h: Float,
        var s: Float,
        var l: Float,
        var count: Int = 0
    )

    fun rgbToHsl(r: Float, g: Float, b: Float): FloatArray {
        val max = maxOf(r, g, b)
        val min = minOf(r, g, b)

        val l = (max + min) / 2f

        if (max == min) {
            return floatArrayOf(0f, 0f, l)
        }

        val d = max - min
        val s = if (l > 0.5f) {
            d / (2f - max - min)
        } else {
            d / (max + min)
        }

        val h = when (max) {
            r -> (g - b) / d + if (g < b) 6f else 0f
            g -> (b - r) / d + 2f
            else -> (r - g) / d + 4f
        } / 6f

        return floatArrayOf(h, s, l)
    }

    val pixels = mutableListOf<Pixel>()

    for (y in 0 until size) {
        for (x in 0 until size) {

            val color = small[x, y]

            val r = ((color shr 16) and 0xff) / 255f
            val g = ((color shr 8) and 0xff) / 255f
            val b = (color and 0xff) / 255f

            val hsl = rgbToHsl(r, g, b)

            // رنگ‌های کاملاً سیاه/سفید را کمتر در نظر می‌گیریم
            if (hsl[2] !in 0.04f..0.96f) {
                continue
            }

            pixels += Pixel(
                r = r, g = g, b = b, h = hsl[0], s = hsl[1], l = hsl[2]
            )
        }
    }

    if (pixels.isEmpty()) {
        return listOf(
            Color.Black, Color.DarkGray, Color.Gray, Color.LightGray
        )
    }

    /*
     * ابتدا رنگ‌ها را بر اساس HSL به چند گروه تقسیم می‌کنیم.
     * 12 کاندید می‌سازیم تا بعداً بهترین 4 تا را انتخاب کنیم.
     */

    val clusters = mutableListOf<Cluster>()

    val initialCount = minOf(12, pixels.size)

    // انتخاب اولیه‌ی نسبتاً پخش‌شده
    for (i in 0 until initialCount) {
        val p = pixels[(i * pixels.size) / initialCount]

        clusters += Cluster(
            r = p.r, g = p.g, b = p.b, h = p.h, s = p.s, l = p.l
        )
    }

    // K-Means
    repeat(8) {

        clusters.forEach {
            it.count = 0
            it.r = 0f
            it.g = 0f
            it.b = 0f
            it.h = 0f
            it.s = 0f
            it.l = 0f
        }

        val sums = Array(clusters.size) {
            FloatArray(6)
        }

        for (p in pixels) {

            var best = 0
            var bestDistance = Float.MAX_VALUE

            for (i in clusters.indices) {
                val c = clusters[i]

                var hueDiff = abs(p.h - c.h)
                if (hueDiff > 0.5f) {
                    hueDiff = 1f - hueDiff
                }

                val distance =
                    hueDiff * hueDiff * 2.5f + (p.s - c.s).pow(2) * 1.5f + (p.l - c.l).pow(2) + (p.r - c.r).pow(
                        2
                    ) * 0.5f + (p.g - c.g).pow(2) * 0.5f + (p.b - c.b).pow(2) * 0.5f

                if (distance < bestDistance) {
                    bestDistance = distance
                    best = i
                }
            }

            val s = sums[best]

            s[0] += p.r
            s[1] += p.g
            s[2] += p.b
            s[3] += p.h
            s[4] += p.s
            s[5] += p.l

            clusters[best].count++
        }

        for (i in clusters.indices) {
            val count = clusters[i].count

            if (count > 0) {
                val s = sums[i]

                clusters[i].r = s[0] / count
                clusters[i].g = s[1] / count
                clusters[i].b = s[2] / count
                clusters[i].h = s[3] / count
                clusters[i].s = s[4] / count
                clusters[i].l = s[5] / count
            }
        }
    }

    val candidates = clusters.filter { it.count > 0 }.map { cluster ->

        val population = cluster.count.toFloat() / pixels.size

        val saturationScore = minOf(cluster.s / 0.65f, 1f)

        val luminanceScore = 1f - abs(cluster.l - 0.5f) * 0.7f

        val score = population * 0.55f + saturationScore * 0.25f + luminanceScore * 0.20f

        cluster to score
    }.sortedByDescending { it.second }

    val selected = mutableListOf<Cluster>()

    for ((candidate, _) in candidates) {

        if (selected.isEmpty()) {
            selected += candidate
            continue
        }

        var tooSimilar = false
        var minimumDistance = Float.MAX_VALUE

        for (other in selected) {

            var hueDiff = abs(candidate.h - other.h)

            if (hueDiff > 0.5f) {
                hueDiff = 1f - hueDiff
            }

            val distance =
                hueDiff * 2.5f + abs(candidate.s - other.s) * 1.2f + abs(candidate.l - other.l)

            minimumDistance = minOf(minimumDistance, distance)

            if (distance < 0.28f) {
                tooSimilar = true
                break
            }
        }

        if (!tooSimilar) {
            selected += candidate
        }

        if (selected.size == 4) {
            break
        }
    }

    /*
     * اگر به دلیل شباهت رنگ‌ها کمتر از 4 رنگ داشتیم،
     * باقی را از بهترین کاندیدها پر می‌کنیم.
     */

    if (selected.size < 4) {
        for ((candidate, _) in candidates) {
            if (candidate !in selected) {
                selected += candidate
            }

            if (selected.size == 4) {
                break
            }
        }
    }

    /*
     * اگر تعداد رنگ‌های واقعی کمتر از 4 بود،
     * با تکرار آخرین رنگ لیست را کامل می‌کنیم.
     */

    while (selected.size < 4) {
        selected += selected.lastOrNull() ?: clusters.first()
    }

    small.recycle()

    return selected.take(4).map {
        Color(
            red = it.r.coerceIn(0f, 1f), green = it.g.coerceIn(0f, 1f), blue = it.b.coerceIn(0f, 1f)
        )
    }
}