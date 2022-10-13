package fr.harmoniamk.statsmk.extension

import fr.harmoniamk.statsmk.model.firebase.NewWarPositions
import fr.harmoniamk.statsmk.model.firebase.NewWarTrack
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

fun List<NewWarTrack>.sortBySize() = this.groupBy { it.trackIndex }.toList().sortedByDescending { it.second.size }
fun List<NewWarTrack>.sortByVictory() = this.groupBy { it.trackIndex }.toList().sortedByDescending { it.second.filter { MKWarTrack(it).displayedDiff.contains('+') }.size }
fun List<NewWarTrack>.sortByWinRate() = this.groupBy { it.trackIndex }.toList().sortedByDescending { it.second.filter { MKWarTrack(it).displayedDiff.contains('+') }.size * 100 / it.second.size }
fun List<NewWarTrack>.sortByAverage() = this.groupBy { it.trackIndex }.toList().sortedByDescending { it.second.map { MKWarTrack(it).diffScore }.sum() / it.second.size }




fun List<NewWarPositions>.withPlayerName(firebaseRepository: FirebaseRepositoryInterface) = flow {
    val temp = mutableListOf<MKWarPosition>()
    this@withPlayerName.forEach {
        val playerName = firebaseRepository.getUser(it.playerId).firstOrNull()?.name
        temp.add(MKWarPosition(position = it, playerName = playerName))
    }
    emit(temp)
}

fun List<MapDetails>.getVictory() = this.maxByOrNull { it.warTrack.teamScore }?.takeIf { it.warTrack.displayedDiff.contains('+') }
fun List<MapDetails>.getDefeat() = this.minByOrNull { it.warTrack.teamScore }?.takeIf { it.warTrack.displayedDiff.contains('-') }