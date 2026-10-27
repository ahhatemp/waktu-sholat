package id.haikal.waktusholat.data.db

import androidx.room.Dao
import androidx.room.Query

@Dao
interface CityDao {

    @Query("SELECT * FROM cities WHERE name_search LIKE :prefix || '%' ORDER BY name COLLATE NOCASE LIMIT 50")
    suspend fun searchByPrefix(prefix: String): List<CityEntity>

    @Query("SELECT * FROM cities ORDER BY name COLLATE NOCASE")
    suspend fun getAll(): List<CityEntity>

    @Query("SELECT * FROM cities WHERE id = :id")
    suspend fun getById(id: Long): CityEntity?
}
