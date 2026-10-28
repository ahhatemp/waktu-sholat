package id.haikal.waktusholat.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import id.haikal.waktusholat.WaktuSholatApp
import id.haikal.waktusholat.data.prefs.SettingsRepository
import id.haikal.waktusholat.domain.model.AdhanSound
import id.haikal.waktusholat.domain.model.Prayer
import id.haikal.waktusholat.domain.model.Settings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class NotificationSettingsUiState(
    val loading: Boolean = true,
    val settings: Settings = Settings(),
)

class NotificationSettingsViewModel(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    val uiState: StateFlow<NotificationSettingsUiState> = settingsRepository.settings
        .map { settings -> NotificationSettingsUiState(loading = false, settings = settings) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(SUBSCRIBE_TIMEOUT_MS),
            initialValue = NotificationSettingsUiState(),
        )

    fun setNotify(prayer: Prayer, enabled: Boolean) {
        update { settings ->
            settings.copy(notifications = settings.notifications + (prayer to enabled))
        }
    }

    fun setPreReminder(minutes: Int) {
        if (minutes !in Settings.PRE_REMINDER_OPTIONS) return
        update { settings -> settings.copy(preReminderMinutes = minutes) }
    }

    fun setSound(prayer: Prayer, sound: AdhanSound) {
        update { settings ->
            settings.copy(sounds = settings.sounds + (prayer to sound))
        }
    }

    private fun update(transform: (Settings) -> Settings) {
        viewModelScope.launch { settingsRepository.update(transform) }
    }

    companion object {
        private const val SUBSCRIBE_TIMEOUT_MS = 5_000L

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as WaktuSholatApp
                NotificationSettingsViewModel(app.container.settingsRepository)
            }
        }
    }
}
