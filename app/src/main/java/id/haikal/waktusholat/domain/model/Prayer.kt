package id.haikal.waktusholat.domain.model

enum class Prayer(val key: String, val isObligatory: Boolean) {
    IMSAK("imsak", false),
    SUBUH("subuh", true),
    TERBIT("terbit", false),
    DZUHUR("dzuhur", true),
    ASHAR("ashar", true),
    MAGHRIB("maghrib", true),
    ISYA("isya", true),
}
