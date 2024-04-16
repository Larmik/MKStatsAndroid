package fr.harmoniamk.statsmk.model.local

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.extension.isTrue
import fr.harmoniamk.statsmk.extension.positionToPoints
import fr.harmoniamk.statsmk.extension.sum
import fr.harmoniamk.statsmk.model.firebase.NewWarTrack
import kotlinx.android.parcel.Parcelize

data class MKWarTrack(val track: NewWarTrack?) {

    val index
        get() = track?.trackIndex

    val teamScore: Int
        get() = track?.warPositions?.map { it.position.positionToPoints() }.sum()

    fun hasPlayer(playerId: String?) = track?.warPositions?.any { pos -> pos.playerId == playerId }.isTrue

    private val opponentScore: Int
        get() {
            teamScore.takeIf { it != 0 }?.let {
                return 82 - it
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

    @Composable
    fun backgroundColor() : Color = when {
            diffScore > 0 -> colorResource(R.color.win)
            diffScore < 0 -> colorResource(R.color.lose)
            else -> colorResource(R.color.transparent)
        }
}