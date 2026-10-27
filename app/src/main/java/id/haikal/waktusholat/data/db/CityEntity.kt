package id.haikal.waktusholat.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import id.haikal.waktusholat.domain.model.City

@Entity(
    tableName = "cities",
    indices = [Index(value = ["name_search"])],
)
data class CityEntity(
    @PrimaryKey val id: Long,
    val name: String,
    @ColumnInfo(name = "name_search") val nameSearch: String,
    val province: String,
    val latitude: Double,
    val longitude: Double,
    @ColumnInfo(name = "timezone_id") val timezoneId: String,
) {
    fun toDomain(): City = City(
        id = id,
        name = name,
        nameSearch = nameSearch,
        province = province,
        latitude = latitude,
        longitude = longitude,
        timezoneId = timezoneId,
    )
}
