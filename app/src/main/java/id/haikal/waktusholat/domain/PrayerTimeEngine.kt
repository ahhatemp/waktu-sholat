package id.haikal.waktusholat.domain

import id.haikal.waktusholat.domain.model.Location
import id.haikal.waktusholat.domain.model.Prayer
import id.haikal.waktusholat.domain.model.PrayerTimes
import id.haikal.waktusholat.domain.model.Settings
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.tan

/**
 * Perhitungan waktu sholat astronomis murni Kotlin/JVM, terpisah dari Android
 * agar bisa diuji terhadap jadwal Kemenag.
 *
 * Konvensi pembulatan mengikuti Kemenag: detik dibulatkan ke atas setelah
 * ihtiyath untuk semua waktu kecuali Terbit, yang dibulatkan ke bawah agar
 * matahari sudah terbit saat waktu tersebut tiba.
 */
class PrayerTimeEngine {

    fun calculate(date: LocalDate, location: Location, settings: Settings): PrayerTimes {
        val zone = runCatching { ZoneId.of(location.timezoneId) }.getOrDefault(ZoneId.systemDefault())
        val offsetMinutes = date.atTime(12, 0).atZone(zone).offset.totalSeconds / 60.0
        val method = settings.calculationMethod
        val ihtiyath = method.ihtiyathMinutes +
            if (settings.ihtiyathEnabled) EXTRA_IHTIYATH_MINUTES else 0

        val latitude = location.latitude
        val longitude = location.longitude
        val shadowFactor = settings.asrMadhab.shadowFactor

        val rawFajr = sunEventMinutes(date, latitude, longitude, offsetMinutes, -method.fajrAngle, morning = true)
        val rawSunrise = sunEventMinutes(date, latitude, longitude, offsetMinutes, SUN_ALTITUDE, morning = true)
        val rawDhuhr = transitMinutes(date, longitude, offsetMinutes)
        val rawAsr = asrMinutes(date, latitude, longitude, offsetMinutes, shadowFactor)
        val rawMaghrib = sunEventMinutes(date, latitude, longitude, offsetMinutes, SUN_ALTITUDE, morning = false)
        val rawIsha = method.ishaIntervalMinutes
            ?.let { interval -> rawMaghrib?.plus(interval) }
            ?: method.ishaAngle?.let { angle ->
                sunEventMinutes(date, latitude, longitude, offsetMinutes, -angle, morning = false)
            }

        val adjusted = mutableMapOf<Prayer, Int>()
        rawFajr?.let { adjusted[Prayer.SUBUH] = ceilToMinute(it + ihtiyath) }
        rawSunrise?.let { adjusted[Prayer.TERBIT] = floorToMinute(it - ihtiyath) }
        rawDhuhr?.let { adjusted[Prayer.DZUHUR] = ceilToMinute(it + ihtiyath) }
        rawAsr?.let { adjusted[Prayer.ASHAR] = ceilToMinute(it + ihtiyath) }
        rawMaghrib?.let { adjusted[Prayer.MAGHRIB] = ceilToMinute(it + ihtiyath) }
        rawIsha?.let { adjusted[Prayer.ISYA] = ceilToMinute(it + ihtiyath) }
        adjusted[Prayer.SUBUH]?.let { subuh ->
            adjusted[Prayer.IMSAK] = subuh - IMSAK_MINUTES_BEFORE_SUBUH
        }

        val times = Prayer.entries.mapNotNull { prayer ->
            val minutes = adjusted[prayer] ?: return@mapNotNull null
            prayer to toLocalTime(minutes + settings.offsetFor(prayer))
        }.toMap()

        return PrayerTimes(
            date = date,
            timezoneId = location.timezoneId,
            times = times,
        )
    }

    private fun transitMinutes(date: LocalDate, longitude: Double, offsetMinutes: Double): Double {
        val julianDay = julianDay(date, minutesUtc = 12 * 60.0)
        val sun = sunPosition(julianDay)
        val noonUtc = 720.0 - 4.0 * longitude - sun.equationOfTimeMinutes
        return noonUtc + offsetMinutes
    }

    private fun sunEventMinutes(
        date: LocalDate,
        latitude: Double,
        longitude: Double,
        offsetMinutes: Double,
        altitude: Double,
        morning: Boolean,
    ): Double? {
        val direction = if (morning) -1.0 else 1.0
        var estimateUtc = 0.0
        var sun = sunPosition(julianDay(date, 12 * 60.0))
        repeat(3) {
            val noonUtc = 720.0 - 4.0 * longitude - sun.equationOfTimeMinutes
            val hourAngle = hourAngleDegrees(altitude, latitude, sun.declination) ?: return null
            estimateUtc = noonUtc + direction * hourAngle / 15.0 * 60.0
            sun = sunPosition(julianDay(date, estimateUtc))
        }
        val noonUtc = 720.0 - 4.0 * longitude - sun.equationOfTimeMinutes
        val hourAngle = hourAngleDegrees(altitude, latitude, sun.declination) ?: return null
        return noonUtc + direction * hourAngle / 15.0 * 60.0 + offsetMinutes
    }

