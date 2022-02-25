package fr.harmoniamk.statsmk.model

import fr.harmoniamk.statsmk.database.model.TOTAL_TRACKS
import fr.harmoniamk.statsmk.database.model.War

data class MKWar(val war: War?) {

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
        get() = "${war?.scoreHost ?: 0 / TOTAL_TRACKS}"


}