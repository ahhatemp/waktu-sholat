package id.haikal.waktusholat.domain.model

enum class LocationSource { GPS, MANUAL }

data class Location(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val timezoneId: String,
    val source: LocationSource,
)
