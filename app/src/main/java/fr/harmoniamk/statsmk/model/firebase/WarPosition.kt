package fr.harmoniamk.statsmk.model.firebase

data class WarPosition(
    val mid: String? = null,
    val warTrackId: String? = null,
    val playerId: String? = null,
    val position: Int? = null
) {
    val points: Int? = when (position) {
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
        else -> null
    }
}