package fr.harmoniamk.statsmk.model.local

import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.model.firebase.TOTAL_TRACK_SCORE
import fr.harmoniamk.statsmk.extension.positionToPoints
import fr.harmoniamk.statsmk.extension.sum
import fr.harmoniamk.statsmk.model.firebase.NewWarTrack

data class MKWarTrack(val track: NewWarTrack?) {

    val teamScore = track?.warPositions?.map { it.position.positionToPoints() }.sum()

    val isOver = track?.warPositions?.size == 6

    val opponentScore: Int?
        get() {
            teamScore.takeIf { it != 0 }?.let {
                return TOTAL_TRACK_SCORE - it
            }
            return null
        }

    val diffScore: Int?
        get() {
            opponentScore?.let {
                return teamScore - it
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

    val backgroundColor : Int
        get() {
            diffScore?.let {
                return when {
                    it > 0 -> R.color.win
                    it < 0 -> R.color.lose
                    else -> R.color.white_alphaed
                }
            }
            return R.color.white_alphaed
        }

}