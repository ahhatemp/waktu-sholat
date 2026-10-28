package id.haikal.waktusholat.domain.model

import id.haikal.waktusholat.domain.AsrMadhab
import id.haikal.waktusholat.domain.CalculationMethod

data class Settings(
    val location: Location? = null,
    val calculationMethod: CalculationMethod = CalculationMethod.KEMENAG,
    val asrMadhab: AsrMadhab = AsrMadhab.SHAFII,
    val offsets: Map<Prayer, Int> = emptyMap(),
    val ihtiyathEnabled: Boolean = false,
    val notifications: Map<Prayer, Boolean> = emptyMap(),
    val preReminderMinutes: Int = 0,
    val sounds: Map<Prayer, AdhanSound> = emptyMap(),
) {
    fun offsetFor(prayer: Prayer): Int =
        (offsets[prayer] ?: 0).coerceIn(MIN_CORRECTION_MINUTES, MAX_CORRECTION_MINUTES)

    fun notifyFor(prayer: Prayer): Boolean = notifications[prayer] ?: prayer.isObligatory

    fun soundFor(prayer: Prayer): AdhanSound = sounds[prayer] ?: AdhanSound.FULL

    companion object {
        const val MIN_CORRECTION_MINUTES = -10
        const val MAX_CORRECTION_MINUTES = 10

        val PRE_REMINDER_OPTIONS = listOf(0, 5, 10, 15)
    }
}
