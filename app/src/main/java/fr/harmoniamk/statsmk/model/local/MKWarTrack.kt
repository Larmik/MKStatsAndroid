package fr.harmoniamk.statsmk.model.local

import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.model.firebase.TOTAL_TRACK_SCORE
import fr.harmoniamk.statsmk.extension.positionToPoints
import fr.harmoniamk.statsmk.extension.sum
import fr.harmoniamk.statsmk.model.firebase.NewWarTrack

data class MKWarTrack(val track: NewWarTrack?) {

    val teamScore: Int
        get() = track?.warPositions?.map { it.position.positionToPoints() }.sum()

    val isOver: Boolean
        get() = track?.warPositions?.size == 6

    val opponentScore: Int
        get() {
            teamScore.takeIf { it != 0 }?.let {
                return TOTAL_TRACK_SCORE - it
            }
            return 0
        }

    val diffScore: Int
        get() {
            opponentScore.takeIf { it != 0 }?.let {
                return teamScore - it
            }
            return 0
        }
    val displayedResult: String
        get() = "$teamScore - $opponentScore"

    val displayedDiff: String
        get() = if (diffScore > 0) "+$diffScore" else "$diffScore"

    val backgroundColor : Int
        get() = when {
            diffScore > 0 -> R.color.win
            diffScore < 0 -> R.color.lose
            else -> R.color.white_alphaed
        }
}