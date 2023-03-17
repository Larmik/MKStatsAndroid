package fr.harmoniamk.statsmk.model.local

import android.os.Parcelable
import fr.harmoniamk.statsmk.enums.Maps
import fr.harmoniamk.statsmk.extension.*
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@Parcelize
data class Stats(
    val warStats: WarStats,
    val mostPlayedTeam: TeamStats?,
    val mostDefeatedTeam: TeamStats?,
    val lessDefeatedTeam: TeamStats?,
    val warScores: List<WarScore>,
    val maps: List<TrackStats>,
    val averageForMaps: List<TrackStats>,
): Parcelable {
     val highestScore: WarScore? = warScores.maxByOrNull { it.score }
     val lowestScore: WarScore? = warScores.minByOrNull { it.score }
     val bestMap: TrackStats? = averageForMaps.filter { it.totalPlayed >= 2 }.maxByOrNull { it.teamScore ?: 0 }
     val worstMap: TrackStats? = averageForMaps.filter { it.totalPlayed >= 2 }.minByOrNull { it.teamScore ?: 0 }
     val mostPlayedMap: TrackStats? = averageForMaps.maxByOrNull { it.totalPlayed }
     val averagePoints: Int = warScores.map { it.score }.sum() / (warScores.takeIf { it.isNotEmpty() }?.size ?: 1)
     val averagePointsLabel: String = averagePoints.warScoreToDiff()
     private val averageMapPoints: Int = (maps.map { it.teamScore }.sum() / (maps.takeIf { it.isNotEmpty() }?.size ?: 1))
     private val averagePlayerPosition: Int = (maps.map { it.playerScore }.sum() / (maps.takeIf { it.isNotEmpty() }?.size ?: 1))
     val averageMapPointsLabel = averageMapPoints.trackScoreToDiff()
     val averagePlayerMapPoints: Int = averagePlayerPosition.pointsToPosition()
     val mapsWon = "${maps.filter { (it.teamScore ?: 0) < 41 }.size} / ${maps.size}"
     val shockCount = maps.map { it.shockCount }.sum()
 }

@Parcelize
class WarScore(
    val war: MKWar,
    val score: Int
): Parcelable {
    val opponentLabel = "vs ${war.name?.split('-')?.lastOrNull()?.trim()}"
}

@Parcelize
data class TrackStats(
    val map: Maps? = null,
    val trackIndex: Int? = null,
    val teamScore: Int? = null,
    val playerScore: Int? = null,
    val totalPlayed: Int = 0,
    val winRate: Int? = null,
    val shockCount: Int? = null
): Parcelable

@Parcelize
class TeamStats(val teamName: String?, val totalPlayed: Int?): Parcelable {
    val totalPlayedLabel = "$totalPlayed matchs joués"
}

@Parcelize
class WarStats(val list : List<MKWar>): Parcelable {
    val warsPlayed = list.count()
    val warsWon = list.filter{ war -> war.displayedDiff.contains('+') }.count()
    val warsTied = list.filter { war -> war.displayedDiff == "0" }.count()
    val warsLoss = list.filter { war -> war.displayedDiff.contains('-') }.count()
    val highestVictory = list.maxByOrNull { war -> war.scoreHost }.takeIf { it?.displayedDiff?.contains("+").isTrue }
    val loudestDefeat = list.minByOrNull { war -> war.scoreHost }.takeIf { it?.displayedDiff?.contains("-").isTrue }
}

@Parcelize
class MapDetails(
    val war: MKWar,
    val warTrack: MKWarTrack,
    val position: Int?
): Parcelable

@FlowPreview
@ExperimentalCoroutinesApi
class MapStats(
    val list: List<MapDetails>,
    private val isIndiv: Boolean,
    userId: String? = null
) {
    private val playerScoreList = list
        .filter { pair -> pair.war.war?.warTracks?.any { MKWarTrack(it).hasPlayer(userId) }.isTrue }
        .mapNotNull { it.warTrack.track?.warPositions }
        .map { it.singleOrNull { it.playerId == userId } }
        .mapNotNull { it?.position.positionToPoints() }
    val trackPlayed = list.filter { !isIndiv || (isIndiv && it.war.war?.warTracks?.any { MKWarTrack(it).hasPlayer(userId) }.isTrue) }.size
    val trackWon = list
        .filter { pair -> pair.warTrack.displayedDiff.contains('+')}
        .filter { !isIndiv || (isIndiv && it.war.war?.warTracks?.any { MKWarTrack(it).hasPlayer(userId) }.isTrue) }
        .size
    val trackTie = list
        .filter { pair -> pair.warTrack.displayedDiff == "0" }
        .filter { !isIndiv || (isIndiv && it.war.war?.warTracks?.any { MKWarTrack(it).hasPlayer(userId) }.isTrue) }
        .count()
    val trackLoss = list
        .filter { pair -> pair.warTrack.displayedDiff.contains('-') }
        .filter { !isIndiv || (isIndiv && it.war.war?.warTracks?.any { MKWarTrack(it).hasPlayer(userId) }.isTrue) }
        .count()
    val teamScore = list.map { pair -> pair.warTrack }.map { it.teamScore }.sum() / list.size
    val playerScore = playerScoreList.takeIf { it.isNotEmpty() }?.let {  (playerScoreList.sum() / playerScoreList.size).pointsToPosition() } ?: 0
    val highestVictory = list.filter { !isIndiv || (isIndiv && it.war.war?.warTracks?.any { MKWarTrack(it).hasPlayer(userId) }.isTrue) }.getVictory()
    val loudestDefeat = list.filter { !isIndiv || (isIndiv && it.war.hasPlayer(userId)) }.getDefeat()
    val shockCount = list.map { it.warTrack.track?.shocks?.filter { !isIndiv || (isIndiv && it.playerId == userId) }?.map { it.count }.sum() }.sum()

}