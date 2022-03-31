package fr.harmoniamk.statsmk.model

import fr.harmoniamk.statsmk.database.model.TOTAL_TRACKS
import fr.harmoniamk.statsmk.database.model.War
import java.io.Serializable

data class MKWar(val war: War?) : Serializable {

    val isOver = war?.trackPlayed == TOTAL_TRACKS

    val displayedState: String
        get() = if (isOver) "War terminée" else "War en cours (${war?.trackPlayed}/$TOTAL_TRACKS)"
    val displayedScore: String
        get() = "${war?.scoreHost} - ${war?.scoreOpponent}"
    val displayedDiff: String
        get() {
            val diff = (war?.scoreHost ?: 0) - (war?.scoreOpponent ?: 0)
            return if (diff > 0) "+$diff" else "$diff"
        }
    val scoreLabel: String
        get() = "Score: ${war?.scoreHost} - ${war?.scoreOpponent}"
    val displayedAverage: String
        get() {
            war?.scoreHost?.let {
                return "${it / TOTAL_TRACKS}"
            }
            return ""
        }


}