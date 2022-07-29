package fr.harmoniamk.statsmk.model.local

import fr.harmoniamk.statsmk.extension.sum
import fr.harmoniamk.statsmk.model.firebase.TOTAL_TRACKS
import fr.harmoniamk.statsmk.model.firebase.NewWar
import fr.harmoniamk.statsmk.model.firebase.TOTAL_TRACK_SCORE
import java.io.Serializable

data class MKWar(val war: NewWar?) : Serializable {

    private val warTracks = war?.warTracks?.map { MKWarTrack(it) }
    private val trackPlayed = warTracks?.size ?: 0
    val scoreHost = warTracks?.map { it.teamScore }.sum()
    private val scoreOpponent = (TOTAL_TRACK_SCORE * trackPlayed) - scoreHost
    val isOver = trackPlayed >= TOTAL_TRACKS
    val displayedScore = "$scoreHost - $scoreOpponent"
    val scoreLabel = "Score: $displayedScore"
    val displayedAverage = "${scoreHost / TOTAL_TRACKS}"
    val displayedState = if (isOver) "War terminée" else "War en cours (${trackPlayed}/$TOTAL_TRACKS)"
    val displayedDiff: String
        get() {
            val diff = scoreHost - scoreOpponent
            return if (diff > 0) "+$diff" else "$diff"
        }

    fun hasPlayer(playerId: String?): Boolean {
        war?.warTracks?.mapNotNull { it.warPositions }?.forEach {
            return it.any { pos -> pos.playerId == playerId }
        }
        return false
    }
}