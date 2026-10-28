package id.haikal.waktusholat.alarm

import id.haikal.waktusholat.data.prefs.SettingsRepository
import kotlinx.coroutines.flow.drop

class AlarmSyncCoordinator(
    private val alarmScheduler: AlarmScheduler,
    private val settingsRepository: SettingsRepository,
) {

    suspend fun syncOnOpen() {
        if (settingsRepository.current().location != null) {
            alarmScheduler.scheduleAll()
        }
    }

    suspend fun observeChanges() {
        settingsRepository.settings.drop(1).collect { settings ->
            if (settings.location == null) {
                alarmScheduler.cancelAll()
            } else {
                alarmScheduler.scheduleAll()
            }
        }
    }
}
