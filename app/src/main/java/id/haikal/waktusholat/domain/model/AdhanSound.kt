package id.haikal.waktusholat.domain.model

enum class AdhanSound(val id: String) {
    FULL("full"),
    SHORT("short"),
    DEFAULT("default"),
    SILENT("silent"),
    ;

    companion object {
        fun fromId(id: String?): AdhanSound =
            entries.firstOrNull { it.id == id } ?: FULL
    }
}
