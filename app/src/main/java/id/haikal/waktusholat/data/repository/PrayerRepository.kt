package id.haikal.waktusholat.data.repository

import id.haikal.waktusholat.data.prefs.SettingsRepository
import id.haikal.waktusholat.domain.PrayerTimeEngine
import id.haikal.waktusholat.domain.model.Prayer
import id.haikal.waktusholat.domain.model.PrayerTimes
import id.haikal.waktusholat.domain.model.Settings
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

class PrayerRepository(
    private val settingsRepository: SettingsRepository,
    private val engine: PrayerTimeEngine = PrayerTimeEngine(),
) {

    fun scheduleFor(date: LocalDate, settings: Settings): PrayerTimes? {
        val location = settings.location ?: return null
        return engine.calculate(date, location, settings)
    }

    suspend fun getDay(date: LocalDate): PrayerTimes? =
        scheduleFor(date, settingsRepository.current())

    suspend fun getMonth(year: Int, month: Int): List<PrayerTimes> {
        val settings = settingsRepository.current()
        val location = settings.location ?: return emptyList()
        val monthLength = YearMonth.of(year, month).lengthOfMonth()
        return (1..monthLength).map { day ->
            engine.calculate(LocalDate.of(year, month, day), location, settings)
        }
    }

    suspend fun getNext(now: Instant = Instant.now()): NextPrayer? {
        val settings = settingsRepository.current()
        val location = settings.location ?: return null
        val zone = ZoneId.of(location.timezoneId)
        val today = now.atZone(zone).toLocalDate()

        val todayTimes = engine.calculate(today, location, settings)
        val nextPrayer = todayTimes.nextObligatoryAt(now)
        if (nextPrayer != null) {
            return NextPrayer(nextPrayer, todayTimes.instantOf(nextPrayer)!!, todayTimes)
        }

        val tomorrowTimes = engine.calculate(today.plusDays(1), location, settings)
        val subuh = tomorrowTimes.instantOf(Prayer.SUBUH) ?: return null
        return NextPrayer(Prayer.SUBUH, subuh, tomorrowTimes)
    }

    data class NextPrayer(
        val prayer: Prayer,
        val instant: Instant,
        val times: PrayerTimes,
    )
}
