package fr.harmoniamk.statsmk.model.local

import android.os.Parcelable
import fr.harmoniamk.statsmk.compose.RankingItemViewModel
import fr.harmoniamk.statsmk.enums.Maps
import fr.harmoniamk.statsmk.extension.*
import fr.harmoniamk.statsmk.model.firebase.Team
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

interface MKStats

@Parcelize
data class Stats(
    val warStats: WarStats,
    val mostPlayedTeam: TeamStats?,
    val mostDefeatedTeam: TeamStats?,
    val lessDefeatedTeam: TeamStats?,
    val warScores: List<WarScore>,
    val maps: List<TrackStats>,
    val averageForMaps: List<TrackStats>,
): Parcelable, MKStats {
     val highestScore: WarScore? = warScores.maxByOrNull { it.score }
     val lowestScore: WarScore? = warScores.minByOrNull { it.score }
     val bestMap: TrackStats? = averageForMaps.filter { it.totalPlayed >= 2 }.maxByOrNull { it.teamScore ?: 0 }
     val worstMap: TrackStats? = averageForMaps.filter { it.totalPlayed >= 2 }.minByOrNull { it.teamScore ?: 0 }
     val bestPlayerMap: TrackStats? = averageForMaps.filter { it.totalPlayed >= 2 }.maxByOrNull { it.playerScore ?: 0 }
     val worstPlayerMap: TrackStats? = averageForMaps.filter { it.totalPlayed >= 2 }.minByOrNull { it.playerScore ?: 0 }
     val mostPlayedMap: TrackStats? = averageForMaps.filter { it.totalPlayed >= 2 }.maxByOrNull { it.totalPlayed }
     val averagePoints: Int = warScores.map { it.score }.sum() / (warScores.takeIf { it.isNotEmpty() }?.size ?: 1)
     val averagePointsLabel: String = averagePoints.warScoreToDiff()
     private val averageMapPoints: Int = (maps.map { it.teamScore }.sum() / (maps.takeIf { it.isNotEmpty() }?.size ?: 1))
     private val averagePlayerPosition: Int = (maps.map { it.playerScore }.sum() / (maps.takeIf { it.isNotEmpty() }?.size ?: 1))
     val averageMapPointsLabel = averageMapPoints.trackScoreToDiff()
     val averagePlayerMapPoints: Int = averagePlayerPosition.pointsToPosition()
     val mapsWon = "${maps.filter { (it.teamScore ?: 0) > 41 }.size} / ${maps.size}"
     val shockCount = maps.map { it.shockCount }.sum()
    var highestPlayerScore: Pair<Int, String?>? = null
    var lowestPlayerScore: Pair<Int, String?>? = null
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
    override val stats: Stats? = null,
    val map: Maps? = null,
    val trackIndex: Int? = null,
    val teamScore: Int? = null,
    val playerScore: Int? = null,
    val totalPlayed: Int = 0,
    val winRate: Int? = null,
    val shockCount: Int? = null
): Parcelable, RankingItemViewModel

@Parcelize
class TeamStats(val team: Team?, val totalPlayed: Int?): Parcelable {
    val teamName = team?.name
}

@Parcelize
class WarStats(val list : List<MKWar>): Parcelable {
    val warsPlayed = list.count()
    val warsWon = list.count { war -> war.displayedDiff.contains('+') }
    val warsTied = list.count { war -> war.displayedDiff == "0" }
    val warsLoss = list.count { war -> war.displayedDiff.contains('-') }
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
    val userId: String? = null
) : MKStats {
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
        .filter { pair -> pair.warTrack.displayedDiff == "0" }.count {
            !isIndiv || (isIndiv && it.war.war?.warTracks?.any {
                MKWarTrack(it).hasPlayer(userId)
            }.isTrue)
        }
    val trackLoss = list
        .filter { pair -> pair.warTrack.displayedDiff.contains('-') }.count {
            !isIndiv || (isIndiv && it.war.war?.warTracks?.any {
                MKWarTrack(it).hasPlayer(userId)
            }.isTrue)
        }
    val teamScore = list.map { pair -> pair.warTrack }.map { it.teamScore }.sum() / list.size
    val playerScore = playerScoreList.takeIf { it.isNotEmpty() }?.let {  (playerScoreList.sum() / playerScoreList.size).pointsToPosition() } ?: 0
    val highestVictory = list.filter { !isIndiv || (isIndiv && it.war.war?.warTracks?.any { MKWarTrack(it).hasPlayer(userId) }.isTrue) }.getVictory()
    val loudestDefeat = list.filter { !isIndiv || (isIndiv && it.war.hasPlayer(userId)) }.getDefeat()
    val shockCount = list.map { it.warTrack.track?.shocks?.filter { !isIndiv || (isIndiv && it.playerId == userId) }?.map { it.count }.sum() }.sum()

}