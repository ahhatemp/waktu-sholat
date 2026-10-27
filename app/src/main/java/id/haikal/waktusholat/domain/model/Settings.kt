package id.haikal.waktusholat.domain.model

import id.haikal.waktusholat.domain.AsrMadhab
import id.haikal.waktusholat.domain.CalculationMethod

data class Settings(
    val location: Location? = null,
    val calculationMethod: CalculationMethod = CalculationMethod.KEMENAG,
    val asrMadhab: AsrMadhab = AsrMadhab.SHAFII,
    val offsets: Map<Prayer, Int> = emptyMap(),
    val ihtiyathEnabled: Boolean = false,
) {
    fun offsetFor(prayer: Prayer): Int =
        (offsets[prayer] ?: 0).coerceIn(MIN_CORRECTION_MINUTES, MAX_CORRECTION_MINUTES)

    companion object {
        const val MIN_CORRECTION_MINUTES = -10
        const val MAX_CORRECTION_MINUTES = 10
    }
}
