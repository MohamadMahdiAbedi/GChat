package ir.gchat

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.material3.MenuDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

@Composable
fun AnimatedMenu(
    modifier: Modifier = Modifier,
    width: Dp,
    height: Dp,
    chord: Dp,
    isExpanded: Boolean,
    close: () -> Unit,
    ratioX: Float = 0f,
    offsetX: Dp = 0.dp,
    ratioY: Float = 0f,
    offsetY: Dp = 0.dp,
    position: Alignment,
    shadow: Boolean = true,
    shadowShape: Shape = RoundedCornerShape(2.dp),
    hasBackgroundCover: Boolean = true,
    content: @Composable () -> Unit
) {
    val sizeBtn by animateDpAsState(
        targetValue = if (isExpanded) chord * 2 else 48.dp, animationSpec = tween(
            durationMillis = 200, easing = FastOutSlowInEasing
        ), label = "circle_size"
    )

    val surface = MenuDefaults.containerColor

    val expandProgress = remember(sizeBtn, chord) {
        if (chord == 24.dp) {
            1f
        } else {
            ((sizeBtn - 48.dp).value / (chord.value * 2 - 48f)).coerceIn(0f, 1f)
        }
    }

    val showContent = expandProgress > 0.05f

    val sharedBackgroundFilter =
        remember(expandProgress) { Color.Black.copy(alpha = expandProgress * 0.25f) }

    BackHandler(enabled = isExpanded) {
        close()
    }

    val interactionSource = remember {
        MutableInteractionSource()
    }

    Box(modifier = Modifier.fillMaxSize()) {

        if (showContent && hasBackgroundCover) {
            Spacer(
                modifier = Modifier
                    .fillMaxSize()
                    .background(sharedBackgroundFilter)
                    .clickable(
                        indication = null, interactionSource = interactionSource
                    ) {
                        close()
                    })
        }

        Box(
            modifier = modifier
                .padding(8.dp)
                .size(width, height)
                .shadow(
                    elevation = if (expandProgress > 0.99f && isExpanded && shadow) {
                        8.dp
                    } else {
                        0.dp
                    }, shape = shadowShape, clip = false
                )
                .clip(shadowShape)
                .align(position)
        ) {

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.Center)
                    .then(
                        if (sizeBtn != 48.dp) {
                        Modifier.clickable(
                            indication = null, interactionSource = interactionSource
                        ) {}
                    } else Modifier)) {

                val radius = sizeBtn.toPx() / 2

                val center = Offset(
                    x = size.width * ratioX - offsetX.toPx(),
                    y = size.height * ratioY - offsetY.toPx()
                )


                drawCircle(
                    color = if (sizeBtn != 48.dp) {
                        surface
                    } else {
                        Color.Transparent
                    }, radius = radius, center = center
                )
            }


            if (showContent) {
                val context = LocalContext.current
                val layoutDirection = if (context.isRtl()) {
                    LayoutDirection.Rtl
                } else {
                    LayoutDirection.Ltr
                }
                CompositionLocalProvider(
                    LocalLayoutDirection provides layoutDirection
                ) {
                    Box(
                        modifier = Modifier.alpha(expandProgress)
                    ) {
                        content()
                    }
                }
            }
        }
    }
}