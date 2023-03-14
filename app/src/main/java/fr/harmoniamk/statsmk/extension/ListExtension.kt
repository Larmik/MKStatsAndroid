package fr.harmoniamk.statsmk.extension

import android.util.Log
import fr.harmoniamk.statsmk.enums.Maps
import fr.harmoniamk.statsmk.fragment.stats.playerRanking.PlayerRankingItemViewModel
import fr.harmoniamk.statsmk.model.firebase.*
import fr.harmoniamk.statsmk.model.local.*
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.StorageRepositoryInterface
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


fun List<User>.withFullStats(firebaseRepository: FirebaseRepositoryInterface, storageRepository: StorageRepositoryInterface) = flow {
    val temp = mutableListOf<PlayerRankingItemViewModel>()
    this@withFullStats.forEach { user ->
        val stats = firebaseRepository.getNewWars().first().map { MKWar(it) }.withFullStats(firebaseRepository, user.mid).first()
        val picture = storageRepository.getPicture(user.mid).map { (it as? PictureResponse.Success)?.url ?: "https://firebasestorage.googleapis.com/v0/b/stats-mk-debug.appspot.com/o/hr_logo.png?alt=media&token=6f4452bf-7028-4203-8d77-0c3eb0d8cd48" }.first()
        temp.add(PlayerRankingItemViewModel(user, stats, picture))
    }
    emit(temp)
}

fun List<MKWar>.withFullStats(firebaseRepository: FirebaseRepositoryInterface, userId: String? = null) = flow {

    val maps = mutableListOf<TrackStats>()
    val warScores = mutableListOf<WarScore>()
    val averageForMaps = mutableListOf<TrackStats>()
    val wars = when (userId) {
        null -> this@withFullStats
        else -> this@withFullStats.filter { it.hasPlayer(userId) }
    }.withName(firebaseRepository).first()

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
                var scoreForTrack = 0
                when (userId) {
                    null -> track.track?.warPositions?.map { it.position.positionToPoints() }?.forEach {
                        scoreForTrack += it
                    }
                    else -> scoreForTrack = track.track?.warPositions
                        ?.singleOrNull { pos -> pos.playerId == userId }
                        ?.position.positionToPoints()
                }
                currentPoints += scoreForTrack
                maps.add(TrackStats(trackIndex = track.track?.trackIndex, score = scoreForTrack))
            }
            warScores.add(WarScore(it.first, currentPoints))
            currentPoints = 0
        }
    maps.groupBy { it.trackIndex }
        .filter { it.value.isNotEmpty() }
        .forEach { entry ->
            val stats = TrackStats(
                map = Maps.values()[entry.key ?: -1],
                score = (entry.value.map { it.score }.sum() / entry.value.map { it.score }.count()),
                totalPlayed = entry.value.size
            )
            Log.d("MKDebug", "averageFor map ${stats.map?.name}, score ${stats.score}")
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


fun List<NewWarPositions>.withPlayerName(firebaseRepository: FirebaseRepositoryInterface) = flow {
    val temp = mutableListOf<MKWarPosition>()
    this@withPlayerName.forEach {
        val user = firebaseRepository.getUser(it.playerId).firstOrNull()
        temp.add(MKWarPosition(position = it, player = user))
    }
    emit(temp)
}

fun List<MapDetails>.getVictory() = this.maxByOrNull { it.warTrack.teamScore }?.takeIf { it.warTrack.displayedDiff.contains('+') }
fun List<MapDetails>.getDefeat() = this.minByOrNull { it.warTrack.teamScore }?.takeIf { it.warTrack.displayedDiff.contains('-') }