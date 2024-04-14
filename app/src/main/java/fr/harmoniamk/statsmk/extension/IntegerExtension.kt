package fr.harmoniamk.statsmk.extension

import androidx.compose.ui.graphics.Color
import fr.harmoniamk.statsmk.R

fun Int?.positionToPoints() = when (this) {
    1 -> 15
    2 -> 12
    3 -> 10
    4 -> 9
    5 -> 8
    6 -> 7
    7 -> 6
    8 -> 5
    9 -> 4
    10 -> 3
    11 -> 2
    12 -> 1
    else -> 0
}
fun Int?.pointsToPosition() = when (this) {
    14, 15 -> 1
    11, 12, 13 -> 2
    10 -> 3
    9 -> 4
    8 -> 5
    7 -> 6
    6 -> 7
    5 -> 8
    4 -> 9
    3 -> 10
    2 -> 11
    1 -> 12
    else -> 0
}

fun Int?.positionColor() = when (this) {
    1 -> R.color.pos_1
    2 -> R.color.pos_2
    3 -> R.color.pos_3
    4 -> R.color.pos_4
    5 -> R.color.pos_5
    6,7 -> R.color.pos_6_7
    8 -> R.color.pos_8
    9,10 -> R.color.pos_9_10
    11 -> R.color.pos_11
    12 -> R.color.pos_12
    else -> R.color.black
}

fun Int.warScoreToDiff() : String {
    val halfDiff = when {
        this > 492 -> this - 492
        this < 492 -> 492 - this
        else -> 0
    }
    return when {
        this > 492 -> "+${halfDiff*2}"
        this < 492 -> "-${halfDiff*2}"
        else -> "0"
    }
}
fun Int.trackScoreToDiff() : String {
    val halfDiff = when {
        this > 41 -> this - 41
        this < 41 -> 41 - this
        else -> 0
    }
    return when {
        this > 41 -> "+${halfDiff*2}"
        this < 41 -> "-${halfDiff*2}"
        else -> "0"
    }
}

fun Color.Companion.fromHex(hexa: String) = Color(android.graphics.Color.parseColor(hexa))

fun Int?.toTeamColor()  = Color(android.graphics.Color.parseColor(when (this) {
    1 -> "#ef5350"
    2 -> "#ffa726"
    3 -> "#d4e157"
    4 -> "#66bb6a"
    5 -> "#26a69a"
    6 -> "#29b6f6"
    7 -> "#5c6bc0"
    8 -> "#7e57c2"
    9 -> "#ec407a"
    10 -> "#888888"
    11 -> "#c62828"
    12 -> "#ef6c00"
    13 -> "#9e9d24"
    14 -> "#2e7d32"
    15 -> "#00897b"
    16 -> "#0277bd"
    17 -> "#283593"
    18 -> "#4527a0"
    19 -> "#ad1457"
    20 -> "#444444"
    21 -> "#d44a48"
    22 -> "#e69422"
    23 -> "#bdc74e"
    24 -> "#4a874c"
    25 -> "#208c81"
    26 -> "#25a5db"
    27 -> "#505ca6"
    28 -> "#6c4ca8"
    29 -> "#d13b6f"
    30 -> "#545454"
    31 -> "#ab2424"
    32 -> "#d45f00"
    33 -> "#82801e"
    34 -> "#205723"
    35 -> "#006e61"
    36 -> "#0369a3"
    37 -> "#222d78"
    38 -> "#382185"
    39 -> "#91114b"
    else -> "#000000"
}))
