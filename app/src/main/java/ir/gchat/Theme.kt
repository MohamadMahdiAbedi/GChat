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
    Color(0xFF607D8B),
    Color(0xFF296A47)
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
        onPrimary = Color(0xFFFFFFFF)
        //onPrimary = Color(0xFF000000)
    ), Palette(
        primary = Color(0xFF03A9F4),
        onPrimary = Color(0xFFFFFFFF)
        //onPrimary = Color(0xFF000000)
    ), Palette(
        primary = Color(0xFF00BCD4),
        onPrimary = Color(0xFFFFFFFF)
        //onPrimary = Color(0xFF000000)
    ), Palette(
        primary = Color(0xFF009688),
        onPrimary = Color(0xFFFFFFFF)
    ), Palette(
        primary = Color(0xFF4CAF50),
        onPrimary = Color(0xFFFFFFFF)
        //onPrimary = Color(0xFF000000)
    ), Palette(
        primary = Color(0xFF8BC34A),
        onPrimary = Color(0xFFFFFFFF)
        //onPrimary = Color(0xFF000000)
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
        onPrimary = Color(0xFFFFFFFF)
        //onPrimary = Color(0xFF000000)
    ), Palette(
        primary = Color(0xFFFF5722),
        onPrimary = Color(0xFFFFFFFF)
        //onPrimary = Color(0xFF000000)
    ), Palette(
        primary = Color(0xFF795548),
        onPrimary = Color(0xFFFFFFFF)
    ), Palette(
        primary = Color(0xFF9E9E9E),
        onPrimary = Color(0xFF000000)
    ), Palette(
        primary = Color(0xFF607D8B),
        onPrimary = Color(0xFFFFFFFF)
    ),
    // GChat Palette
    Palette(
        primary = Color(0xFF296A47),
        onPrimary = Color(0xFFFFFFFF)
    )
)

var backgroundLight = Color(0xFFF4F4F4)
var onBackgroundLight = Color(0xFF1A1A1A)

var surfaceLight = Color(0xFFFFFFFF)
var onSurfaceLight = Color(0xFF1A1A1A)

var backgroundDark = Color(0xFF212121)
var onBackgroundDark = Color(0xFFE0E0E0)

//var surfaceDark = Color(0xFF2C2C2C)
var surfaceDark = Color(0xFF333333)
var onSurfaceDark = Color(0xFFE0E0E0)

@Composable
fun GChatTheme(
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    paletteIndex: Int,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {

    val palette = materialPalette[paletteIndex]
    val primary = palette.primary
    val onPrimary = palette.onPrimary

    val lightScheme = lightColorScheme(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = Color(0xFF747474),
        onPrimaryContainer = Color(0xFFFEFCFC),
        secondary = Color(0xFF5F5E5E),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFE4E2E1),
        onSecondaryContainer = Color(0xFF656464),
        tertiary = Color(0xFF5E5B5D),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFF777375),
        onTertiaryContainer = Color(0xFFFFFBFF),
        error = Color(0xFFBA1A1A),
        onError = Color(0xFFFFFFFF),
        errorContainer = Color(0xFFFFDAD6),
        onErrorContainer = Color(0xFF93000A),
        background = backgroundLight,
        onBackground = Color(0xFF1C1B1B),
        surface = surfaceLight,
        onSurface = Color(0xFF1C1B1B),
        surfaceVariant = Color(0xFFE0E3E3),
        onSurfaceVariant = Color(0xFF444748),
        outline = Color(0xFF747878),
        outlineVariant = Color(0xFFC4C7C7),
        scrim = Color(0xFF000000),
        inverseSurface = Color(0xFF313030),
        inverseOnSurface = Color(0xFFF4F0EF),
        inversePrimary = Color(0xFFC7C6C6),
        surfaceDim = Color(0xFFDDD9D8),
        surfaceBright = Color(0xFFFCF8F8),
        surfaceContainerLowest = Color(0xFFFFFFFF),
        surfaceContainerLow = Color(0xFFF7F3F2),
        surfaceContainer = Color(0xFFF1EDEC),
        surfaceContainerHigh = Color(0xFFEBE7E7),
        surfaceContainerHighest = Color(0xFFE5E2E1)
    )

    val darkScheme = darkColorScheme(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = Color(0xFF919191),
        onPrimaryContainer = Color(0xFF202121),
        secondary = Color(0xFFC8C6C5),
        onSecondary = Color(0xFF303030),
        secondaryContainer = Color(0xFF474746),
        onSecondaryContainer = Color(0xFFB7B5B4),
        tertiary = Color(0xFFCAC5C7),
        onTertiary = Color(0xFF323031),
        tertiaryContainer = Color(0xFF949092),
        onTertiaryContainer = Color(0xFF222022),
        error = Color(0xFFFFB4AB),
        onError = Color(0xFF690005),
        errorContainer = Color(0xFF93000A),
        onErrorContainer = Color(0xFFFFDAD6),
        background = backgroundDark,
        onBackground = Color(0xFFE5E2E1),
        surface = surfaceDark,
        onSurface = Color(0xFFE5E2E1),
        surfaceVariant = Color(0xFF444748),
        onSurfaceVariant = Color(0xFFC4C7C7),
        outline = Color(0xFF8E9192),
        outlineVariant = Color(0xFF444748),
        scrim = Color(0xFF000000),
        inverseSurface = Color(0xFFE5E2E1),
        inverseOnSurface = Color(0xFF313030),
        inversePrimary = Color(0xFF5E5E5E),
        surfaceDim = Color(0xFF141313),
        surfaceBright = Color(0xFF3A3939),
        surfaceContainerLowest = Color(0xFF0E0E0E),
        surfaceContainerLow = Color(0xFF1C1B1B),
        surfaceContainer = Color(0xFF201F1F),
        surfaceContainerHigh = Color(0xFF2A2A2A),
        surfaceContainerHighest = Color(0xFF353434)
    )

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(
                context
            )
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