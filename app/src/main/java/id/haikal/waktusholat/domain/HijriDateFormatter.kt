package id.haikal.waktusholat.domain

import java.time.LocalDate
import java.time.chrono.HijrahDate
import java.time.temporal.ChronoField

object HijriDateFormatter {

    private val monthNames = listOf(
        "Muharram",
        "Safar",
        "Rabiul Awal",
        "Rabiul Akhir",
        "Jumadil Awal",
        "Jumadil Akhir",
        "Rajab",
        "Sya'ban",
        "Ramadan",
        "Syawal",
        "Dzulqa'dah",
        "Dzulhijjah",
    )

    fun format(date: LocalDate): String {
        val hijri = HijrahDate.from(date)
        val day = hijri.get(ChronoField.DAY_OF_MONTH)
        val month = monthNames[hijri.get(ChronoField.MONTH_OF_YEAR) - 1]
        val year = hijri.get(ChronoField.YEAR)
        return "$day $month $year H"
    }
}
