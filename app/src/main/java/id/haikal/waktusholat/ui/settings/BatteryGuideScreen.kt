package id.haikal.waktusholat.ui.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings as AndroidSettings
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import id.haikal.waktusholat.R
import id.haikal.waktusholat.ui.common.ScreenHeader
import id.haikal.waktusholat.ui.theme.Dimens
import id.haikal.waktusholat.ui.theme.waktuSholatColors

@Composable
fun BatteryGuideScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var exempt by remember { mutableStateOf(isBatteryExempt(context)) }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        exempt = isBatteryExempt(context)
    }

    Column(modifier = modifier.fillMaxSize()) {
        ScreenHeader(
            title = stringResource(R.string.battery_guide_title),
            onBack = onBack,
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = Dimens.Space4,
                end = Dimens.Space4,
                bottom = Dimens.Space6,
            ),
            verticalArrangement = Arrangement.spacedBy(Dimens.Space3),
        ) {
            item {
                Text(
                    text = stringResource(R.string.battery_guide_intro),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            item {
                ExemptionCard(
                    exempt = exempt,
                    onRequestExemption = { requestBatteryExemption(context) },
                )
            }
            item {
                Text(
                    text = stringResource(R.string.battery_brands_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = Dimens.Space2),
                )
            }
            items(BRAND_GUIDES, key = { it.nameRes }) { guide ->
                BrandGuideCard(guide = guide)
            }
            item {
                Text(
                    text = stringResource(R.string.battery_guide_disclaimer),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = Dimens.Space2),
                )
            }
        }
    }
}

@Composable
private fun ExemptionCard(
    exempt: Boolean,
    onRequestExemption: () -> Unit,
) {
    val container = if (exempt) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.waktuSholatColors.warningContainer
    }
    val content = if (exempt) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.waktuSholatColors.onWarningContainer
    }

    Surface(
        color = container,
        contentColor = content,
        shape = RoundedCornerShape(Dimens.RadiusLg),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(Dimens.Space4)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (exempt) Icons.Filled.Check else Icons.Filled.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(Dimens.IconMd),
                )
                Spacer(Modifier.width(Dimens.Space2))
                Text(
                    text = stringResource(
                        if (exempt) R.string.battery_status_exempt
                        else R.string.battery_status_not_exempt,
                    ),
                    style = MaterialTheme.typography.titleSmall,
                )
            }
            Text(
                text = stringResource(R.string.battery_status_explain),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = Dimens.Space2),
            )
            if (!exempt) {
                TextButton(
                    onClick = onRequestExemption,
                    modifier = Modifier.padding(top = Dimens.Space1),
                ) {
                    Text(stringResource(R.string.battery_exemption_action))
                }
            }
        }
    }
}

@Composable
private fun BrandGuideCard(guide: BrandGuide) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(Dimens.RadiusLg),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(Dimens.Space4)) {
            Text(
                text = stringResource(guide.nameRes),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(Dimens.Space2))
            guide.stepRes.forEachIndexed { index, stepRes ->
                Row(modifier = Modifier.padding(vertical = Dimens.Space1)) {
                    Text(
                        text = "${index + 1}.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.width(Dimens.ListNumberWidth),
                    )
                    Text(
                        text = stringResource(stepRes),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

private fun isBatteryExempt(context: Context): Boolean {
    val powerManager = context.getSystemService(PowerManager::class.java) ?: return false
    return powerManager.isIgnoringBatteryOptimizations(context.packageName)
}

private fun requestBatteryExemption(context: Context) {
    val directIntent = Intent(
        AndroidSettings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
        Uri.parse("package:${context.packageName}"),
    )
    val launched = runCatching { context.startActivity(directIntent) }.isSuccess
    if (!launched) {
        runCatching {
            context.startActivity(Intent(AndroidSettings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
        }
    }
}

private data class BrandGuide(
    @param:StringRes val nameRes: Int,
    val stepRes: List<Int>,
)

private val BRAND_GUIDES = listOf(
    BrandGuide(
        nameRes = R.string.battery_brand_xiaomi,
        stepRes = listOf(
            R.string.battery_xiaomi_step_1,
            R.string.battery_xiaomi_step_2,
            R.string.battery_xiaomi_step_3,
        ),
    ),
    BrandGuide(
        nameRes = R.string.battery_brand_oppo,
        stepRes = listOf(
            R.string.battery_oppo_step_1,
            R.string.battery_oppo_step_2,
            R.string.battery_oppo_step_3,
        ),
    ),
    BrandGuide(
        nameRes = R.string.battery_brand_vivo,
        stepRes = listOf(
            R.string.battery_vivo_step_1,
            R.string.battery_vivo_step_2,
            R.string.battery_vivo_step_3,
        ),
    ),
    BrandGuide(
        nameRes = R.string.battery_brand_samsung,
        stepRes = listOf(
            R.string.battery_samsung_step_1,
            R.string.battery_samsung_step_2,
            R.string.battery_samsung_step_3,
        ),
    ),
    BrandGuide(
        nameRes = R.string.battery_brand_realme,
        stepRes = listOf(
            R.string.battery_realme_step_1,
            R.string.battery_realme_step_2,
            R.string.battery_realme_step_3,
        ),
    ),
)
