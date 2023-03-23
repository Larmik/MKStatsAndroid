package fr.harmoniamk.statsmk.extension

import android.util.Log
import fr.harmoniamk.statsmk.enums.Maps
import fr.harmoniamk.statsmk.fragment.stats.opponentRanking.OpponentRankingItemViewModel
import fr.harmoniamk.statsmk.fragment.stats.playerRanking.PlayerRankingItemViewModel
import fr.harmoniamk.statsmk.model.firebase.*
import fr.harmoniamk.statsmk.model.local.*
import fr.harmoniamk.statsmk.repository.DatabaseRepositoryInterface
import kotlinx.coroutines.flow.*

fun <T> List<T>.safeSubList(from: Int, to: Int): List<T> = when {
    this.size < to -> this
    else -> this.subList(from, to)
}

fun Any?.toMapList(): List<Map<*,*>>? = this as? List<Map<*,*>>

fun List<MKWar>.getCurrent(teamId: String?) = this.singleOrNull { war -> !war.isOver && war.war?.teamHost == teamId }
fun List<MKWar>.withFullStats(databaseRepository: DatabaseRepositoryInterface, userId: String? = null, teamId: String? = null, isIndiv: Boolean = false) = flow {
    Log.d("MKDebugOnly", "ListExtension withFullStats: userId = $userId, teamId = $teamId, isIndiv= $isIndiv")
    val maps = mutableListOf<TrackStats>()
    val warScores = mutableListOf<WarScore>()
    val averageForMaps = mutableListOf<TrackStats>()
    val wars = when  {
        isIndiv && userId != null && teamId != null -> this@withFullStats.filter { it.hasPlayer(userId) && it.hasTeam(teamId) }
        isIndiv && userId != null -> this@withFullStats.filter { it.hasPlayer(userId) }
        teamId != null -> this@withFullStats.filter { it.hasTeam(teamId) }
        else -> this@withFullStats
    }

    val mostPlayedTeamId = wars
        .filterNot { it.war?.teamOpponent == "1652270659565" }
        .groupBy { it.war?.teamOpponent }
        .toList()
        .sortedByDescending { it.second.size }
        .firstOrNull()

    val mostDefeatedTeamId = wars
        .filterNot { it.displayedDiff.contains('-') }
        .filterNot { it.war?.teamOpponent == "1652270659565" }
        .groupBy { it.war?.teamOpponent }
        .toList()
        .sortedByDescending { it.second.size }
        .firstOrNull()

    val lessDefeatedTeamId = wars
        .filter { it.displayedDiff.contains('-') }
        .filterNot { it.war?.teamOpponent == "1652270659565" }
        .groupBy { it.war?.teamOpponent }
        .toList()
        .sortedByDescending { it.second.size }
        .firstOrNull()

    wars.map { Pair(it, it.war?.warTracks?.map { MKWarTrack(it) }) }
        .forEach {
            var currentPoints = 0
            it.second?.forEach { track ->
                val playerScoreForTrack = track.track?.warPositions
                    ?.singleOrNull { pos -> pos.playerId == userId }
                    ?.position.positionToPoints()
                var teamScoreForTrack = 0
                track.track?.warPositions?.map { it.position.positionToPoints() }?.forEach {
                    teamScoreForTrack += it
                }
                currentPoints += when (isIndiv) {
                    true -> playerScoreForTrack
                    else -> teamScoreForTrack
                }
                var shockCount = 0
                track.track?.shocks?.filter { (!isIndiv || userId == null) || it.playerId == userId }?.map { it.count }?.forEach {
                    shockCount += it
                }
                maps.add(TrackStats(trackIndex = track.track?.trackIndex, teamScore = teamScoreForTrack, playerScore = playerScoreForTrack, shockCount = shockCount))
            }
            warScores.add(WarScore(it.first, currentPoints))
            currentPoints = 0
        }
    maps.groupBy { it.trackIndex }
        .filter { it.value.isNotEmpty() }
        .forEach { entry ->
            val stats = TrackStats(
                map = Maps.values()[entry.key ?: -1],
                teamScore = (entry.value.map { it.teamScore }.sum() / entry.value.map { it.teamScore }.count()),
                playerScore = (entry.value.map { it.playerScore }.sum() / entry.value.map { it.playerScore }.count()),
                totalPlayed = entry.value.size
            )
            averageForMaps.add(stats)
        }

    val mostPlayedTeamData = databaseRepository.getTeam(mostPlayedTeamId?.first)
        .mapNotNull { TeamStats(it, mostPlayedTeamId?.second?.size) }
        .firstOrNull()
    val mostDefeatedTeamData = databaseRepository.getTeam(mostDefeatedTeamId?.first)
        .mapNotNull { TeamStats(it, mostDefeatedTeamId?.second?.size) }
        .firstOrNull()
    val lessDefeatedTeamData = databaseRepository.getTeam(lessDefeatedTeamId?.first)
        .mapNotNull { TeamStats(it, lessDefeatedTeamId?.second?.size) }
        .firstOrNull()

    val newStats = Stats(
        warStats = WarStats(wars),
        warScores = warScores,
        maps = maps,
        averageForMaps = averageForMaps,
        mostPlayedTeam = mostPlayedTeamData,
        mostDefeatedTeam = mostDefeatedTeamData,
        lessDefeatedTeam = lessDefeatedTeamData
    )
    emit(newStats)
}
fun List<MKWar?>.withName(databaseRepository: DatabaseRepositoryInterface) = flow {
    val temp = mutableListOf<MKWar>()
    Log.d("MKDebugOnly", "ListExtension withName: ")
    this@withName.forEach { war ->
        war?.let {
            val hostName = databaseRepository.getTeam(it.war?.teamHost).firstOrNull()?.shortName
            val opponentName = databaseRepository.getTeam(it.war?.teamOpponent).firstOrNull()?.shortName
            temp.add(it.apply { this.name = "$hostName - $opponentName" })
        }
    }
    emit(temp)
}
fun NewWar?.withName(databaseRepository: DatabaseRepositoryInterface) = flow {
    this@withName?.let {
        val hostName = databaseRepository.getTeam(it?.teamHost).firstOrNull()?.shortName
        val opponentName = databaseRepository.getTeam(it?.teamOpponent).firstOrNull()?.shortName
        emit(MKWar(it).apply { this.name = "$hostName - $opponentName" })
    }
}

