package fr.harmoniamk.statsmk.model.local

import android.os.Build
import fr.harmoniamk.statsmk.extension.get
import fr.harmoniamk.statsmk.extension.set
import fr.harmoniamk.statsmk.extension.sum
import fr.harmoniamk.statsmk.model.firebase.TOTAL_TRACKS
import fr.harmoniamk.statsmk.model.firebase.NewWar
import fr.harmoniamk.statsmk.model.firebase.TOTAL_TRACK_SCORE
import java.io.Serializable
import java.util.*

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
    var name: String? = null

    val isThisWeek: Boolean
      get() {
          val weekAgo = Date().set(Calendar.WEEK_OF_YEAR, Date().get(Calendar.WEEK_OF_YEAR) - 1)
          val warDate = war?.mid?.toLong()?.let { Date(it) }
          warDate?.let {
              return it.after(weekAgo)
          }
          return false
      }

    val isThisMonth: Boolean
      get() {
          val monthAgo = Date().set(Calendar.MONTH, Date().get(Calendar.MONTH) - 1)
          val warDate = war?.mid?.toLong()?.let { Date(it) }
          warDate?.let {
              return it.after(monthAgo)
          }
          return false
      }
}