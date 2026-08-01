package ir.gchat

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.Typography

val materialColors = listOf(
    Color(0xFFF44336),
    Color(0xFFE91E63),
    Color(0xFF9C27B0),
    Color(0xFF673AB7),
    Color(0xFF3F51B5),
    Color(0xFF2196F3),
    Color(0xFF03A9F4),
    Color(0xFF00BCD4),
    Color(0xFF009688),
    Color(0xFF4CAF50),
    Color(0xFF8BC34A),
    Color(0xFFCDDC39),
    Color(0xFFFFEB3B),
    Color(0xFFFFC107),
    Color(0xFFFF9800),
    Color(0xFFFF5722),
    Color(0xFF795548),
    Color(0xFF9E9E9E),
    Color(0xFF607D8B)
)

val materialPalette = listOf(
    Palette(
        primary = Color(0xFFF44336),
        onPrimary = Color(0xFFFFFFFF)
    ), Palette(
        primary = Color(0xFFE91E63),
        onPrimary = Color(0xFFFFFFFF)
    ), Palette(
        primary = Color(0xFF9C27B0),
        onPrimary = Color(0xFFFFFFFF)
    ), Palette(
        primary = Color(0xFF673AB7),
        onPrimary = Color(0xFFFFFFFF)
    ), Palette(
        primary = Color(0xFF3F51B5),
        onPrimary = Color(0xFFFFFFFF)
    ), Palette(
        primary = Color(0xFF2196F3),
        onPrimary = Color(0xFF000000)
    ), Palette(
        primary = Color(0xFF03A9F4),
        onPrimary = Color(0xFF000000)
    ), Palette(
        primary = Color(0xFF00BCD4),
        onPrimary = Color(0xFF000000)
    ), Palette(
        primary = Color(0xFF009688),
        onPrimary = Color(0xFFFFFFFF)
    ), Palette(
        primary = Color(0xFF4CAF50),
        onPrimary = Color(0xFF000000)
    ), Palette(
        primary = Color(0xFF8BC34A),
        onPrimary = Color(0xFF000000)
    ), Palette(
        primary = Color(0xFFCDDC39),
        onPrimary = Color(0xFF000000)
    ), Palette(
        primary = Color(0xFFFFEB3B),
        onPrimary = Color(0xFF000000)
    ), Palette(
        primary = Color(0xFFFFC107),
        onPrimary = Color(0xFF000000)
    ), Palette(
        primary = Color(0xFFFF9800),
        onPrimary = Color(0xFF000000)
    ), Palette(
        primary = Color(0xFFFF5722),
        onPrimary = Color(0xFF000000)
    ), Palette(
        primary = Color(0xFF795548),
        onPrimary = Color(0xFFFFFFFF)
    ), Palette(
        primary = Color(0xFF9E9E9E),
        onPrimary = Color(0xFF000000)
    ), Palette(
        primary = Color(0xFF607D8B),
        onPrimary = Color(0xFFFFFFFF)
    )
)

var primary = materialPalette[8].primary
var onPrimary =  materialPalette[8].onPrimary
var error = Color(0xFFBA1A1A)
var onError = Color(0xFFFFFFFF)
var backgroundLight = Color(0xFFF4F4F4)
var onBackgroundLight = Color(0xFF1A1A1A)
var surfaceLight = Color(0xFFFFFFFF)
var onSurfaceLight = Color(0xFF1A1A1A)
var backgroundDark = Color(0xFF212121)
var onBackgroundDark = Color(0xFFE0E0E0)
var surfaceDark = Color(0xFF2C2C2C)
var onSurfaceDark = Color(0xFFE0E0E0)
private val lightScheme = lightColorScheme(
    primary = primary,
    onPrimary = onPrimary,
    primaryContainer = primary.copy(
        red = surfaceLight.red*0.8f + primary.red*0.2f,
        green = surfaceLight.green*0.8f + primary.green*0.2f,
        blue = surfaceLight.blue*0.8f + primary.blue*0.2f
    ),
    onPrimaryContainer = onSurfaceLight,
    secondary = primary,
    onSecondary = onPrimary,
    secondaryContainer = primary,
    onSecondaryContainer = onPrimary,
    tertiary = primary,
    onTertiary = onPrimary,
    tertiaryContainer = primary,
    onTertiaryContainer = onPrimary,
    error = error,
    onError = onError,
    errorContainer = error,
    onErrorContainer = onError,
    background = backgroundLight,
    onBackground = onBackgroundLight,
    surface = surfaceLight,
    onSurface = onSurfaceLight,
    surfaceVariant = surfaceLight,
    onSurfaceVariant = onSurfaceLight,
    outline = surfaceLight,
    outlineVariant = surfaceLight,
    scrim = Color(0xFF888888),
    inverseSurface = surfaceDark,
    inverseOnSurface = onSurfaceDark,
    inversePrimary = primary,
    surfaceDim = surfaceLight,
    surfaceBright = surfaceLight,
    surfaceContainerLowest = surfaceLight,
    surfaceContainerLow = surfaceLight,
    surfaceContainer = surfaceLight,
    surfaceContainerHigh = surfaceLight,
    surfaceContainerHighest = surfaceLight,
)

private val darkScheme = darkColorScheme(
    primary = primary,
    onPrimary = onPrimary,
    primaryContainer = primary.copy(
        red = surfaceDark.red*0.8f + primary.red*0.2f,
        green = surfaceDark.green*0.8f + primary.green*0.2f,
        blue = surfaceDark.blue*0.8f + primary.blue*0.2f
    ),
    onPrimaryContainer = onSurfaceDark,
    secondary = primary,
    onSecondary = onPrimary,
    secondaryContainer = primary,
    onSecondaryContainer = onPrimary,
    tertiary = primary,
    onTertiary = onPrimary,
    tertiaryContainer = primary,
    onTertiaryContainer = onPrimary,
    error = error,
    onError = onError,
    errorContainer = error,
    onErrorContainer = onError,
    background = backgroundDark,
    onBackground = onBackgroundDark,
    surface = surfaceDark,
    onSurface = onSurfaceDark,
    surfaceVariant = surfaceDark,
    onSurfaceVariant = onSurfaceDark,
    outline = surfaceDark,
    outlineVariant = surfaceDark,
    scrim = Color(0xFF888888),
    inverseSurface = surfaceLight,
    inverseOnSurface = onSurfaceLight,
    inversePrimary = primary,
    surfaceDim = surfaceDark,
    surfaceBright = surfaceDark,
    surfaceContainerLowest = surfaceDark,
    surfaceContainerLow = surfaceDark,
    surfaceContainer = surfaceDark,
    surfaceContainerHigh = surfaceDark,
    surfaceContainerHighest = surfaceDark,
)

@Composable
fun GChatTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable() () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> darkScheme
        else -> lightScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}