fun List<Team>.withFullTeamStats(wars: List<MKWar>?, databaseRepository: DatabaseRepositoryInterface, userId: String? = null, weekOnly: Boolean = false, monthOnly: Boolean = false, isIndiv: Boolean = false) = flow {
    val temp = mutableListOf<OpponentRankingItemViewModel>()
    Log.d("MKDebugOnly", "ListExtension withFullTeamStats:  wars = ${wars?.map { it.name }}, weekOnly = $weekOnly, monthOnly = $monthOnly, isIndiv= $isIndiv")
    this@withFullTeamStats.forEach { team ->
        wars
            ?.filter { !weekOnly || (weekOnly && it.isThisWeek) }
            ?.filter { !monthOnly || (monthOnly && it.isThisMonth) }
            ?.withFullStats(databaseRepository, teamId = team.mid, userId = userId, isIndiv = isIndiv)?.first()
            ?.let {
                if (it.warStats.list.isNotEmpty())
                    temp.add(OpponentRankingItemViewModel(team, it, userId, isIndiv))
            }
    }
    emit(temp)
}

fun List<Penalty>.withTeamName(databaseRepository: DatabaseRepositoryInterface) = flow {
    val temp = mutableListOf<Penalty>()
    this@withTeamName.forEach {
        val teamName = databaseRepository.getTeam(it.teamId).firstOrNull()?.name
        temp.add(it.apply { this.teamName = teamName })
    }
    emit(temp)
}

fun List<Int?>?.sum(): Int {
    var sum = 0
    this?.filterNotNull()?.forEach { sum += it }
    return sum
}

fun List<Map<*,*>>?.parseTracks() : List<NewWarTrack>? =
    this?.map { track ->
        NewWarTrack(
            mid = track["mid"].toString(),
            trackIndex = track["trackIndex"].toString().toInt(),
            warPositions = (track["warPositions"]?.toMapList())
                ?.map {
                    NewWarPositions(
                        mid = it["mid"].toString(),
                        playerId = it["playerId"].toString(),
                        position = it["position"].toString().toInt()
                    )
                },
            shocks = (track["shocks"]?.toMapList())
                ?.map {
                    Shock(
                        playerId = it["playerId"].toString(),
                        count = it["count"].toString().toInt()
                    )
                }
        )

}

fun List<Map<*,*>>?.parsePenalties() : List<Penalty>? =
    this?.map { item ->
        Penalty(
            teamId = item["teamId"].toString(),
            amount = item["amount"].toString().toInt()
        )
}

fun List<MapDetails>.getVictory() = this.maxByOrNull { it.warTrack.teamScore }?.takeIf { it.warTrack.displayedDiff.contains('+') }
fun List<MapDetails>.getDefeat() = this.minByOrNull { it.warTrack.teamScore }?.takeIf { it.warTrack.displayedDiff.contains('-') }