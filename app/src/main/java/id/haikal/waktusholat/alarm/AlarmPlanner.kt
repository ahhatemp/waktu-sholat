package id.haikal.waktusholat.alarm

import id.haikal.waktusholat.domain.model.Prayer
import id.haikal.waktusholat.domain.model.PrayerTimes
import id.haikal.waktusholat.domain.model.Settings
import java.time.Instant
import java.time.ZoneId

enum class AlarmKind { ADHAN, REMINDER }

data class PlannedAlarm(
    val requestCode: Int,
    val triggerAt: Instant,
    val prayer: Prayer,
    val kind: AlarmKind,
)

data class AlarmPlan(
    val alarms: List<PlannedAlarm>,
    val midnightAt: Instant?,
)

object AlarmPlanner {
    const val MIDNIGHT_REQUEST_CODE = 9_900

    private const val DAY_OFFSETS = 2
    private const val DAY_REQUEST_CODE_STRIDE = 100
    private const val MIDNIGHT_HOUR = 0
    private const val MIDNIGHT_MINUTE = 5

    fun plan(
        settings: Settings,
        today: PrayerTimes,
        tomorrow: PrayerTimes,
        now: Instant,
    ): AlarmPlan {
        val enabled = Prayer.entries.filter(settings::notifyFor)
        val hasRemainingToday = enabled.any { prayer ->
            today.instantOf(prayer)?.isAfter(now) == true
        }
        val days = if (hasRemainingToday) {
            listOf(0 to today)
        } else {
            listOf(1 to tomorrow)
        }

        val alarms = buildList {
            days.forEach { (dayOffset, times) ->
                enabled.forEach { prayer ->
                    val instant = times.instantOf(prayer) ?: return@forEach
                    if (instant.isAfter(now)) {
                        add(
                            PlannedAlarm(
                                requestCode = requestCode(dayOffset, prayer, AlarmKind.ADHAN),
                                triggerAt = instant,
                                prayer = prayer,
                                kind = AlarmKind.ADHAN,
                            ),
                        )
                    }
                    val reminderMinutes = settings.preReminderMinutes
                    if (reminderMinutes > 0) {
                        val reminderAt = instant.minusSeconds(reminderMinutes * 60L)
                        if (reminderAt.isAfter(now)) {
                            add(
                                PlannedAlarm(
                                    requestCode = requestCode(dayOffset, prayer, AlarmKind.REMINDER),
                                    triggerAt = reminderAt,
                                    prayer = prayer,
                                    kind = AlarmKind.REMINDER,
                                ),
                            )
                        }
                    }
                }
            }
        }

        return AlarmPlan(
            alarms = alarms,
            midnightAt = nextMidnight(settings, now),
        )
    }

    fun allRequestCodes(): List<Int> = buildList {
        repeat(DAY_OFFSETS) { dayOffset ->
            Prayer.entries.forEach { prayer ->
                AlarmKind.entries.forEach { kind ->
                    add(requestCode(dayOffset, prayer, kind))
                }
            }
        }
        add(MIDNIGHT_REQUEST_CODE)
    }

    private fun requestCode(dayOffset: Int, prayer: Prayer, kind: AlarmKind): Int =
        dayOffset * DAY_REQUEST_CODE_STRIDE + prayer.ordinal * 2 + kind.ordinal

    private fun nextMidnight(settings: Settings, now: Instant): Instant? {
        val timezone = settings.location?.timezoneId ?: return null
        val zone = runCatching { ZoneId.of(timezone) }.getOrDefault(ZoneId.systemDefault())
        val today = now.atZone(zone).toLocalDate()
        val todayMidnight = today.atTime(MIDNIGHT_HOUR, MIDNIGHT_MINUTE).atZone(zone).toInstant()
        return if (todayMidnight.isAfter(now)) {
            todayMidnight
        } else {
            today.plusDays(1).atTime(MIDNIGHT_HOUR, MIDNIGHT_MINUTE).atZone(zone).toInstant()
        }
    }
}
