package fr.harmoniamk.statsmk.model.local

import android.os.Parcelable
import fr.harmoniamk.statsmk.extension.*
import fr.harmoniamk.statsmk.model.firebase.TOTAL_TRACKS
import fr.harmoniamk.statsmk.model.firebase.NewWar
import fr.harmoniamk.statsmk.model.firebase.TOTAL_TRACK_SCORE
import kotlinx.android.parcel.Parcelize
import java.io.Serializable
import java.util.*

@Parcelize
data class MKWar(val war: NewWar?) : Serializable, Parcelable {

    val warTracks = war?.warTracks?.map { MKWarTrack(it) }
    private val trackPlayed = warTracks?.size ?: 0
    val scoreHost = warTracks?.map { it.teamScore }.sum()
    private val scoreHostWithPenalties = scoreHost - war?.penalties?.filter { it.teamId == war.teamHost }?.map { it.amount }.sum()
    private val scoreOpponent = (TOTAL_TRACK_SCORE * trackPlayed) - scoreHost
    private val scoreOpponentWithPenalties = scoreOpponent - war?.penalties?.filter { it.teamId == war.teamOpponent }?.map { it.amount }.sum()
    val isOver = trackPlayed >= TOTAL_TRACKS
    val displayedScore = "$scoreHostWithPenalties - $scoreOpponentWithPenalties"
    val scoreLabel = "Score: $displayedScore"
    val displayedAverage = (scoreHost / TOTAL_TRACKS).trackScoreToDiff()
    val displayedState = if (isOver) "War terminée" else "War en cours (${trackPlayed}/$TOTAL_TRACKS)"
    val displayedDiff: String
        get() {
            val diff = scoreHostWithPenalties - scoreOpponentWithPenalties
            return if (diff > 0) "+$diff" else "$diff"
        }

    fun hasPlayer(playerId: String?): Boolean {
        war?.warTracks?.let {
            return it.size == it.filter { it.warPositions?.any { pos -> pos.playerId == playerId }.isTrue }.size
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