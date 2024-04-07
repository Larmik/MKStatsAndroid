package fr.harmoniamk.statsmk.extension

import android.util.Log
import fr.harmoniamk.statsmk.model.firebase.NewWar
import fr.harmoniamk.statsmk.model.firebase.WarDispo
import fr.harmoniamk.statsmk.model.local.MKWar
import fr.harmoniamk.statsmk.model.network.MKCTeam
import fr.harmoniamk.statsmk.repository.DatabaseRepositoryInterface
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow

fun WarDispo.withLineUpAndOpponent(databaseRepository: DatabaseRepositoryInterface) = flow {
    Log.d("MKDebugOnly", "ListExtension withLineUpAndOpponent")
    this@withLineUpAndOpponent.opponentId?.takeIf { it != "null" }.let { id ->
        val opponentName = databaseRepository.getNewTeam(id).firstOrNull()?.team_name
        val lineupNames = mutableListOf<String?>()
        var hostName: String? = null
        this@withLineUpAndOpponent.lineUp?.forEach {
            val playerName = databaseRepository.getNewUser(it.userId).firstOrNull()?.name
            lineupNames.add(playerName)
        }
        this@withLineUpAndOpponent.host?.takeIf { it != "null" }?.let {
            hostName = when (this@withLineUpAndOpponent.host?.contains("-")) {
                true -> this@withLineUpAndOpponent.host
                else -> databaseRepository.getNewUser(this@withLineUpAndOpponent.host)
                    .firstOrNull()?.name
            }
        }
        emit(this@withLineUpAndOpponent.apply {
            this.lineupNames = lineupNames.filterNotNull()
            this.opponentName = opponentName
            this.hostName = hostName
        })
    }
}

fun NewWar?.withName(databaseRepository: DatabaseRepositoryInterface) = flow {
    Log.d("MKDebugOnly", "ListExtension withName: for war")
    this@withName?.let {
        val hostName = databaseRepository.getNewTeam(it.teamHost).firstOrNull()?.team_tag
        val opponentName = databaseRepository.getNewTeam(it.teamOpponent).firstOrNull()?.team_tag
        emit(MKWar(it).apply { this.name = "$hostName - $opponentName" })
    } ?: emit(null)
}

fun MKCTeam.withFullTeamStats(
    wars: List<MKWar>?,
    databaseRepository: DatabaseRepositoryInterface,
    userId: String? = null,
    weekOnly: Boolean = false,
    monthOnly: Boolean = false,
) = flow {
    Log.d("MKDebugOnly", "ListExtension withFullTeamStats: for team")
    wars
        ?.filter { (weekOnly && it.isThisWeek) || !weekOnly }
        ?.filter { (monthOnly && it.isThisMonth) || !monthOnly }
        ?.withFullStats(databaseRepository, teamId = this@withFullTeamStats.team_id, userId = userId)
        ?.first()
        ?.takeIf { it.warStats.list.isNotEmpty() }
        ?.let { emit(it) }
}