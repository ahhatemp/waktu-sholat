package id.haikal.waktusholat.ui.common

import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

private val IndonesianLocale = Locale.forLanguageTag("id-ID")
private val FullDateFormat = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", IndonesianLocale)
private val MonthYearFormat = DateTimeFormatter.ofPattern("MMMM yyyy", IndonesianLocale)
private val WeekdayFormat = DateTimeFormatter.ofPattern("EE", IndonesianLocale)
private val TimeFormat = DateTimeFormatter.ofPattern("HH:mm")

object IndonesianDates {
    fun full(date: LocalDate): String = date.format(FullDateFormat)

    fun monthYear(month: YearMonth): String = month.atDay(1).format(MonthYearFormat)

    fun weekday(date: LocalDate): String = date.format(WeekdayFormat)
}

fun LocalTime.toDisplayString(): String = format(TimeFormat)

fun formatCountdown(totalSeconds: Long): String {
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.ROOT, "%02d:%02d:%02d", hours, minutes, seconds)
}
