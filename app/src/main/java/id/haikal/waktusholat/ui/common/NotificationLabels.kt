package id.haikal.waktusholat.ui.common

import androidx.annotation.StringRes
import id.haikal.waktusholat.R
import id.haikal.waktusholat.domain.model.AdhanSound

@get:StringRes
val AdhanSound.labelRes: Int
    get() = when (this) {
        AdhanSound.FULL -> R.string.sound_full
        AdhanSound.SHORT -> R.string.sound_short
        AdhanSound.DEFAULT -> R.string.sound_default
        AdhanSound.SILENT -> R.string.sound_silent
    }
