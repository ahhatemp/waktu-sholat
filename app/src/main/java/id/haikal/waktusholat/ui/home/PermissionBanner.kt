package id.haikal.waktusholat.ui.home

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import id.haikal.waktusholat.R
import id.haikal.waktusholat.alarm.SchedulingPermissions
import id.haikal.waktusholat.ui.theme.Dimens
import id.haikal.waktusholat.ui.theme.PermissionBannerTokens

@Composable
fun HomePermissionBanners(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var exactAlarmGranted by remember {
        mutableStateOf(SchedulingPermissions.canScheduleExactAlarms(context))
    }
    var notificationsGranted by remember {
        mutableStateOf(SchedulingPermissions.canPostNotifications(context))
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        exactAlarmGranted = SchedulingPermissions.canScheduleExactAlarms(context)
        notificationsGranted = SchedulingPermissions.canPostNotifications(context)
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) {
        notificationsGranted = SchedulingPermissions.canPostNotifications(context)
    }

    if (exactAlarmGranted && notificationsGranted) return

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Dimens.Space2),
    ) {
        if (!exactAlarmGranted) {
            PermissionBanner(
                message = stringResource(R.string.home_banner_exact_alarm),
                actionLabel = stringResource(R.string.home_banner_exact_alarm_action),
                onAction = {
                    launchSettings(
                        context = context,
                        intent = SchedulingPermissions.exactAlarmSettingsIntent(context),
                    )
                },
            )
        }
        if (!notificationsGranted) {
            PermissionBanner(
                message = stringResource(R.string.home_banner_notifications),
                actionLabel = stringResource(R.string.home_banner_notifications_action),
                onAction = {
                    val needsRuntimeRequest = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.POST_NOTIFICATIONS,
                        ) != PackageManager.PERMISSION_GRANTED
                    if (needsRuntimeRequest) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        launchSettings(
                            context = context,
                            intent = SchedulingPermissions.notificationSettingsIntent(context),
                        )
                    }
                },
            )
        }
    }
}

@Composable
private fun PermissionBanner(
    message: String,
    actionLabel: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = PermissionBannerTokens.background,
        contentColor = PermissionBannerTokens.foreground,
        shape = RoundedCornerShape(Dimens.RadiusMd),
        modifier = modifier
            .fillMaxWidth()
            .semantics { liveRegion = LiveRegionMode.Polite },
    ) {
        Row(
            modifier = Modifier.padding(start = Dimens.Space4, end = Dimens.Space1),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = null,
                modifier = Modifier.size(Dimens.IconSm),
            )
            Spacer(Modifier.width(Dimens.Space3))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = Dimens.Space3),
            )
            TextButton(onClick = onAction) {
                Text(text = actionLabel)
            }
        }
    }
}

private fun launchSettings(context: Context, intent: Intent) {
    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        runCatching { context.startActivity(SchedulingPermissions.appDetailsIntent(context)) }
    }
}
