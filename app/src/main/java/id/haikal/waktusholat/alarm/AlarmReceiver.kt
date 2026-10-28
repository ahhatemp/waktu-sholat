package id.haikal.waktusholat.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import id.haikal.waktusholat.WaktuSholatApp
import id.haikal.waktusholat.di.AppContainer
import id.haikal.waktusholat.domain.model.Prayer
import id.haikal.waktusholat.domain.model.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val prayerKey = intent.getStringExtra(AlarmContract.EXTRA_PRAYER) ?: return
        val prayer = Prayer.entries.firstOrNull { it.key == prayerKey } ?: return
        val kindName = intent.getStringExtra(AlarmContract.EXTRA_KIND) ?: return
        val kind = AlarmKind.entries.firstOrNull { it.name == kindName } ?: return

        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            try {
                runCatching { deliver(context, prayer, kind) }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun deliver(context: Context, prayer: Prayer, kind: AlarmKind) {
        val container = (context.applicationContext as? WaktuSholatApp)?.container ?: return
        val settings = container.settingsRepository.current()
        val now = Instant.now()
        val timeText = timeText(container, settings, prayer, now)

        when (kind) {
            AlarmKind.ADHAN -> {
                if (settings.notifyFor(prayer)) {
                    container.notificationHelper.showAdhan(
                        prayer = prayer,
                        sound = settings.soundFor(prayer),
                        locationName = settings.location?.name,
                        timeText = timeText,
                    )
                }
            }

            AlarmKind.REMINDER -> {
                if (settings.notifyFor(prayer) && settings.preReminderMinutes > 0) {
                    container.notificationHelper.showReminder(
                        prayer = prayer,
                        minutesBefore = settings.preReminderMinutes,
                        timeText = timeText,
                    )
                }
            }
        }

        container.alarmScheduler.scheduleNext(now)
    }

    private fun timeText(
        container: AppContainer,
        settings: Settings,
        prayer: Prayer,
        now: Instant,
    ): String? {
        val location = settings.location ?: return null
        val zone = runCatching { ZoneId.of(location.timezoneId) }.getOrDefault(ZoneId.systemDefault())
        val today = now.atZone(zone).toLocalDate()
        return container.prayerRepository.scheduleFor(today, settings)
            ?.get(prayer)
            ?.format(TIME_FORMAT)
    }

    private companion object {
        val TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    }
}
