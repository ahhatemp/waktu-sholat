package id.haikal.waktusholat.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import id.haikal.waktusholat.data.prefs.SettingsRepository
import id.haikal.waktusholat.data.repository.PrayerRepository
import id.haikal.waktusholat.domain.model.Settings
import java.time.Instant
import java.time.ZoneId

class AlarmScheduler(
    context: Context,
    private val settingsRepository: SettingsRepository,
    private val prayerRepository: PrayerRepository,
) {
    private val appContext = context.applicationContext
    private val alarmManager = appContext.getSystemService(AlarmManager::class.java)

    suspend fun scheduleAll(now: Instant = Instant.now()) {
        cancelAll()
        schedule(settingsRepository.current(), now)
    }

    suspend fun scheduleNext(now: Instant = Instant.now()) {
        schedule(settingsRepository.current(), now)
    }

    fun cancelAll() {
        AlarmPlanner.allRequestCodes()
            .filter { it != AlarmPlanner.MIDNIGHT_REQUEST_CODE }
            .forEach { requestCode -> alarmManager.cancel(prayerPendingIntent(requestCode)) }
        alarmManager.cancel(midnightPendingIntent())
    }

    private suspend fun schedule(settings: Settings, now: Instant) {
        val location = settings.location ?: return
        val zone = runCatching { ZoneId.of(location.timezoneId) }.getOrDefault(ZoneId.systemDefault())
        val today = now.atZone(zone).toLocalDate()
        val todayTimes = prayerRepository.scheduleFor(today, settings) ?: return
        val tomorrowTimes = prayerRepository.scheduleFor(today.plusDays(1), settings) ?: return
        val plan = AlarmPlanner.plan(settings, todayTimes, tomorrowTimes, now)

        plan.alarms.forEach { alarm ->
            set(alarm.triggerAt, alarmPendingIntent(alarm))
        }
        plan.midnightAt?.let { triggerAt ->
            set(triggerAt, midnightPendingIntent())
        }
    }

    private fun set(triggerAt: Instant, pendingIntent: PendingIntent) {
        val millis = triggerAt.toEpochMilli()
        val exactScheduled = SchedulingPermissions.canScheduleExactAlarms(appContext) &&
            runCatching {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    millis,
                    pendingIntent,
                )
            }.isSuccess
        if (!exactScheduled) {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                millis,
                pendingIntent,
            )
        }
    }

    private fun alarmPendingIntent(alarm: PlannedAlarm): PendingIntent =
        PendingIntent.getBroadcast(
            appContext,
            alarm.requestCode,
            Intent(appContext, AlarmReceiver::class.java).apply {
                putExtra(AlarmContract.EXTRA_PRAYER, alarm.prayer.key)
                putExtra(AlarmContract.EXTRA_KIND, alarm.kind.name)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    private fun prayerPendingIntent(requestCode: Int): PendingIntent =
        PendingIntent.getBroadcast(
            appContext,
            requestCode,
            Intent(appContext, AlarmReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    private fun midnightPendingIntent(): PendingIntent =
        PendingIntent.getBroadcast(
            appContext,
            AlarmPlanner.MIDNIGHT_REQUEST_CODE,
            Intent(appContext, MidnightReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
}
