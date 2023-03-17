package fr.harmoniamk.statsmk.extension

import android.util.Log
import fr.harmoniamk.statsmk.enums.Maps
import fr.harmoniamk.statsmk.fragment.stats.opponentRanking.OpponentRankingItemViewModel
import fr.harmoniamk.statsmk.fragment.stats.playerRanking.PlayerRankingItemViewModel
import fr.harmoniamk.statsmk.model.firebase.*
import fr.harmoniamk.statsmk.model.local.*
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import kotlinx.coroutines.flow.*

fun List<MKWar>.getLasts(teamId: String?) = this.filter { war -> war.isOver && war.war?.teamHost == teamId }.sortedByDescending{ it.war?.createdDate?.formatToDate() }.safeSubList(0, 5)
fun List<MKWar>.getCurrent(teamId: String?) = this.singleOrNull { war -> !war.isOver && war.war?.teamHost == teamId }
fun List<MKWar?>.withName(firebaseRepository: FirebaseRepositoryInterface) = flow {
    val temp = mutableListOf<MKWar>()
    this@withName.forEach { war ->
        war?.let {
            val hostName = firebaseRepository.getTeam(it.war?.teamHost).firstOrNull()?.shortName
            val opponentName = firebaseRepository.getTeam(it.war?.teamOpponent).firstOrNull()?.shortName
            temp.add(it.apply { this.name = "$hostName - $opponentName" })
        }
    }
    emit(temp)
}


fun List<User>.withFullStats(firebaseRepository: FirebaseRepositoryInterface) = flow {
    val temp = mutableListOf<PlayerRankingItemViewModel>()
    val wars = firebaseRepository.getNewWars().first().map { MKWar(it) }
    this@withFullStats.forEach { user ->
        val stats = wars.withFullStats(firebaseRepository, userId = user.mid).first()
        temp.add(PlayerRankingItemViewModel(user, stats))
    }
    emit(temp)
}


fun List<Team>.withFullTeamStats(firebaseRepository: FirebaseRepositoryInterface, userId: String? = null) = flow {
    val temp = mutableListOf<OpponentRankingItemViewModel>()
    val wars = firebaseRepository.getNewWars().first().map { MKWar(it) }
    this@withFullTeamStats.forEach { team ->
        val stats = wars.withFullStats(firebaseRepository, teamId = team.mid, userId = userId).first()
        temp.add(OpponentRankingItemViewModel(team, stats, userId))
    }
    emit(temp.filterNot { vm -> vm.stats.warStats.warsPlayed < 2 })
}

fun List<MKWar>.withFullStats(firebaseRepository: FirebaseRepositoryInterface, userId: String? = null, teamId: String? = null) = flow {

    val maps = mutableListOf<TrackStats>()
    val warScores = mutableListOf<WarScore>()
    val averageForMaps = mutableListOf<TrackStats>()
    val wars = when  {
        userId != null && teamId != null -> this@withFullStats.filter { it.hasPlayer(userId) && it.hasTeam(teamId) }
        userId != null -> this@withFullStats.filter { it.hasPlayer(userId) }
        teamId != null -> this@withFullStats.filter { it.hasTeam(teamId) }
        else -> this@withFullStats.withName(firebaseRepository).first()
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
                currentPoints += when (userId) {
                    null -> teamScoreForTrack
                    else -> playerScoreForTrack
                }
                var shockCount = 0
                track.track?.shocks?.filter { userId == null || it.playerId == userId }?.map { it.count }?.forEach {
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
                totalPlayed = entry.value.size
            )
            Log.d("MKDebug", "averageFor map ${stats.map?.name}, score ${stats.teamScore}")
            averageForMaps.add(stats)
        }

    val mostPlayedTeamData = firebaseRepository.getTeam(mostPlayedTeamId?.first ?: "")
        .mapNotNull { TeamStats(it?.name, mostPlayedTeamId?.second?.size) }
        .firstOrNull()
    val mostDefeatedTeamData = firebaseRepository.getTeam(mostDefeatedTeamId?.first ?: "")
        .mapNotNull { TeamStats(it?.name, mostDefeatedTeamId?.second?.size) }
        .firstOrNull()
    val lessDefeatedTeamData = firebaseRepository.getTeam(lessDefeatedTeamId?.first ?: "")
        .mapNotNull { TeamStats(it?.name, lessDefeatedTeamId?.second?.size) }
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

fun List<Penalty>.withTeamName(firebaseRepository: FirebaseRepositoryInterface) = flow {
    val temp = mutableListOf<Penalty>()
    this@withTeamName.forEach {
        val teamName = firebaseRepository.getTeam(it.teamId).firstOrNull()?.name
        temp.add(it.apply { this.teamName = teamName })
    }
    emit(temp)
}

fun List<Int?>?.sum(): Int {
    var sum = 0
    this?.filterNotNull()?.forEach { sum += it }
    return sum
}

fun <T> List<T>.safeSubList(from: Int, to: Int): List<T> = when {
    this.size < to -> this
    else -> this.subList(from, to)
}

fun Any?.toMapList(): List<Map<*,*>>? = this as? List<Map<*,*>>

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