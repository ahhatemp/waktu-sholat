package id.haikal.waktusholat.domain

import id.haikal.waktusholat.domain.model.Location
import id.haikal.waktusholat.domain.model.LocationSource
import id.haikal.waktusholat.domain.model.Prayer
import id.haikal.waktusholat.domain.model.Settings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import kotlin.math.abs

class PrayerTimeEngineTest {

    private val engine = PrayerTimeEngine()

    @Test
    fun `matches Kemenag reference within two minutes`() {
        val failures = mutableListOf<String>()
        var maxDifference = 0
        var comparisons = 0

        loadReference().forEach { row ->
            val times = engine.calculate(
                date = LocalDate.parse(row.date),
                location = row.toLocation(),
                settings = Settings(),
            )
            Prayer.entries.forEach { prayer ->
                val expected = row.times.getValue(prayer)
                val actual = times[prayer]
                if (actual == null) {
                    failures += "${row.city} ${row.date} $prayer: tidak dihitung"
                    return@forEach
                }
                val difference = abs(minutesOf(expected) - minutesOf(actual))
                maxDifference = maxOf(maxDifference, difference)
                comparisons++
                if (difference > TOLERANCE_MINUTES) {
                    failures += "${row.city} ${row.date} $prayer: acuan $expected, hasil $actual ($difference menit)"
                }
            }
        }

        assertTrue("Tidak ada data acuan", comparisons > 0)
        assertTrue(failures.joinToString("\n"), failures.isEmpty())
        assertTrue("Selisih maksimum $maxDifference menit", maxDifference <= TOLERANCE_MINUTES)
    }

    @Test
    fun `enabling ihtiyath shifts every time by two minutes in the safe direction`() {
        val date = LocalDate.of(2026, 7, 1)
        val location = Location("Makassar", -5.13333, 119.46667, "Asia/Makassar", LocationSource.MANUAL)
        val base = engine.calculate(date, location, Settings())
        val extra = engine.calculate(date, location, Settings(ihtiyathEnabled = true))

        Prayer.entries.forEach { prayer ->
            val expectedShift = if (prayer == Prayer.TERBIT) -2 else 2
            assertEquals(
                "$prayer",
                expectedShift,
                minutesOf(extra[prayer]!!) - minutesOf(base[prayer]!!),
            )
        }
    }

    @Test
    fun `minute corrections are applied after ihtiyath`() {
        val date = LocalDate.of(2026, 1, 1)
        val location = Location("Yogyakarta", -7.8, 110.375, "Asia/Jakarta", LocationSource.MANUAL)
        val base = engine.calculate(date, location, Settings())
        val corrected = engine.calculate(
            date,
            location,
            Settings(offsets = Prayer.entries.associateWith { 5 }),
        )

        Prayer.entries.forEach { prayer ->
            assertEquals("$prayer", 5, minutesOf(corrected[prayer]!!) - minutesOf(base[prayer]!!))
        }
    }

    @Test
    fun `minute corrections are clamped to minus ten through plus ten`() {
        val date = LocalDate.of(2026, 1, 1)
        val location = Location("Medan", 3.59333, 98.66944, "Asia/Jakarta", LocationSource.MANUAL)
        val upper = engine.calculate(date, location, Settings(offsets = mapOf(Prayer.SUBUH to 99)))
        val clampedUpper = engine.calculate(date, location, Settings(offsets = mapOf(Prayer.SUBUH to 10)))
        val lower = engine.calculate(date, location, Settings(offsets = mapOf(Prayer.SUBUH to -99)))
        val clampedLower = engine.calculate(date, location, Settings(offsets = mapOf(Prayer.SUBUH to -10)))

        assertEquals(upper[Prayer.SUBUH], clampedUpper[Prayer.SUBUH])
        assertEquals(lower[Prayer.SUBUH], clampedLower[Prayer.SUBUH])
    }

    @Test
    fun `Hanafi asr is later than Shafii asr`() {
        val date = LocalDate.of(2026, 3, 15)
        val location = Location("Jakarta", -6.2, 106.8167, "Asia/Jakarta", LocationSource.MANUAL)
        val shafii = engine.calculate(date, location, Settings(asrMadhab = AsrMadhab.SHAFII))
        val hanafi = engine.calculate(date, location, Settings(asrMadhab = AsrMadhab.HANAFI))

        assertTrue(minutesOf(hanafi[Prayer.ASHAR]!!) > minutesOf(shafii[Prayer.ASHAR]!!))
    }