    private fun asrMinutes(
        date: LocalDate,
        latitude: Double,
        longitude: Double,
        offsetMinutes: Double,
        shadowFactor: Double,
    ): Double? {
        var estimateUtc = 0.0
        var sun = sunPosition(julianDay(date, 12 * 60.0))
        repeat(3) {
            val noonUtc = 720.0 - 4.0 * longitude - sun.equationOfTimeMinutes
            val altitude = asrAltitudeDegrees(latitude, sun.declination, shadowFactor)
            val hourAngle = hourAngleDegrees(altitude, latitude, sun.declination) ?: return null
            estimateUtc = noonUtc + hourAngle / 15.0 * 60.0
            sun = sunPosition(julianDay(date, estimateUtc))
        }
        val noonUtc = 720.0 - 4.0 * longitude - sun.equationOfTimeMinutes
        val altitude = asrAltitudeDegrees(latitude, sun.declination, shadowFactor)
        val hourAngle = hourAngleDegrees(altitude, latitude, sun.declination) ?: return null
        return noonUtc + hourAngle / 15.0 * 60.0 + offsetMinutes
    }

    private fun hourAngleDegrees(altitude: Double, latitude: Double, declination: Double): Double? {
        val denominator = cosDeg(latitude) * cosDeg(declination)
        if (abs(denominator) < 1e-9) return null
        val cosHourAngle = (sinDeg(altitude) - sinDeg(latitude) * sinDeg(declination)) / denominator
        if (cosHourAngle < -1.0 || cosHourAngle > 1.0) return null
        return acosDeg(cosHourAngle)
    }

    private fun asrAltitudeDegrees(latitude: Double, declination: Double, shadowFactor: Double): Double =
        atanDeg(1.0 / (shadowFactor + tanDeg(abs(latitude - declination))))

    private fun sunPosition(julianDay: Double): SunPosition {
        val t = (julianDay - JULIAN_DAY_J2000) / 36525.0
        val meanLongitude = (280.46646 + t * (36000.76983 + t * 0.0003032)) % 360.0
        val meanAnomaly = 357.52911 + t * (35999.05029 - 0.0001537 * t)
        val eccentricity = 0.016708634 - t * (0.000042037 + 0.0000001267 * t)
        val equationOfCenter = sinDeg(meanAnomaly) *
            (1.914602 - t * (0.004817 + 0.000014 * t)) +
            sinDeg(2 * meanAnomaly) * (0.019993 - 0.000101 * t) +
            sinDeg(3 * meanAnomaly) * 0.000289
        val trueLongitude = meanLongitude + equationOfCenter
        val omega = 125.04 - 1934.136 * t
        val apparentLongitude = trueLongitude - 0.00569 - 0.00478 * sinDeg(omega)
        val meanObliquity = 23.0 +
            (26.0 + (21.448 - t * (46.815 + t * (0.00059 - t * 0.001813))) / 60.0) / 60.0
        val obliquity = meanObliquity + 0.00256 * cosDeg(omega)
        val declination = asinDeg(sinDeg(obliquity) * sinDeg(apparentLongitude))
        val y = tanDeg(obliquity / 2.0).let { it * it }
        val equationOfTime = 4.0 * RAD_TO_DEG * (
            y * sinDeg(2 * meanLongitude) -
                2 * eccentricity * sinDeg(meanAnomaly) +
                4 * eccentricity * y * sinDeg(meanAnomaly) * cosDeg(2 * meanLongitude) -
                0.5 * y * y * sinDeg(4 * meanLongitude) -
                1.25 * eccentricity * eccentricity * sinDeg(2 * meanAnomaly)
            )
        return SunPosition(declination, equationOfTime)
    }

    private fun julianDay(date: LocalDate, minutesUtc: Double): Double =
        date.toEpochDay() + JULIAN_DAY_UNIX_EPOCH + minutesUtc / 1440.0

    private fun ceilToMinute(minutes: Double): Int = ceil(minutes - EPSILON).toInt()

    private fun floorToMinute(minutes: Double): Int = floor(minutes + EPSILON).toInt()

    private fun toLocalTime(minutes: Int): LocalTime =
        LocalTime.of((Math.floorMod(minutes, MINUTES_PER_DAY)) / 60, Math.floorMod(minutes, 60))

    private fun sinDeg(degrees: Double): Double = sin(Math.toRadians(degrees))
    private fun cosDeg(degrees: Double): Double = cos(Math.toRadians(degrees))
    private fun tanDeg(degrees: Double): Double = tan(Math.toRadians(degrees))
    private fun asinDeg(value: Double): Double = Math.toDegrees(asin(value))
    private fun acosDeg(value: Double): Double = Math.toDegrees(acos(value))
    private fun atanDeg(value: Double): Double = Math.toDegrees(atan(value))

    private data class SunPosition(
        val declination: Double,
        val equationOfTimeMinutes: Double,
    )

    private companion object {
        const val JULIAN_DAY_J2000 = 2451545.0
        const val JULIAN_DAY_UNIX_EPOCH = 2440587.5
        const val RAD_TO_DEG = 57.29577951308232
        const val SUN_ALTITUDE = -0.833
        const val IMSAK_MINUTES_BEFORE_SUBUH = 10
        const val EXTRA_IHTIYATH_MINUTES = 2
        const val MINUTES_PER_DAY = 24 * 60
        const val EPSILON = 1e-9
    }
}
