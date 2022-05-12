package fr.harmoniamk.statsmk.model.local

import fr.harmoniamk.statsmk.extension.sum
import fr.harmoniamk.statsmk.model.firebase.TOTAL_TRACKS
import fr.harmoniamk.statsmk.model.firebase.NewWar
import fr.harmoniamk.statsmk.model.firebase.TOTAL_TRACK_SCORE
import java.io.Serializable

data class MKWar(val war: NewWar?) : Serializable {

    val warTracks = war?.warTracks?.map { MKWarTrack(it) }

    val trackPlayed = warTracks?.size ?: 0
    val isOver = trackPlayed == TOTAL_TRACKS
    val scoreHost = warTracks?.map { it.teamScore }.sum()
    val scoreOpponent = (TOTAL_TRACK_SCORE * trackPlayed) - scoreHost

    val displayedState: String
        get() = if (isOver) "War terminée" else "War en cours (${trackPlayed}/$TOTAL_TRACKS)"

    val displayedScore: String
        get() = "$scoreHost - $scoreOpponent"

    val displayedDiff: String
        get() {
            val diff = scoreHost - scoreOpponent
            return if (diff > 0) "+$diff" else "$diff"
        }
    val scoreLabel: String
        get() = "Score: $displayedScore"

    val displayedAverage = "${scoreHost / TOTAL_TRACKS}"

}