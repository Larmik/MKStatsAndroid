package fr.harmoniamk.statsmk.extension

import java.text.SimpleDateFormat
import java.util.*

fun Date.displayedString(): String = SimpleDateFormat("dd/MM/yyyy - HH'h'mm", Locale.FRANCE).format(this)