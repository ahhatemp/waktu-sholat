package id.haikal.waktusholat.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import id.haikal.waktusholat.WaktuSholatApp
import id.haikal.waktusholat.data.prefs.SettingsRepository
import id.haikal.waktusholat.data.repository.PrayerRepository
import id.haikal.waktusholat.domain.model.Prayer
import id.haikal.waktusholat.domain.model.Settings
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

data class PrayerSlot(
    val prayer: Prayer,
    val time: LocalTime,
)

data class NextPrayerInfo(
    val prayer: Prayer,
    val time: LocalTime,
    val remainingSeconds: Long,
    val isTomorrow: Boolean,
)

data class HomeUiState(
    val loading: Boolean = true,
    val locationName: String? = null,
    val date: LocalDate? = null,
    val slots: List<PrayerSlot> = emptyList(),
    val next: NextPrayerInfo? = null,
    val current: Prayer? = null,
    val now: LocalTime? = null,
    val notifications: Map<Prayer, Boolean> = emptyMap(),
)

class HomeViewModel(
    private val prayerRepository: PrayerRepository,
    settingsRepository: SettingsRepository,
) : ViewModel() {

    private val ticker = flow {
        while (true) {
            emit(Instant.now())
            delay(TICK_MS)
        }
    }

    val uiState: StateFlow<HomeUiState> = combine(
        settingsRepository.settings,
        ticker,
    ) { settings, now -> buildState(settings, now) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(SUBSCRIBE_TIMEOUT_MS),
            initialValue = HomeUiState(),
        )

    private fun buildState(settings: Settings, now: Instant): HomeUiState {
        val location = settings.location ?: return HomeUiState(loading = false)
        val zone = runCatching { ZoneId.of(location.timezoneId) }.getOrDefault(ZoneId.systemDefault())
        val today = now.atZone(zone).toLocalDate()
        val todayTimes = prayerRepository.scheduleFor(today, settings)
            ?: return HomeUiState(loading = false, locationName = location.name, date = today)

        val nextPrayer = todayTimes.nextObligatoryAt(now)
        val next = if (nextPrayer != null) {
            val instant = todayTimes.instantOf(nextPrayer)!!
            NextPrayerInfo(
                prayer = nextPrayer,
                time = todayTimes[nextPrayer]!!,
                remainingSeconds = (instant.epochSecond - now.epochSecond).coerceAtLeast(0),
                isTomorrow = false,
            )
        } else {
            val tomorrowTimes = prayerRepository.scheduleFor(today.plusDays(1), settings)
            val subuhInstant = tomorrowTimes?.instantOf(Prayer.SUBUH)
            if (tomorrowTimes != null && subuhInstant != null) {
                NextPrayerInfo(
                    prayer = Prayer.SUBUH,
                    time = tomorrowTimes[Prayer.SUBUH]!!,
                    remainingSeconds = (subuhInstant.epochSecond - now.epochSecond).coerceAtLeast(0),
                    isTomorrow = true,
                )
            } else {
                null
            }
        }

        return HomeUiState(
            loading = false,
            locationName = location.name,
            date = today,
            slots = todayTimes.ordered().map { (prayer, time) -> PrayerSlot(prayer, time) },
            next = next,
            current = todayTimes.currentObligatoryAt(now),
            now = now.atZone(zone).toLocalTime(),
            notifications = Prayer.entries.associateWith(settings::notifyFor),
        )
    }

    companion object {
        private const val TICK_MS = 1_000L
        private const val SUBSCRIBE_TIMEOUT_MS = 5_000L

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as WaktuSholatApp
                HomeViewModel(
                    prayerRepository = app.container.prayerRepository,
                    settingsRepository = app.container.settingsRepository,
                )
            }
        }
    }
}
