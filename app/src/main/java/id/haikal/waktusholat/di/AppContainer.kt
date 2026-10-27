package id.haikal.waktusholat.di

import android.content.Context
import androidx.room.Room
import id.haikal.waktusholat.data.db.AppDatabase
import id.haikal.waktusholat.data.prefs.SettingsRepository
import id.haikal.waktusholat.data.prefs.settingsDataStore
import id.haikal.waktusholat.data.repository.LocationRepository
import id.haikal.waktusholat.data.repository.PrayerRepository

class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    private val database: AppDatabase by lazy {
        Room.databaseBuilder(appContext, AppDatabase::class.java, AppDatabase.NAME)
            .createFromAsset(AppDatabase.PREPACKAGED_ASSET)
            .build()
    }

    val settingsRepository: SettingsRepository by lazy {
        SettingsRepository(appContext.settingsDataStore)
    }

    val locationRepository: LocationRepository by lazy {
        LocationRepository(appContext, database.cityDao(), settingsRepository)
    }

    val prayerRepository: PrayerRepository by lazy {
        PrayerRepository(settingsRepository)
    }
}
