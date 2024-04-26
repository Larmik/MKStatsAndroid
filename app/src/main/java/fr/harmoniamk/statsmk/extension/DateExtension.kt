package fr.harmoniamk.statsmk.extension

import java.text.SimpleDateFormat
import java.util.*

fun Date.displayedString(pattern: String): String = SimpleDateFormat(pattern, Locale.getDefault()).format(this)
fun String.formatToDate(pattern: String = "dd/MM/yyyy - HH'h'mm"): Date? = SimpleDateFormat(pattern, Locale.getDefault()).parse(this)

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

/**
 * Method used to get specific calendar field from date
 */
fun Date.get(field: Int = Calendar.DATE): Int {
    val c = Calendar.getInstance()
    c.time = this
    return c.get(field)
}

