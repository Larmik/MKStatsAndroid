package fr.harmoniamk.statsmk.extension

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
    15 -> 1
    12 -> 2
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
    else -> R.color.waluigi_second
}