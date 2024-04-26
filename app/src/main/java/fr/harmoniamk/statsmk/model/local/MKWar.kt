package fr.harmoniamk.statsmk.model.local

import fr.harmoniamk.statsmk.extension.*
import fr.harmoniamk.statsmk.model.firebase.NewWar
import fr.harmoniamk.statsmk.model.network.MKCFullTeam
import java.io.Serializable
import java.util.*

data class MKWar(val war: NewWar?) : Serializable {


    val warTracks = war?.warTracks?.map { MKWarTrack(it) }
    val trackPlayed = warTracks?.size ?: 0
    val scoreHost = warTracks?.map { it.teamScore }.sum()
    private val scoreHostWithPenalties = scoreHost - war?.penalties?.filter { it.teamId == war.teamHost }?.map { it.amount }.sum()
    private val scoreOpponent = (82 * trackPlayed) - scoreHost
    private val scoreOpponentWithPenalties = scoreOpponent - war?.penalties?.filter { it.teamId == war.teamOpponent }?.map { it.amount }.sum()
    val isOver = trackPlayed >= 12
    val displayedScore = "$scoreHostWithPenalties - $scoreOpponentWithPenalties"
    val mapsWon = "${war?.warTracks?.map { MKWarTrack(it) }?.filter { it.displayedDiff.contains("+") }?.size} / 12"
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
    fun hasTeam(teamId: String?): Boolean {
        return war?.teamHost == teamId || war?.teamOpponent == teamId
    }
    fun hasTeam(team: MKCFullTeam?, rosterOnly: Boolean = false): Boolean {
        return when (rosterOnly) {
            true -> war?.teamHost == team?.id || war?.teamOpponent == team?.id
            else -> war?.teamHost == team?.id
                    || war?.teamHost == team?.primary_team_id?.toString()
                    ||team?.secondary_teams?.map { it.id }?.contains(war?.teamHost).isTrue
                    || war?.teamOpponent == team?.id
                    || war?.teamOpponent == team?.primary_team_id?.toString()
                    || team?.secondary_teams?.map { it.id }?.contains(war?.teamOpponent).isTrue
        }
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