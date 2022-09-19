package fr.harmoniamk.statsmk.extension

import fr.harmoniamk.statsmk.model.firebase.NewWarPositions
import fr.harmoniamk.statsmk.model.firebase.NewWarTrack
import fr.harmoniamk.statsmk.model.local.MKWar
import fr.harmoniamk.statsmk.model.local.MKWarPosition
import fr.harmoniamk.statsmk.model.local.MKWarTrack
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow

fun List<MKWar>.getLasts(teamId: String?) = this.filter {
        war -> war.isOver && war.war?.teamHost == teamId
}.sortedByDescending{ it.war?.createdDate?.formatToDate() }.safeSubList(0, 5)
fun List<MKWar>.getCurrent(teamId: String?) = this.singleOrNull { war -> !war.isOver && war.war?.teamHost == teamId }
fun List<MKWar>.getBests(teamId: String?) = this.filter { war -> war.isOver && war.war?.teamHost == teamId  }.sortedWith(compareBy<MKWar> { it.scoreHost }.thenBy { it.displayedAverage.toInt() }).reversed().safeSubList(0, 3)
fun List<MKWar>.withName(firebaseRepository: FirebaseRepositoryInterface) = flow {
    val temp = mutableListOf<MKWar>()
    this@withName.forEach {
        val hostName = firebaseRepository.getTeam(it.war?.teamHost).firstOrNull()?.shortName
        val opponentName = firebaseRepository.getTeam(it.war?.teamOpponent).firstOrNull()?.shortName
        temp.add(it.apply { this.name = "$hostName - $opponentName" })
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
fun List<NewWarTrack>.sortByVictory() = this.groupBy { it.trackIndex }.toList().sortedByDescending { it.second.filter { MKWarTrack(it).displayedDiff.contains('+') }.size * 100 / it.second.size }
fun List<NewWarTrack>.sortByDefeat() = this.groupBy { it.trackIndex }.toList().sortedByDescending { it.second.filter { MKWarTrack(it).displayedDiff.contains('-') }.size * 100 / it.second.size }

fun List<NewWarPositions>.withPlayerName(firebaseRepository: FirebaseRepositoryInterface) = flow {
    val temp = mutableListOf<MKWarPosition>()
    this@withPlayerName.forEach {
        val playerName = firebaseRepository.getUser(it.playerId).firstOrNull()?.name
        temp.add(MKWarPosition(position = it, playerName = playerName))
    }
    emit(temp)
}