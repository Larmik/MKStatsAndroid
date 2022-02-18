package fr.harmoniamk.statsmk.database.firebase.model

import java.io.Serializable

data class WarTrack(
    val mid: String? = null,
    val warId: String? = null,
    val trackIndex: Int? = null,
    var isOver: Boolean? = null,
    var teamScore: Int? = null
) : Serializable {
    val opponentScore: Int?
        get() {
            teamScore?.takeIf { it != 0 }?.let {
                return TOTAL_TRACK_SCORE - it
            }
            return null
        }

    val diffScore: Int?
        get() {
            teamScore?.let { teamScore ->
                opponentScore?.let {
                    return teamScore - it
                }
            }
            return null
        }
    val displayedResult: String
        get() = "$teamScore - $opponentScore"

    val displayedDiff: String
        get() {
            diffScore?.let {
                return if (it > 0) "+$it" else "$it"
            }
            return ""
        }


}