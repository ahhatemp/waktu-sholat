package id.haikal.waktusholat.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [CityEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cityDao(): CityDao

    companion object {
        const val NAME = "app.db"
        const val PREPACKAGED_ASSET = "cities.db"
    }
}