    @Test
    fun `wall clock times follow the location timezone`() {
        val date = LocalDate.of(2026, 5, 20)
        val jakarta = engine.calculate(
            date,
            Location("Jakarta", -6.2, 106.8167, "Asia/Jakarta", LocationSource.MANUAL),
            Settings(),
        )
        val jayapura = engine.calculate(
            date,
            Location("Jayapura", -6.2, 106.8167, "Asia/Jayapura", LocationSource.MANUAL),
            Settings(),
        )

        Prayer.entries.forEach { prayer ->
            assertEquals("$prayer", 120, minutesOf(jayapura[prayer]!!) - minutesOf(jakarta[prayer]!!))
        }
    }

    @Test
    fun `polar summer omits fajr and isha without failing`() {
        val times = engine.calculate(
            LocalDate.of(2026, 6, 21),
            Location("Reykjavik", 64.1466, -21.9426, "Atlantic/Reykjavik", LocationSource.MANUAL),
            Settings(),
        )

        assertNull(times[Prayer.SUBUH])
        assertNull(times[Prayer.ISYA])
        assertNotNull(times[Prayer.DZUHUR])
        assertNotNull(times[Prayer.TERBIT])
        assertNotNull(times[Prayer.MAGHRIB])
    }

    @Test
    fun `next obligatory prayer rolls over at the end of the day`() {
        val times = engine.calculate(
            LocalDate.of(2026, 12, 31),
            Location("Jakarta", -6.2, 106.8167, "Asia/Jakarta", LocationSource.MANUAL),
            Settings(),
        )

        val subuh = times.instantOf(Prayer.SUBUH)!!
        assertEquals(Prayer.SUBUH, times.nextObligatoryAt(subuh.minusSeconds(60)))
        assertNull(times.nextObligatoryAt(times.instantOf(Prayer.ISYA)!!.plusSeconds(60)))
    }

    @Test
    fun `a full month is calculated well under 200 ms`() {
        val location = Location("Jakarta", -6.2, 106.8167, "Asia/Jakarta", LocationSource.MANUAL)
        repeat(3) { day ->
            engine.calculate(LocalDate.of(2026, 1, 1).plusDays(day.toLong()), location, Settings())
        }

        val start = System.nanoTime()
        (0..30).forEach { offset ->
            engine.calculate(LocalDate.of(2026, 1, 1).plusDays(offset.toLong()), location, Settings())
        }
        val elapsedMs = (System.nanoTime() - start) / 1_000_000

        assertTrue("Perhitungan sebulan memakan $elapsedMs ms", elapsedMs < 200)
    }

    private data class ReferenceRow(
        val city: String,
        val latitude: Double,
        val longitude: Double,
        val timezone: String,
        val date: String,
        val times: Map<Prayer, LocalTime>,
    ) {
        fun toLocation() = Location(city, latitude, longitude, timezone, LocationSource.MANUAL)
    }

    private fun loadReference(): List<ReferenceRow> {
        val stream = javaClass.getResourceAsStream("/kemenag_reference.csv")
            ?: error("kemenag_reference.csv tidak ditemukan")
        return stream.bufferedReader().useLines { lines ->
            lines.filter { it.isNotBlank() && !it.startsWith("#") }
                .mapNotNull { line ->
                    val parts = line.split(",")
                    if (parts.size < 12) return@mapNotNull null
                    val times = listOf(
                        Prayer.IMSAK to parts[5],
                        Prayer.SUBUH to parts[6],
                        Prayer.TERBIT to parts[7],
                        Prayer.DZUHUR to parts[8],
                        Prayer.ASHAR to parts[9],
                        Prayer.MAGHRIB to parts[10],
                        Prayer.ISYA to parts[11],
                    ).associate { (prayer, value) -> prayer to LocalTime.parse(value) }
                    ReferenceRow(
                        city = parts[0],
                        latitude = parts[1].toDouble(),
                        longitude = parts[2].toDouble(),
                        timezone = parts[3],
                        date = parts[4],
                        times = times,
                    )
                }
                .toList()
        }
    }

    private fun minutesOf(time: LocalTime): Int = time.hour * 60 + time.minute

    private companion object {
        const val TOLERANCE_MINUTES = 2
    }
}
