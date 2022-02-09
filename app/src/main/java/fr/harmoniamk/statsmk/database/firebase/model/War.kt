package fr.harmoniamk.statsmk.database.firebase.model


const val TOTAL_TRACKS = 12
const val TOTAL_WAR_SCORE = 984
const val TOTAL_TRACK_SCORE = 82

data class War(
    val mid: String? = null,
    val name: String? = null,
    val playerHostId: String? = null,
    val teamHost: String? = null,
    val teamOpponent: String? = null,
    var scoreHost: Int = 0,
    var scoreOpponent: Int = 0,
    var trackPlayed: Int = 0,
    val createdDate: String? = null,
    var updatedDate: String? = null
) {

    val isOver = trackPlayed == TOTAL_TRACKS

    val displayedState: String
        get() = if (isOver) "War terminée" else "War en cours (${trackPlayed}/${TOTAL_TRACKS})"
    val displayedScore: String
        get() = "$scoreHost - $scoreOpponent"
    val displayedDiff: String
        get() {
            val diff = scoreHost-scoreOpponent
            return if (diff > 0) "+$diff" else "$diff"
        }
    val scoreLabel: String
        get() = "Score: $scoreHost - $scoreOpponent"
}