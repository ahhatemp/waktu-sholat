package id.haikal.waktusholat.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import id.haikal.waktusholat.domain.AsrMadhab
import id.haikal.waktusholat.domain.CalculationMethod
import id.haikal.waktusholat.domain.model.Location
import id.haikal.waktusholat.domain.model.LocationSource
import id.haikal.waktusholat.domain.model.Prayer
import id.haikal.waktusholat.domain.model.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(
    private val dataStore: DataStore<Preferences>,
) {
    val settings: Flow<Settings> = dataStore.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map(::toSettings)

    suspend fun current(): Settings = settings.first()

    suspend fun update(transform: (Settings) -> Settings) {
        dataStore.edit { preferences ->
            writeSettings(preferences, transform(toSettings(preferences)))
        }
    }

    private fun toSettings(preferences: Preferences): Settings {
        val location = readLocation(preferences)
        val method = CalculationMethod.fromId(preferences[Keys.CALC_METHOD])
        val madhab = AsrMadhab.fromId(preferences[Keys.ASR_MADHAB])
        val offsets = Prayer.entries.mapNotNull { prayer ->
            preferences[Keys.offset(prayer)]?.let { value ->
                prayer to value.coerceIn(Settings.MIN_CORRECTION_MINUTES, Settings.MAX_CORRECTION_MINUTES)
            }
        }.toMap()
        return Settings(
            location = location,
            calculationMethod = method,
            asrMadhab = madhab,
            offsets = offsets,
            ihtiyathEnabled = preferences[Keys.IHTIYATH_ENABLED] ?: false,
        )
    }

    private fun readLocation(preferences: Preferences): Location? {
        val latitude = preferences[Keys.LOCATION_LAT] ?: return null
        val longitude = preferences[Keys.LOCATION_LNG] ?: return null
        val name = preferences[Keys.LOCATION_NAME] ?: return null
        val timezone = preferences[Keys.LOCATION_TIMEZONE] ?: return null
        val source = when (preferences[Keys.LOCATION_SOURCE]) {
            LocationSource.GPS.name -> LocationSource.GPS
            else -> LocationSource.MANUAL
        }
        return Location(
            name = name,
            latitude = latitude,
            longitude = longitude,
            timezoneId = timezone,
            source = source,
        )
    }

    private fun writeSettings(preferences: MutablePreferences, settings: Settings) {
        val location = settings.location
        if (location == null) {
            preferences.remove(Keys.LOCATION_LAT)
            preferences.remove(Keys.LOCATION_LNG)
            preferences.remove(Keys.LOCATION_NAME)
            preferences.remove(Keys.LOCATION_TIMEZONE)
            preferences.remove(Keys.LOCATION_SOURCE)
        } else {
            preferences[Keys.LOCATION_LAT] = location.latitude
            preferences[Keys.LOCATION_LNG] = location.longitude
            preferences[Keys.LOCATION_NAME] = location.name
            preferences[Keys.LOCATION_TIMEZONE] = location.timezoneId
            preferences[Keys.LOCATION_SOURCE] = location.source.name
        }

        preferences[Keys.CALC_METHOD] = settings.calculationMethod.id
        preferences[Keys.ASR_MADHAB] = settings.asrMadhab.id
        Prayer.entries.forEach { prayer ->
            preferences[Keys.offset(prayer)] = settings.offsetFor(prayer)
        }
        preferences[Keys.IHTIYATH_ENABLED] = settings.ihtiyathEnabled
    }

    private object Keys {
        val LOCATION_LAT = doublePreferencesKey("location_lat")
        val LOCATION_LNG = doublePreferencesKey("location_lng")
        val LOCATION_NAME = stringPreferencesKey("location_name")
        val LOCATION_TIMEZONE = stringPreferencesKey("location_timezone")
        val LOCATION_SOURCE = stringPreferencesKey("location_source")
        val CALC_METHOD = stringPreferencesKey("calc_method")
        val ASR_MADHAB = stringPreferencesKey("asr_madhab")
        val IHTIYATH_ENABLED = booleanPreferencesKey("ihtiyath_enabled")

        fun offset(prayer: Prayer) = intPreferencesKey("offset_${prayer.key}")
    }
}
