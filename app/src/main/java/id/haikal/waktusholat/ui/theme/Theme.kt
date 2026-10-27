package id.haikal.waktusholat.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@Composable
fun WaktuSholatTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    val extendedColors = if (darkTheme) DarkExtendedColors else LightExtendedColors

    CompositionLocalProvider(LocalWaktuSholatColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = WaktuSholatTypography,
            shapes = WaktuSholatShapes,
            content = content,
        )
    }
}
