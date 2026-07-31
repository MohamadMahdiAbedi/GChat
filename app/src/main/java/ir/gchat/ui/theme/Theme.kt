package ir.gchat.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.Typography

val primaryLight = Color(0xFF296A47)
val onPrimaryLight = Color(0xFFFFFFFF)
val primaryContainerLight = Color(0xFFAEF2C5)
val onPrimaryContainerLight = Color(0xFF095131)
val secondaryLight = Color(0xFF4E6355)
val onSecondaryLight = Color(0xFFFFFFFF)
val secondaryContainerLight = Color(0xFFD1E8D6)
val onSecondaryContainerLight = Color(0xFF374B3E)
val tertiaryLight = Color(0xFF3B6471)
val onTertiaryLight = Color(0xFFFFFFFF)
val tertiaryContainerLight = Color(0xFFBFE9F8)
val onTertiaryContainerLight = Color(0xFF224C58)
val errorLight = Color(0xFFBA1A1A)
val onErrorLight = Color(0xFFFFFFFF)
val errorContainerLight = Color(0xFFFFDAD6)
val onErrorContainerLight = Color(0xFF93000A)
val backgroundLight = Color(0xFFF6FBF4)
val onBackgroundLight = Color(0xFF171D19)
val surfaceLight = Color(0xFFF6FBF4)
val onSurfaceLight = Color(0xFF171D19)
val surfaceVariantLight = Color(0xFFDCE5DC)
val onSurfaceVariantLight = Color(0xFF404942)
val outlineLight = Color(0xFF717972)
val outlineVariantLight = Color(0xFFC0C9C0)
val scrimLight = Color(0xFF000000)
val inverseSurfaceLight = Color(0xFF2C322D)
val inverseOnSurfaceLight = Color(0xFFEDF2EB)
val inversePrimaryLight = Color(0xFF93D5AB)
val surfaceDimLight = Color(0xFFD6DBD5)
val surfaceBrightLight = Color(0xFFF6FBF4)
val surfaceContainerLowestLight = Color(0xFFFFFFFF)
val surfaceContainerLowLight = Color(0xFFF0F5EE)
val surfaceContainerLight = Color(0xFFEAEFE8)
val surfaceContainerHighLight = Color(0xFFE4EAE3)
val surfaceContainerHighestLight = Color(0xFFDFE4DD)

val primaryDark = Color(0xFF93D5AB)
val onPrimaryDark = Color(0xFF003920)
val primaryContainerDark = Color(0xFF095131)
val onPrimaryContainerDark = Color(0xFFAEF2C5)
val secondaryDark = Color(0xFFB5CCBA)
val onSecondaryDark = Color(0xFF213528)
val secondaryContainerDark = Color(0xFF374B3E)
val onSecondaryContainerDark = Color(0xFFD1E8D6)
val tertiaryDark = Color(0xFFA3CDDC)
val onTertiaryDark = Color(0xFF033541)
val tertiaryContainerDark = Color(0xFF224C58)
val onTertiaryContainerDark = Color(0xFFBFE9F8)
val errorDark = Color(0xFFFFB4AB)
val onErrorDark = Color(0xFF690005)
val errorContainerDark = Color(0xFF93000A)
val onErrorContainerDark = Color(0xFFFFDAD6)
val backgroundDark = Color(0xFF0F1511)
val onBackgroundDark = Color(0xFFDFE4DD)
val surfaceDark = Color(0xFF0F1511)
val onSurfaceDark = Color(0xFFDFE4DD)
val surfaceVariantDark = Color(0xFF404942)
val onSurfaceVariantDark = Color(0xFFC0C9C0)
val outlineDark = Color(0xFF8A938B)
val outlineVariantDark = Color(0xFF404942)
val scrimDark = Color(0xFF000000)
val inverseSurfaceDark = Color(0xFFDFE4DD)
val inverseOnSurfaceDark = Color(0xFF2C322D)
val inversePrimaryDark = Color(0xFF296A47)
val surfaceDimDark = Color(0xFF0F1511)
val surfaceBrightDark = Color(0xFF353B36)
val surfaceContainerLowestDark = Color(0xFF0A0F0C)
val surfaceContainerLowDark = Color(0xFF171D19)
val surfaceContainerDark = Color(0xFF1B211D)
val surfaceContainerHighDark = Color(0xFF262B27)
val surfaceContainerHighestDark = Color(0xFF313632)
private val lightScheme = lightColorScheme(
    primary = primaryLight,
    onPrimary = onPrimaryLight,
    primaryContainer = primaryContainerLight,
    onPrimaryContainer = onPrimaryContainerLight,
    secondary = secondaryLight,
    onSecondary = onSecondaryLight,
    secondaryContainer = secondaryContainerLight,
    onSecondaryContainer = onSecondaryContainerLight,
    tertiary = tertiaryLight,
    onTertiary = onTertiaryLight,
    tertiaryContainer = tertiaryContainerLight,
    onTertiaryContainer = onTertiaryContainerLight,
    error = errorLight,
    onError = onErrorLight,
    errorContainer = errorContainerLight,
    onErrorContainer = onErrorContainerLight,
    background = backgroundLight,
    onBackground = onBackgroundLight,
    surface = surfaceLight,
    onSurface = onSurfaceLight,
    surfaceVariant = surfaceVariantLight,
    onSurfaceVariant = onSurfaceVariantLight,
    outline = outlineLight,
    outlineVariant = outlineVariantLight,
    scrim = scrimLight,
    inverseSurface = inverseSurfaceLight,
    inverseOnSurface = inverseOnSurfaceLight,
    inversePrimary = inversePrimaryLight,
    surfaceDim = surfaceDimLight,
    surfaceBright = surfaceBrightLight,
    surfaceContainerLowest = surfaceContainerLowestLight,
    surfaceContainerLow = surfaceContainerLowLight,
    surfaceContainer = surfaceContainerLight,
    surfaceContainerHigh = surfaceContainerHighLight,
    surfaceContainerHighest = surfaceContainerHighestLight,
)

private val darkScheme = darkColorScheme(
    primary = primaryDark,
    onPrimary = onPrimaryDark,
    primaryContainer = primaryContainerDark,
    onPrimaryContainer = onPrimaryContainerDark,
    secondary = secondaryDark,
    onSecondary = onSecondaryDark,
    secondaryContainer = secondaryContainerDark,
    onSecondaryContainer = onSecondaryContainerDark,
    tertiary = tertiaryDark,
    onTertiary = onTertiaryDark,
    tertiaryContainer = tertiaryContainerDark,
    onTertiaryContainer = onTertiaryContainerDark,
    error = errorDark,
    onError = onErrorDark,
    errorContainer = errorContainerDark,
    onErrorContainer = onErrorContainerDark,
    background = backgroundDark,
    onBackground = onBackgroundDark,
    surface = surfaceDark,
    onSurface = onSurfaceDark,
    surfaceVariant = surfaceVariantDark,
    onSurfaceVariant = onSurfaceVariantDark,
    outline = outlineDark,
    outlineVariant = outlineVariantDark,
    scrim = scrimDark,
    inverseSurface = inverseSurfaceDark,
    inverseOnSurface = inverseOnSurfaceDark,
    inversePrimary = inversePrimaryDark,
    surfaceDim = surfaceDimDark,
    surfaceBright = surfaceBrightDark,
    surfaceContainerLowest = surfaceContainerLowestDark,
    surfaceContainerLow = surfaceContainerLowDark,
    surfaceContainer = surfaceContainerDark,
    surfaceContainerHigh = surfaceContainerHighDark,
    surfaceContainerHighest = surfaceContainerHighestDark,
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

