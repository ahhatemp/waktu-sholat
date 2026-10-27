package id.haikal.waktusholat.ui.common

import androidx.annotation.StringRes
import id.haikal.waktusholat.R
import id.haikal.waktusholat.domain.model.Prayer

@get:StringRes
val Prayer.labelRes: Int
    get() = when (this) {
        Prayer.IMSAK -> R.string.prayer_imsak
        Prayer.SUBUH -> R.string.prayer_subuh
        Prayer.TERBIT -> R.string.prayer_terbit
        Prayer.DZUHUR -> R.string.prayer_dzuhur
        Prayer.ASHAR -> R.string.prayer_ashar
        Prayer.MAGHRIB -> R.string.prayer_maghrib
        Prayer.ISYA -> R.string.prayer_isya
    }
