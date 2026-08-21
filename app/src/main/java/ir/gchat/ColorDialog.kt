package ir.gchat

import android.view.SoundEffectConstants
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun ColorPickerDialog(
    initialColor: Color, onDismissRequest: () -> Unit, onColorSelected: (Color) -> Unit
) {
    var color by remember(initialColor) {
        mutableStateOf(initialColor)
    }

    fun rgbToHsv(color: Color): FloatArray {
        val r = color.red
        val g = color.green
        val b = color.blue

        val max = maxOf(r, g, b)
        val min = minOf(r, g, b)
        val delta = max - min

        val h = when {
            delta == 0f -> 0f

            max == r -> {
                (60f * ((g - b) / delta)).let { if (it < 0f) it + 360f else it }
            }

            max == g -> 60f * ((b - r) / delta + 2f)

            else -> 60f * ((r - g) / delta + 4f)
        }.coerceIn(0f, 360f)

        val s = if (max == 0f) 0f
        else (delta / max).coerceIn(0f, 1f)

        return floatArrayOf(
            h, s, max.coerceIn(0f, 1f)
        )
    }

    val hsv = remember(initialColor) {
        rgbToHsv(initialColor)
    }

    var hue by remember(initialColor) {
        mutableFloatStateOf(hsv[0])
    }

    var saturation by remember(initialColor) {
        mutableFloatStateOf(hsv[1])
    }

    var value by remember(initialColor) {
        mutableFloatStateOf(hsv[2])
    }

    fun updateColor(
        h: Float = hue, s: Float = saturation, v: Float = value
    ) {
        hue = h.coerceIn(0f, 360f)
        saturation = s.coerceIn(0f, 1f)
        value = v.coerceIn(0f, 1f)

        color = Color.hsv(
            hue, saturation, value
        )
    }

    val hex = String.format(
        Locale.US,
        "#%02X%02X%02X",
        (color.red * 255f).roundToInt().coerceIn(0, 255),
        (color.green * 255f).roundToInt().coerceIn(0, 255),
        (color.blue * 255f).roundToInt().coerceIn(0, 255)
    )

    val hueBrush = remember {
        Brush.verticalGradient(
            colors = List(361) {
                Color.hsv(it.toFloat(), 1f, 1f)
            })
    }

    Dialog(
        onDismissRequest = onDismissRequest
    ) {
        BoxWithConstraints(
            modifier = Modifier
                //.fillMaxSize()
                .padding(16.dp)
        ) {

            val maxPickerWidth = maxHeight * 2/3f

            Surface(
                shape = RoundedCornerShape(2.dp),
                modifier = Modifier
                    .align(Alignment.Center)
                    .shadow(
                        elevation = 24.dp, clip = false
                    ),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                border = BorderStroke(
                    0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier
                        //.fillMaxSize()
                        .verticalScroll(
                            rememberScrollState()
                        )
                        .padding(16.dp)
                ) {

                    Text(text = "Select Color", style = MaterialTheme.typography.headlineSmall)

                    Spacer(modifier = Modifier.height(16.dp))

                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.CenterHorizontally)
                    ) {
                        val hueWidth = 32.dp
                        val spacing = 12.dp

                        val squareSize = minOf(
                            maxPickerWidth, maxWidth - hueWidth - spacing
                        ).coerceAtLeast(1.dp)

                        Row(
                            modifier = Modifier
                                .width(
                                    squareSize + spacing + hueWidth
                                )
                                .align(Alignment.Center),
                            horizontalArrangement = Arrangement.spacedBy(spacing),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            /*
                             * SATURATION / VALUE
                             */
                            Canvas(
                                modifier = Modifier
                                    .size(squareSize)
                                    .clip(
                                        RoundedCornerShape(2.dp)
                                    )
                                    .pointerInput(hue) {
                                        detectDragGestures(
                                            onDragStart = { position ->

                                                val x = position.x.coerceIn(
                                                    0f, size.width.toFloat()
                                                )

                                                val y = position.y.coerceIn(
                                                    0f, size.height.toFloat()
                                                )

                                                updateColor(
                                                    s = x / size.width, v = 1f - y / size.height
                                                )
                                            },

                                            onDrag = { change, _ ->

                                                change.consume()

                                                val x = change.position.x.coerceIn(
                                                    0f, size.width.toFloat()
                                                )

                                                val y = change.position.y.coerceIn(
                                                    0f, size.height.toFloat()
                                                )

                                                updateColor(
                                                    s = x / size.width, v = 1f - y / size.height
                                                )
                                            })
                                    }) {

                                val hueColor = Color.hsv(
                                    hue, 1f, 1f
                                )

                                // White -> Hue
                                drawRect(
                                    brush = Brush.horizontalGradient(
                                        listOf(
                                            Color.White, hueColor
                                        )
                                    )
                                )

                                // Transparent -> Black
                                drawRect(
                                    brush = Brush.verticalGradient(
                                        listOf(
                                            Color.Transparent, Color.Black
                                        )
                                    )
                                )

                                /*
                                 * Selection indicator
                                 */
                                val x = saturation * size.width

                                val y = (1f - value) * size.height

                                drawCircle(
                                    color = Color.Black.copy(
                                        alpha = 0.55f
                                    ), radius = 14.dp.toPx(), center = Offset(x, y), style = Stroke(
                                        width = 2.dp.toPx()
                                    )
                                )

                                drawCircle(
                                    color = Color.White,
                                    radius = 11.dp.toPx(),
                                    center = Offset(x, y),
                                    style = Stroke(
                                        width = 2.5.dp.toPx()
                                    )
                                )
                            }

                            /*
                             * HUE
                             */
                            Canvas(
                                modifier = Modifier
                                    .width(hueWidth)
                                    .height(squareSize)
                                    .clip(
                                        RoundedCornerShape(2.dp)
                                    )
                                    .pointerInput(Unit) {
                                        detectDragGestures(
                                            onDragStart = { position ->

                                                val y = position.y.coerceIn(
                                                    0f, size.height.toFloat()
                                                )

                                                updateColor(
                                                    h = y / size.height * 360f
                                                )
                                            },

                                            onDrag = { change, _ ->

                                                change.consume()

                                                val y = change.position.y.coerceIn(
                                                    0f, size.height.toFloat()
                                                )

                                                updateColor(
                                                    h = y / size.height * 360f
                                                )
                                            })
                                    }
                            ) {
                                drawRect(brush = hueBrush)

                                /*
                                 * Hue indicator
                                 */
                                val indicatorY = hue.coerceIn(
                                    0f, 360f
                                ) / 360f * size.height

                                //drawLine(
                                //    color = Color.White, start = Offset(
                                //        2.dp.toPx(), indicatorY
                                //    ), end = Offset(
                                //        size.width - 2.dp.toPx(), indicatorY
                                //    ), strokeWidth = 4.dp.toPx(), cap = StrokeCap.Round
                                //)

                                drawCircle(
                                    color = Color.Black.copy(
                                        alpha = 0.55f
                                    ), radius = 14.dp.toPx(), center = Offset(size.width/2f, indicatorY), style = Stroke(
                                        width = 2.dp.toPx()
                                    )
                                )

                                drawCircle(
                                    color = Color.White,
                                    radius = 11.dp.toPx(),
                                    center = Offset(size.width/2f, indicatorY),
                                    style = Stroke(
                                        width = 2.5.dp.toPx()
                                    )
                                )
                            }
                        }
                    }

                    Spacer(
                        modifier = Modifier.height(16.dp)
                    )

                    HorizontalDivider()

                    Spacer(
                        modifier = Modifier.height(16.dp)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    1.5.dp, MaterialTheme.colorScheme.outline, CircleShape
                                )
                        )

                        Text(
                            text = hex,
                            style = MaterialTheme.typography.titleMedium,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(
                        modifier = Modifier.height(24.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End
                    ) {
                        val view = LocalView.current
                        TextButton(
                            shape = RoundedCornerShape(2.dp), onClick = {
                                view.playSoundEffect(SoundEffectConstants.CLICK)
                                onDismissRequest()
                            }) {
                            Text("Cancel")
                        }
                        Spacer(
                            modifier = Modifier.width(8.dp)
                        )

                        Button(
                            onClick = {
                                onColorSelected(color)
                                onDismissRequest()
                            },
                            shape = RoundedCornerShape(2.dp),
                        ) {
                            Text("OK")
                        }
                    }
                }
            }
        }
    }
}