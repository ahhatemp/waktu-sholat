package id.haikal.waktusholat.domain.model

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

data class PrayerTimes(
    val date: LocalDate,
    val timezoneId: String,
    val times: Map<Prayer, LocalTime>,
) {
    val zoneId: ZoneId
        get() = ZoneId.of(timezoneId)

    operator fun get(prayer: Prayer): LocalTime? = times[prayer]

    fun instantOf(prayer: Prayer): Instant? =
        times[prayer]?.let { date.atTime(it).atZone(zoneId).toInstant() }

    fun ordered(): List<Pair<Prayer, LocalTime>> =
        Prayer.entries.mapNotNull { prayer -> times[prayer]?.let { prayer to it } }

    fun nextObligatoryAt(now: Instant): Prayer? =
        Prayer.entries
            .filter { it.isObligatory }
            .mapNotNull { prayer -> instantOf(prayer)?.let { prayer to it } }
            .firstOrNull { (_, instant) -> instant.isAfter(now) }
            ?.first

    fun currentObligatoryAt(now: Instant): Prayer? =
        Prayer.entries
            .filter { it.isObligatory }
            .mapNotNull { prayer -> instantOf(prayer)?.let { prayer to it } }
            .lastOrNull { (_, instant) -> !instant.isAfter(now) }
            ?.first
}
