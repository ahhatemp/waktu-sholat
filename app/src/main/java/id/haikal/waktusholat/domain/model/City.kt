package id.haikal.waktusholat.domain.model

data class City(
    val id: Long,
    val name: String,
    val nameSearch: String,
    val province: String,
    val latitude: Double,
    val longitude: Double,
    val timezoneId: String,
) {
    fun toLocation(source: LocationSource): Location = Location(
        name = name,
        latitude = latitude,
        longitude = longitude,
        timezoneId = timezoneId,
        source = source,
    )
}
