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
import androidx.compose.runtime.mutableStateListOf

var materialPalette = mutableStateListOf(
    Palette(
        primary = Color(0xFFF44336), onPrimary = Color(0xFFFFFFFF)
    ), Palette(
        primary = Color(0xFFE91E63), onPrimary = Color(0xFFFFFFFF)
    ), Palette(
        primary = Color(0xFF9C27B0), onPrimary = Color(0xFFFFFFFF)
    ), Palette(
        primary = Color(0xFF673AB7), onPrimary = Color(0xFFFFFFFF)
    ), Palette(
        primary = Color(0xFF3F51B5), onPrimary = Color(0xFFFFFFFF)
    ), Palette(
        primary = Color(0xFF2196F3), onPrimary = Color(0xFFFFFFFF)
        //onPrimary = Color(0xFF000000)
    ), Palette(
        primary = Color(0xFF03A9F4), onPrimary = Color(0xFFFFFFFF)
        //onPrimary = Color(0xFF000000)
    ), Palette(
        primary = Color(0xFF00BCD4), onPrimary = Color(0xFFFFFFFF)
        //onPrimary = Color(0xFF000000)
    ), Palette(
        primary = Color(0xFF009688),
        //#008577 in android 5 - 11
        onPrimary = Color(0xFFFFFFFF)
    ), Palette(
        primary = Color(0xFF4CAF50), onPrimary = Color(0xFFFFFFFF)
        //onPrimary = Color(0xFF000000)
    ), Palette(
        primary = Color(0xFF8BC34A), onPrimary = Color(0xFFFFFFFF)
        //onPrimary = Color(0xFF000000)
    ), Palette(
        primary = Color(0xFFCDDC39), onPrimary = Color(0xFF000000)
    ), Palette(
        primary = Color(0xFFFFEB3B), onPrimary = Color(0xFF000000)
    ), Palette(
        primary = Color(0xFFFFC107), onPrimary = Color(0xFF000000)
    ), Palette(
        primary = Color(0xFFFF9800), onPrimary = Color(0xFFFFFFFF)
        //onPrimary = Color(0xFF000000)
    ), Palette(
        primary = Color(0xFFFF5722), onPrimary = Color(0xFFFFFFFF)
        //onPrimary = Color(0xFF000000)
    ), Palette(
        primary = Color(0xFF795548), onPrimary = Color(0xFFFFFFFF)
    ), Palette(
        primary = Color(0xFF9E9E9E), onPrimary = Color(0xFF000000)
    ), Palette(
        primary = Color(0xFF607D8B), onPrimary = Color(0xFFFFFFFF)
    ),
    // Mint Palette
    Palette(
        primary = Color(0xFF296A47), onPrimary = Color(0xFFFFFFFF)
    )
)

var backgroundLight = Color(0xFFF4F4F4)
//var onBackgroundLight = Color(0xFF1A1A1A)

var surfaceLight = Color(0xFFFFFFFF)
//var onSurfaceLight = Color(0xFF1A1A1A)

var backgroundDark = Color(0xFF212121)
//var onBackgroundDark = Color(0xFFE0E0E0)

//var surfaceDark = Color(0xFF2C2C2C)
var surfaceDark = Color(0xFF333333)
//var onSurfaceDark = Color(0xFFE0E0E0)

var tWallpaperColors = mutableStateListOf(
    listOf(
        Color(0xFFDBDDBB),
        Color(0xFF6BA587),
        Color(0xFFD5D88D),
        Color(0xFF88B884)
    ),
    listOf(
        Color(0xFF4F5BD5),
        Color(0xFF962FBF),
        Color(0xFFDD6CB9),
        Color(0xFFFEC496)
    ),
    listOf(
        Color(0xFFBAA161),
        Color(0xFFDDB56D),
        Color(0xFFCEA668),
        Color(0xFFFAF4D2)
    ),
    listOf(
        Color(0xFFECD893),
        Color(0xFFE5A1D0),
        Color(0xFFEDD594),
        Color(0xFFD1A3E2)
    ),
    listOf(
        Color(0xFFEFD359),
        Color(0xFFE984D8),
        Color(0xFFAC86ED),
        Color(0xFF40CDDE)
    ),
    listOf(
        Color(0xFFFBD9E6),
        Color(0xFFFB9AE5),
        Color(0xFFD5F7FF),
        Color(0xFF73CAFF)
    ),
    listOf(
        Color(0xFFB4936E),
        Color(0xFFEAB9D9),
        Color(0xFF8376C2),
        Color(0xFFE4B2EA)
    ),
    listOf(
        Color(0xFF679CED),
        Color(0xFFE39FEA),
        Color(0xFF888DEC),
        Color(0xFF8ADBF2)
    ),
    listOf(
        Color(0xFF85D685),
        Color(0xFF67A3F2),
        Color(0xFF8FE1D6),
        Color(0xFFDCEB92)
    ),
    listOf(
        Color(0xFFB9E2FF),
        Color(0xFFECCBFF),
        Color(0xFFA2B4FF),
        Color(0xFFDAEACB)
    ),
    listOf(
        Color(0xFFEFB7DC),
        Color(0xFFC6B1EF),
        Color(0xFFB1E9EA),
        Color(0xFF97BEEB)
    ),
    listOf(
        Color(0xFFFBE37D),
        Color(0xFF336F55),
        Color(0xFFFFF5C5),
        Color(0xFF7FA381)
    ),
    listOf(
        Color(0xFFB2E3DD),
        Color(0xFFBBEAD5),
        Color(0xFF9FB0EA),
        Color(0xFFB0CDEB)
    ),
    listOf(
        Color(0xFFF7DD6D),
        Color(0xFFE96CAF),
        Color(0xFFEDAC4C),
        Color(0xFFA464F4)
    ),
    listOf(
        Color(0xFFE8C06E),
        Color(0xFFF29EBF),
        Color(0xFFF0E486),
        Color(0xFFEAA36E)
    ),
    listOf(
        Color(0xFFF0C07A),
        Color(0xFFAFD677),
        Color(0xFFE4D573),
        Color(0xFF7FC289)
    ),
    listOf(
        Color(0xFFFFE7B2),
        Color(0xFFE2C0FF),
        Color(0xFFFFC3B2),
        Color(0xFFFFD5B2)
    ),
    listOf(
        Color(0xFF6C8CD4),
        Color(0xFFD4A7C9),
        Color(0xFFB2B1EE),
        Color(0xFF8F9EE1)
    ),
    listOf(
        Color(0xFF527BDD),
        Color(0xFF009FDD),
        Color(0xFFA4DBFF),
        Color(0xFF7BABEE)
    )
)

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
            if (darkTheme) {
                dynamicDarkColorScheme(context).let { scheme ->
                    scheme.copy(
                        background = scheme.surfaceContainer, surface = scheme.surfaceBright
                    )
                }
            } else {
                dynamicLightColorScheme(context).let { scheme ->
                    scheme.copy(
                        background = scheme.surfaceContainer, surface = scheme.surface
                    )
                }
            }
        }

        darkTheme -> darkScheme
        else -> lightScheme
    }

    MaterialTheme(
        colorScheme = colorScheme, typography = Typography(), content = content
    )
}