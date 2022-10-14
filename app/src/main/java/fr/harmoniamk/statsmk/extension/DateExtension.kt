package fr.harmoniamk.statsmk.extension

import java.text.SimpleDateFormat
import java.util.*

fun Date.displayedString(): String = SimpleDateFormat("dd/MM/yyyy - HH'h'mm", Locale.FRANCE).format(this)
fun String.formatToDate(): Date? = SimpleDateFormat("dd/MM/yyyy - HH'h'mm", Locale.FRANCE).parse(this)

fun Date.add(field: Int = Calendar.DATE, amount: Int): Date {
    val c = Calendar.getInstance()
    c.time = this
    c.add(field, amount)
    return c.time
}

fun Date.set(field: Int = Calendar.DATE, value: Int): Date {
    val c = Calendar.getInstance()
    c.time = this
    c.set(field, value)
    return c.time
}

fun Date.sameDay(date: Date): Boolean {
    val cal1 = Calendar.getInstance()
    val cal2 = Calendar.getInstance()
    cal1.time = this
    cal2.time = date
    return cal1[Calendar.DAY_OF_YEAR] == cal2[Calendar.DAY_OF_YEAR] &&
            cal1[Calendar.YEAR] == cal2[Calendar.YEAR]
}

/**
 * Method used to get specific calendar field from date
 */
fun Date.get(field: Int = Calendar.DATE): Int {
    val c = Calendar.getInstance()
    c.time = this
    return c.get(field)
}

val Date.calendar: Calendar
    get() {
        val calendar = Calendar.getInstance()
        calendar.time = this
        return calendar
    }

fun Date.isBefore(other: Date): Boolean = this.time - other.time < 0

fun Date.timeBetween(other: Date): Long = when (this.isBefore(other)) {
    true -> (0 - this.time) + other.time
    else -> this.time - other.time
}