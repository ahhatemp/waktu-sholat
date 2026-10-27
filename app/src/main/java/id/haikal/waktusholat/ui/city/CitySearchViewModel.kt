package id.haikal.waktusholat.ui.city

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import id.haikal.waktusholat.WaktuSholatApp
import id.haikal.waktusholat.data.repository.LocationFailure
import id.haikal.waktusholat.data.repository.LocationRepository
import id.haikal.waktusholat.domain.model.City
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CitySearchUiState(
    val query: String = "",
    val results: List<City> = emptyList(),
    val searching: Boolean = false,
    val detecting: Boolean = false,
    val error: LocationFailure? = null,
    val saved: Boolean = false,
)

class CitySearchViewModel(
    private val locationRepository: LocationRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CitySearchUiState())
    val uiState: StateFlow<CitySearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query, error = null) }
        searchJob?.cancel()
        val trimmed = query.trim()
        if (trimmed.isEmpty()) {
            _uiState.update { it.copy(results = emptyList(), searching = false) }
            return
        }
        searchJob = viewModelScope.launch {
            _uiState.update { it.copy(searching = true) }
            delay(SEARCH_DEBOUNCE_MS)
            val results = locationRepository.searchCity(trimmed)
            _uiState.update { it.copy(results = results, searching = false) }
        }
    }

    fun selectCity(city: City) {
        if (_uiState.value.saved) return
        viewModelScope.launch {
            locationRepository.saveCity(city)
            _uiState.update { it.copy(saved = true, error = null) }
        }
    }

    fun useCurrentLocation() {
        if (_uiState.value.detecting || _uiState.value.saved) return
        viewModelScope.launch {
            _uiState.update { it.copy(detecting = true, error = null) }
            locationRepository.detectOnce()
                .onSuccess { location ->
                    locationRepository.saveLocation(location)
                    _uiState.update { it.copy(detecting = false, saved = true) }
                }
                .onFailure { failure ->
                    _uiState.update {
                        it.copy(
                            detecting = false,
                            error = failure as? LocationFailure ?: LocationFailure.Unavailable,
                        )
                    }
                }
        }
    }

    fun onPermissionDenied() {
        _uiState.update { it.copy(error = LocationFailure.PermissionDenied) }
    }

    companion object {
        private const val SEARCH_DEBOUNCE_MS = 250L

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as WaktuSholatApp
                CitySearchViewModel(locationRepository = app.container.locationRepository)
            }
        }
    }
}
