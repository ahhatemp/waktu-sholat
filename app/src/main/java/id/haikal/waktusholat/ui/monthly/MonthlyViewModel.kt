package id.haikal.waktusholat.ui.monthly

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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId

data class MonthlyRow(
    val date: LocalDate,
    val times: Map<Prayer, LocalTime>,
)

data class MonthlyUiState(
    val loading: Boolean = true,
    val hasLocation: Boolean = true,
    val month: YearMonth = YearMonth.now(),
    val today: LocalDate? = null,
    val rows: List<MonthlyRow> = emptyList(),
)

class MonthlyViewModel(
    private val prayerRepository: PrayerRepository,
    settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MonthlyUiState())
    val uiState: StateFlow<MonthlyUiState> = _uiState.asStateFlow()

    private var selectedMonth: YearMonth? = null
    private var latestSettings: Settings? = null
    private var loadJob: Job? = null

    init {
        viewModelScope.launch {
            settingsRepository.settings.collect { settings ->
                latestSettings = settings
                load(settings)
            }
        }
    }

    fun previousMonth() = shiftMonth(-1)

    fun nextMonth() = shiftMonth(1)

    private fun shiftMonth(months: Long) {
        val settings = latestSettings ?: return
        selectedMonth = _uiState.value.month.plusMonths(months)
        load(settings)
    }

    private fun load(settings: Settings) {
        loadJob?.cancel()
        val location = settings.location
        if (location == null) {
            _uiState.value = MonthlyUiState(
                loading = false,
                hasLocation = false,
                month = selectedMonth ?: _uiState.value.month,
            )
            return
        }

        loadJob = viewModelScope.launch {
            val zone = runCatching { ZoneId.of(location.timezoneId) }
                .getOrDefault(ZoneId.systemDefault())
            val today = LocalDate.now(zone)
            val month = selectedMonth ?: YearMonth.from(today)
            _uiState.value = MonthlyUiState(
                loading = true,
                hasLocation = true,
                month = month,
                today = today,
            )
            val rows = withContext(Dispatchers.Default) {
                prayerRepository.getMonth(month.year, month.monthValue).map { times ->
                    MonthlyRow(date = times.date, times = times.times)
                }
            }
            _uiState.value = MonthlyUiState(
                loading = false,
                hasLocation = true,
                month = month,
                today = today,
                rows = rows,
            )
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as WaktuSholatApp
                MonthlyViewModel(
                    prayerRepository = app.container.prayerRepository,
                    settingsRepository = app.container.settingsRepository,
                )
            }
        }
    }
}
