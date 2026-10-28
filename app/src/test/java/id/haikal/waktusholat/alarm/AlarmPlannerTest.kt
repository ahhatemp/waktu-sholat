package id.haikal.waktusholat.alarm

import id.haikal.waktusholat.domain.model.Location
import id.haikal.waktusholat.domain.model.LocationSource
import id.haikal.waktusholat.domain.model.Prayer
import id.haikal.waktusholat.domain.model.PrayerTimes
import id.haikal.waktusholat.domain.model.Settings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class AlarmPlannerTest {

    private val date = LocalDate.of(2026, 10, 28)
    private val zone = "Asia/Jakarta"
    private val location = Location(
        name = "Kota Bandung",
        latitude = -6.9175,
        longitude = 107.6191,
        timezoneId = zone,
        source = LocationSource.MANUAL,
    )

    private fun times(date: LocalDate, timezone: String = zone) = PrayerTimes(
        date = date,
        timezoneId = timezone,
        times = mapOf(
            Prayer.IMSAK to LocalTime.of(3, 56),
            Prayer.SUBUH to LocalTime.of(4, 6),
            Prayer.TERBIT to LocalTime.of(5, 21),
            Prayer.DZUHUR to LocalTime.of(11, 36),
            Prayer.ASHAR to LocalTime.of(14, 49),
            Prayer.MAGHRIB to LocalTime.of(17, 46),
            Prayer.ISYA to LocalTime.of(18, 57),
        ),
    )

    private fun instant(
        date: LocalDate,
        time: LocalTime,
        timezone: String = zone,
    ): Instant = date.atTime(time).atZone(ZoneId.of(timezone)).toInstant()

    private fun settings(
        notifications: Map<Prayer, Boolean> = emptyMap(),
        preReminderMinutes: Int = 0,
        hasLocation: Boolean = true,
    ) = Settings(
        location = if (hasLocation) location else null,
        notifications = notifications,
        preReminderMinutes = preReminderMinutes,
    )

    private fun plan(
        now: Instant,
        settings: Settings = settings(),
    ) = AlarmPlanner.plan(
        settings = settings,
        today = times(date),
        tomorrow = times(date.plusDays(1)),
        now = now,
    )

    @Test
    fun `default settings schedule obligatory prayers only`() {
        val result = plan(now = instant(date, LocalTime.of(3, 0)))

        val prayers = result.alarms.filter { it.kind == AlarmKind.ADHAN }.map { it.prayer }
        assertEquals(
            listOf(Prayer.SUBUH, Prayer.DZUHUR, Prayer.ASHAR, Prayer.MAGHRIB, Prayer.ISYA),
            prayers,
        )
    }

    @Test
    fun `disabled prayer produces no alarm`() {
        val result = plan(
            now = instant(date, LocalTime.of(3, 0)),
            settings = settings(notifications = mapOf(Prayer.SUBUH to false)),
        )

        assertFalse(result.alarms.any { it.prayer == Prayer.SUBUH })
        assertEquals(4, result.alarms.size)
    }

    @Test
    fun `imsak and sunrise can be enabled`() {
        val result = plan(
            now = instant(date, LocalTime.of(3, 0)),
            settings = settings(
                notifications = mapOf(Prayer.IMSAK to true, Prayer.TERBIT to true),
            ),
        )

        assertTrue(result.alarms.any { it.prayer == Prayer.IMSAK })
        assertTrue(result.alarms.any { it.prayer == Prayer.TERBIT })
    }

    @Test
    fun `pre reminder adds alarm before each adhan`() {
        val result = plan(
            now = instant(date, LocalTime.of(3, 0)),
            settings = settings(preReminderMinutes = 10),
        )

        val subuhReminder = result.alarms.single {
            it.prayer == Prayer.SUBUH && it.kind == AlarmKind.REMINDER
        }
        assertEquals(instant(date, LocalTime.of(3, 56)), subuhReminder.triggerAt)
        assertEquals(10, result.alarms.size)
    }

    @Test
    fun `passed reminder is skipped while adhan remains`() {
        val result = plan(
            now = instant(date, LocalTime.of(4, 0)),
            settings = settings(preReminderMinutes = 10),
        )

        assertFalse(
            result.alarms.any { it.prayer == Prayer.SUBUH && it.kind == AlarmKind.REMINDER },
        )
        assertTrue(
            result.alarms.any { it.prayer == Prayer.SUBUH && it.kind == AlarmKind.ADHAN },
        )
    }

    @Test
    fun `after isya all alarms move to tomorrow`() {
        val result = plan(now = instant(date, LocalTime.of(20, 0)))

        val adhan = result.alarms.filter { it.kind == AlarmKind.ADHAN }
        assertEquals(5, adhan.size)
        assertTrue(adhan.all { it.triggerAt == instant(date.plusDays(1), times(date.plusDays(1))[it.prayer]!!) })
        assertTrue(adhan.all { it.requestCode >= 100 })
    }

    @Test
    fun `trigger instants follow location timezone`() {
        val jayapura = times(date, "Asia/Jayapura")
        val result = AlarmPlanner.plan(
            settings = settings(),
            today = jayapura,
            tomorrow = times(date.plusDays(1), "Asia/Jayapura"),
            now = instant(date, LocalTime.of(0, 30)),
        )

        val subuh = result.alarms.single {
            it.prayer == Prayer.SUBUH && it.kind == AlarmKind.ADHAN
        }
        assertEquals(instant(date, LocalTime.of(4, 6), "Asia/Jayapura"), subuh.triggerAt)
    }

    @Test
    fun `midnight alarm fires at five past midnight location time`() {
        val early = plan(now = instant(date, LocalTime.of(0, 1)))
        assertEquals(instant(date, LocalTime.of(0, 5)), early.midnightAt)

        val late = plan(now = instant(date, LocalTime.of(20, 0)))
        assertEquals(instant(date.plusDays(1), LocalTime.of(0, 5)), late.midnightAt)
    }

    @Test
    fun `request codes are unique`() {
        val result = plan(
            now = instant(date, LocalTime.of(3, 0)),
            settings = settings(preReminderMinutes = 10),
        )

        val codes = result.alarms.map { it.requestCode }
        assertEquals(codes.size, codes.distinct().size)
        assertEquals(
            AlarmPlanner.allRequestCodes().size,
            AlarmPlanner.allRequestCodes().distinct().size,
        )
    }

    @Test
    fun `no location means no midnight alarm`() {
        val result = plan(
            now = instant(date, LocalTime.of(3, 0)),
            settings = settings(hasLocation = false),
        )

        assertNull(result.midnightAt)
    }
}
