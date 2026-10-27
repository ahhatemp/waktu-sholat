package id.haikal.waktusholat.domain

/**
 * Parameter sudut matahari mengikuti konvensi masing-masing otoritas.
 * Kemenag RI memakai Subuh 20 derajat, Isya 18 derajat, dan ihtiyath 2 menit
 * sehingga hasil default sudah sepadan dengan jadwal resmi tanpa setting tambahan.
 */
enum class CalculationMethod(
    val id: String,
    val fajrAngle: Double,
    val ishaAngle: Double?,
    val ihtiyathMinutes: Int,
    val ishaIntervalMinutes: Int? = null,
) {
    KEMENAG("kemenag", fajrAngle = 20.0, ishaAngle = 18.0, ihtiyathMinutes = 2),
    MUSLIM_WORLD_LEAGUE("mwl", fajrAngle = 18.0, ishaAngle = 17.0, ihtiyathMinutes = 0),
    UMM_AL_QURA("umm_al_qura", fajrAngle = 18.5, ishaAngle = null, ihtiyathMinutes = 0, ishaIntervalMinutes = 90),
    EGYPTIAN("egyptian", fajrAngle = 19.5, ishaAngle = 17.5, ihtiyathMinutes = 0),
    KARACHI("karachi", fajrAngle = 18.0, ishaAngle = 18.0, ihtiyathMinutes = 0),
    ISNA("isna", fajrAngle = 15.0, ishaAngle = 15.0, ihtiyathMinutes = 0),
    ;

    companion object {
        fun fromId(id: String?): CalculationMethod =
            entries.firstOrNull { it.id == id } ?: KEMENAG
    }
}

enum class AsrMadhab(val id: String, val shadowFactor: Double) {
    SHAFII("shafii", 1.0),
    HANAFI("hanafi", 2.0),
    ;

    companion object {
        fun fromId(id: String?): AsrMadhab =
            entries.firstOrNull { it.id == id } ?: SHAFII
    }
}
