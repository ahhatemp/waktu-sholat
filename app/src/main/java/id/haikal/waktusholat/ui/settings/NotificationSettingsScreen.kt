package id.haikal.waktusholat.ui.settings

import android.content.Context
import android.media.MediaPlayer
import android.media.Ringtone
import android.media.RingtoneManager
import android.provider.Settings as AndroidSettings
import androidx.annotation.RawRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import id.haikal.waktusholat.R
import id.haikal.waktusholat.domain.model.AdhanSound
import id.haikal.waktusholat.domain.model.Prayer
import id.haikal.waktusholat.domain.model.Settings
import id.haikal.waktusholat.ui.common.ScreenHeader
import id.haikal.waktusholat.ui.common.labelRes
import id.haikal.waktusholat.ui.theme.Dimens

@Composable
fun NotificationSettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: NotificationSettingsViewModel = viewModel(
        factory = NotificationSettingsViewModel.Factory,
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val previewPlayer = remember { SoundPreviewPlayer(context.applicationContext) }

    DisposableEffect(Unit) {
        onDispose { previewPlayer.release() }
    }

    Column(modifier = modifier.fillMaxSize()) {
        ScreenHeader(
            title = stringResource(R.string.notification_settings_title),
            onBack = onBack,
        )
        if (state.loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = Dimens.Space4,
                    end = Dimens.Space4,
                    bottom = Dimens.Space6,
                ),
                verticalArrangement = Arrangement.spacedBy(Dimens.Space2),
            ) {
                item {
                    SectionTitle(stringResource(R.string.notification_section_prayers))
                }
                item {
                    SettingsCard {
                        Prayer.entries.forEachIndexed { index, prayer ->
                            NotifyRow(
                                prayer = prayer,
                                enabled = state.settings.notifyFor(prayer),
                                onToggle = viewModel::setNotify,
                            )
                            if (index != Prayer.entries.lastIndex) SettingsDivider()
                        }
                    }
                }
                item {
                    Column(Modifier.padding(top = Dimens.Space4)) {
                        SectionTitle(stringResource(R.string.notification_section_reminder))
                        Text(
                            text = stringResource(R.string.notification_reminder_description),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = Dimens.Space2),
                        )
                        SettingsCard {
                            Settings.PRE_REMINDER_OPTIONS.forEachIndexed { index, minutes ->
                                ReminderRow(
                                    minutes = minutes,
                                    selected = state.settings.preReminderMinutes == minutes,
                                    onSelect = viewModel::setPreReminder,
                                )
                                if (index != Settings.PRE_REMINDER_OPTIONS.lastIndex) SettingsDivider()
                            }
                        }
                    }
                }
                item {
                    Column(Modifier.padding(top = Dimens.Space4)) {
                        SectionTitle(stringResource(R.string.notification_section_sound))
                        SettingsCard {
                            Prayer.entries.forEachIndexed { index, prayer ->
                                SoundRow(
                                    prayer = prayer,
                                    sound = state.settings.soundFor(prayer),
                                    onSelect = { sound -> viewModel.setSound(prayer, sound) },
                                    onPreview = previewPlayer::play,
                                )
                                if (index != Prayer.entries.lastIndex) SettingsDivider()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(top = Dimens.Space2, bottom = Dimens.Space1),
    )
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(Dimens.RadiusLg),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(content = content)
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}

@Composable
private fun NotifyRow(
    prayer: Prayer,
    enabled: Boolean,
    onToggle: (Prayer, Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(
                value = enabled,
                role = Role.Switch,
                onValueChange = { checked -> onToggle(prayer, checked) },
            )
            .heightIn(min = Dimens.SettingsItemMinHeight)
            .padding(horizontal = Dimens.Space4),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(prayer.labelRes),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        Switch(checked = enabled, onCheckedChange = null)
    }
}

@Composable
private fun ReminderRow(
    minutes: Int,
    selected: Boolean,
    onSelect: (Int) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = { onSelect(minutes) },
            )
            .heightIn(min = Dimens.SettingsItemMinHeight)
            .padding(horizontal = Dimens.Space4),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = reminderLabel(minutes),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        RadioButton(selected = selected, onClick = null)
    }
}

@Composable
private fun reminderLabel(minutes: Int): String =
    if (minutes == 0) {
        stringResource(R.string.reminder_off)
    } else {
        stringResource(R.string.reminder_minutes, minutes)
    }

@Composable
private fun SoundRow(
    prayer: Prayer,
    sound: AdhanSound,
    onSelect: (AdhanSound) -> Unit,
    onPreview: (AdhanSound) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val prayerName = stringResource(prayer.labelRes)
    val chooseLabel = stringResource(R.string.sound_choose, prayerName)

    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClickLabel = chooseLabel) { expanded = true }
                .heightIn(min = Dimens.SettingsItemMinHeight)
                .padding(start = Dimens.Space4, end = Dimens.Space2),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = Dimens.Space2),
            ) {
                Text(
                    text = prayerName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(sound.labelRes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(
                onClick = { onPreview(sound) },
                enabled = sound != AdhanSound.SILENT,
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = stringResource(R.string.sound_preview, prayerName),
                    tint = if (sound != AdhanSound.SILENT) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
            Icon(
                imageVector = Icons.Filled.ArrowDropDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.width(Dimens.Space1))
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            AdhanSound.entries.forEach { option ->
                DropdownMenuItem(
                    text = { Text(stringResource(option.labelRes)) },
                    onClick = {
                        expanded = false
                        onSelect(option)
                    },
                    leadingIcon = {
                        if (option == sound) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                modifier = Modifier.size(Dimens.IconSm),
                            )
                        } else {
                            Spacer(Modifier.size(Dimens.IconSm))
                        }
                    },
                )
            }
        }
    }
}

private class SoundPreviewPlayer(private val context: Context) {

    private var player: MediaPlayer? = null
    private var ringtone: Ringtone? = null

    fun play(sound: AdhanSound) {
        release()
        when (sound) {
            AdhanSound.FULL -> playRaw(R.raw.adzan_full)
            AdhanSound.SHORT -> playRaw(R.raw.adzan_short)
            AdhanSound.DEFAULT -> {
                ringtone = runCatching {
                    RingtoneManager.getRingtone(
                        context,
                        AndroidSettings.System.DEFAULT_NOTIFICATION_URI,
                    )
                }.getOrNull()
                ringtone?.play()
            }

            AdhanSound.SILENT -> Unit
        }
    }

    private fun playRaw(@RawRes resId: Int) {
        player = MediaPlayer.create(context, resId)?.apply {
            setOnCompletionListener { completed ->
                completed.release()
                if (player === completed) player = null
            }
            start()
        }
    }

    fun release() {
        player?.let { mediaPlayer ->
            runCatching { mediaPlayer.stop() }
            mediaPlayer.release()
        }
        player = null
        runCatching { ringtone?.stop() }
        ringtone = null
    }
}
