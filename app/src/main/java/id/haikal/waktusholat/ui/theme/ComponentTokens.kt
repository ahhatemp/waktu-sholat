package id.haikal.waktusholat.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

object PrayerRowTokens {
    val height: Dp = Dimens.PrayerRowHeight
    val padding: Dp = Dimens.Space4
    val background: Color @Composable get() = MaterialTheme.colorScheme.surface
    val backgroundNext: Color @Composable get() = MaterialTheme.colorScheme.primaryContainer
    val foreground: Color @Composable get() = MaterialTheme.colorScheme.onSurface
    val foregroundNext: Color @Composable get() = MaterialTheme.colorScheme.onPrimaryContainer
    val foregroundPast: Color @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
}

object NextPrayerCardTokens {
    val padding: Dp = Dimens.Space6
    val background: Color @Composable get() = MaterialTheme.colorScheme.primary
    val foreground: Color @Composable get() = MaterialTheme.colorScheme.onPrimary
}

object NextBadgeTokens {
    val background: Color @Composable get() = MaterialTheme.waktuSholatColors.accentContainer
    val foreground: Color @Composable get() = MaterialTheme.waktuSholatColors.onAccentContainer
}

object PermissionBannerTokens {
    val background: Color @Composable get() = MaterialTheme.waktuSholatColors.warningContainer
    val foreground: Color @Composable get() = MaterialTheme.waktuSholatColors.onWarningContainer
}

object BottomBarTokens {
    val background: Color @Composable get() = MaterialTheme.colorScheme.surface
    val iconActive: Color @Composable get() = MaterialTheme.colorScheme.primary
    val iconInactive: Color @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
    val indicator: Color @Composable get() = MaterialTheme.colorScheme.primaryContainer
}
