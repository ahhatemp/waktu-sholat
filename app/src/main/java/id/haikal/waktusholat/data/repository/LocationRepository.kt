package id.haikal.waktusholat.data.repository

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location as AndroidLocation
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import androidx.core.content.ContextCompat
import id.haikal.waktusholat.data.db.CityDao
import id.haikal.waktusholat.data.prefs.SettingsRepository
import id.haikal.waktusholat.domain.model.City
import id.haikal.waktusholat.domain.model.Location
import id.haikal.waktusholat.domain.model.LocationSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import java.util.TimeZone
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

sealed class LocationFailure(message: String) : Exception(message) {
    data object PermissionDenied : LocationFailure("Izin lokasi ditolak")
    data object LocationDisabled : LocationFailure("Lokasi perangkat tidak aktif")
    data object Timeout : LocationFailure("Lokasi tidak ditemukan")
    data object Unavailable : LocationFailure("Lokasi tidak tersedia")
}

class LocationRepository(
    private val context: Context,
    private val cityDao: CityDao,
    private val settingsRepository: SettingsRepository,
) {

    suspend fun detectOnce(): Result<Location> {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return Result.failure(LocationFailure.PermissionDenied)
        }

        val manager = context.getSystemService(LocationManager::class.java)
            ?: return Result.failure(LocationFailure.Unavailable)
        val providers = listOf(LocationManager.NETWORK_PROVIDER, LocationManager.GPS_PROVIDER)
            .filter { provider -> runCatching { manager.isProviderEnabled(provider) }.getOrDefault(false) }
        if (providers.isEmpty()) {
            return Result.failure(LocationFailure.LocationDisabled)
        }

        val fix = try {
            val lastKnown = providers.firstNotNullOfOrNull { provider ->
                runCatching { manager.getLastKnownLocation(provider) }.getOrNull()
            }
            lastKnown ?: withTimeoutOrNull(TIMEOUT_MS) { awaitFix(manager, providers) }
        } catch (error: SecurityException) {
            return Result.failure(LocationFailure.PermissionDenied)
        } ?: return Result.failure(LocationFailure.Timeout)

        return Result.success(matchNearestCity(fix.latitude, fix.longitude))
    }

    suspend fun searchCity(query: String): List<City> {
        val normalized = normalizeQuery(query)
        if (normalized.isEmpty()) return emptyList()
        return cityDao.searchByPrefix(normalized).map { it.toDomain() }
    }

    suspend fun saveCity(city: City) {
        settingsRepository.update { settings ->
            settings.copy(location = city.toLocation(LocationSource.MANUAL))
        }
    }

    suspend fun saveLocation(location: Location) {
        settingsRepository.update { settings -> settings.copy(location = location) }
    }

    private suspend fun matchNearestCity(latitude: Double, longitude: Double): Location {
        val nearest = runCatching { cityDao.getAll() }.getOrNull()
            ?.minByOrNull { entity -> distanceKm(latitude, longitude, entity.latitude, entity.longitude) }
            ?.toDomain()

        return nearest?.toLocation(LocationSource.GPS) ?: Location(
            name = "Lokasi saya",
            latitude = latitude,
            longitude = longitude,
            timezoneId = TimeZone.getDefault().id,
            source = LocationSource.GPS,
        )
    }

    @SuppressLint("MissingPermission")
    private suspend fun awaitFix(
        manager: LocationManager,
        providers: List<String>,
    ): AndroidLocation? = suspendCancellableCoroutine { continuation ->
        val listeners = mutableListOf<Pair<String, LocationListener>>()

        fun cleanup() {
            listeners.forEach { (_, listener) -> runCatching { manager.removeUpdates(listener) } }
            listeners.clear()
        }

        fun deliver(location: AndroidLocation?) {
            if (!continuation.isActive) return
            cleanup()
            continuation.resume(location)
        }

        try {
            providers.forEach { provider ->
                val listener = object : LocationListener {
                    override fun onLocationChanged(location: AndroidLocation) = deliver(location)

                    @Deprecated("Deprecated in Java")
                    override fun onStatusChanged(provider: String?, status: Int, extras: android.os.Bundle?) = Unit

                    override fun onProviderEnabled(provider: String) = Unit

                    override fun onProviderDisabled(provider: String) = Unit
                }
                listeners += provider to listener
                manager.requestLocationUpdates(provider, 0L, 0f, listener, Looper.getMainLooper())
            }
        } catch (error: SecurityException) {
            cleanup()
            continuation.resumeWithException(error)
        }

        continuation.invokeOnCancellation { cleanup() }
    }

    private fun normalizeQuery(query: String): String =
        query.trim().lowercase(Locale.ROOT)
            .removePrefix("kabupaten ")
            .removePrefix("kota ")
            .removePrefix("kab. ")
            .removePrefix("kab ")
            .replace(Regex("\\s+"), " ")
            .trim()

    private fun distanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2) * sin(dLon / 2)
        return 2 * EARTH_RADIUS_KM * asin(sqrt(a.coerceIn(0.0, 1.0)))
    }

    private companion object {
        const val TIMEOUT_MS = 15_000L
        const val EARTH_RADIUS_KM = 6371.0
    }
}
