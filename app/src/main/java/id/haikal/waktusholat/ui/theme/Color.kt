package id.haikal.waktusholat.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

internal val LightColors = lightColorScheme(
    primary = Green700,
    onPrimary = Neutral0,
    primaryContainer = Green100,
    onPrimaryContainer = Green900,
    secondary = Gold800,
    onSecondary = Neutral0,
    secondaryContainer = Gold100,
    onSecondaryContainer = Gold950,
    tertiary = Green500,
    onTertiary = Neutral0,
    tertiaryContainer = Green50,
    onTertiaryContainer = Green900,
    background = Neutral50,
    onBackground = Neutral900,
    surface = Neutral0,
    onSurface = Neutral900,
    surfaceVariant = Neutral100,
    onSurfaceVariant = Neutral600,
    surfaceTint = Green700,
    inverseSurface = Neutral900,
    inverseOnSurface = Neutral50,
    inversePrimary = Green300,
    error = Red600,
    onError = Neutral0,
    errorContainer = Red300,
    onErrorContainer = Neutral950,
    outline = Neutral500,
    outlineVariant = Neutral200,
    scrim = Neutral950,
    surfaceDim = Neutral200,
    surfaceBright = Neutral0,
    surfaceContainerLowest = Neutral0,
    surfaceContainerLow = Neutral50,
    surfaceContainer = Neutral100,
    surfaceContainerHigh = Neutral100,
    surfaceContainerHighest = Neutral200,
)

internal val DarkColors = darkColorScheme(
    primary = Green300,
    onPrimary = Green900,
    primaryContainer = Green800,
    onPrimaryContainer = Green100,
    secondary = Gold300,
    onSecondary = Gold950,
    secondaryContainer = Gold950,
    onSecondaryContainer = Gold100,
    tertiary = Green200,
    onTertiary = Green950,
    tertiaryContainer = Green900,
    onTertiaryContainer = Green100,
    background = Neutral950,
    onBackground = Neutral50,
    surface = Neutral900,
    onSurface = Neutral50,
    surfaceVariant = Neutral800,
    onSurfaceVariant = Neutral300,
    surfaceTint = Green300,
    inverseSurface = Neutral50,
    inverseOnSurface = Neutral900,
    inversePrimary = Green700,
    error = Red300,
    onError = Neutral950,
    errorContainer = Red600,
    onErrorContainer = Neutral0,
    outline = Neutral400,
    outlineVariant = Neutral700,
    scrim = Neutral950,
    surfaceDim = Neutral950,
    surfaceBright = Neutral700,
    surfaceContainerLowest = Neutral950,
    surfaceContainerLow = Neutral900,
    surfaceContainer = Neutral800,
    surfaceContainerHigh = Neutral800,
    surfaceContainerHighest = Neutral700,
)

@Immutable
data class WaktuSholatExtendedColors(
    val accent: Color,
    val accentContainer: Color,
    val onAccentContainer: Color,
    val warningContainer: Color,
    val onWarningContainer: Color,
)

internal val LightExtendedColors = WaktuSholatExtendedColors(
    accent = Gold800,
    accentContainer = Gold100,
    onAccentContainer = Gold950,
    warningContainer = Gold100,
    onWarningContainer = Gold950,
)

internal val DarkExtendedColors = WaktuSholatExtendedColors(
    accent = Gold300,
    accentContainer = Gold950,
    onAccentContainer = Gold100,
    warningContainer = Gold950,
    onWarningContainer = Gold100,
)

internal val LocalWaktuSholatColors = staticCompositionLocalOf { LightExtendedColors }

val MaterialTheme.waktuSholatColors: WaktuSholatExtendedColors
    @Composable
    @ReadOnlyComposable
    get() = LocalWaktuSholatColors.current
