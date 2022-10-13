package fr.harmoniamk.statsmk.model.local

import fr.harmoniamk.statsmk.enums.Maps
import fr.harmoniamk.statsmk.extension.*
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

class Stats(
    val warStats: WarStats,
    val mostPlayedTeam: TeamStats?,
    val warScores: List<WarScore>,
    val maps: List<TrackStats>,
    val averageForMaps: List<TrackStats>,
) {
     val highestScore: WarScore? = warScores.maxByOrNull { it.score }
     val lowestScore: WarScore? = warScores.minByOrNull { it.score }
     val bestMap: TrackStats? = averageForMaps.filter { it.totalPlayed >= 2 }.maxByOrNull { it.score ?: 0 }
     val worstMap: TrackStats? = averageForMaps.filter { it.totalPlayed >= 2 }.minByOrNull { it.score ?: 0 }
     val mostPlayedMap: TrackStats? = averageForMaps.maxByOrNull { it.totalPlayed }
     val averagePoints: Int = warScores.map { it.score }.sum() / warScores.count()
     val averagePointsLabel: String = averagePoints.warScoreToDiff()
     val averageMapPoints: Int = (maps.map { it.score }.sum() / maps.size)
     val averageMapPointsLabel = averageMapPoints.trackScoreToDiff()
     val averagePlayerMapPoints: Int = averageMapPoints.pointsToPosition()
 }

class WarScore(
    val war: MKWar,
    val score: Int
) {
    val opponentLabel = "vs ${war.name?.split('-')?.lastOrNull()?.trim()}"
}

data class TrackStats(
    val map: Maps? = null,
    val trackIndex: Int? = null,
    val score: Int? = null,
    val totalPlayed: Int = 0,
    val winRate: Int? = null
)

class TeamStats(val teamName: String?, totalPlayed: Int?) {
    val totalPlayedLabel = "$totalPlayed matchs joués"
}

class WarStats(list : List<MKWar>) {
    val warsPlayed = list.count()
    val warsWon = list.filterNot { war -> war.displayedDiff.contains('-') }.count()
    val warsTied = list.filter { war -> war.displayedDiff == "0" }.count()
    val warsLoss = list.filter { war -> war.displayedDiff.contains('-') }.count()
    val winRate = "${((warsWon*100) / warsPlayed)} %"
    val highestVictory = list.maxByOrNull { war -> war.scoreHost }.takeIf { it?.displayedDiff?.contains("+").isTrue }
    val loudestDefeat = list.minByOrNull { war -> war.scoreHost }.takeIf { it?.displayedDiff?.contains("-").isTrue }
}

class MapDetails(
    val war: MKWar,
    val warTrack: MKWarTrack
)

@FlowPreview
@ExperimentalCoroutinesApi
class MapStats(
    val list: List<MapDetails>,
    preferencesRepository: PreferencesRepositoryInterface
) {
    private val playerScoreList = list
        .filter { pair -> pair.war.hasPlayer(preferencesRepository.currentUser?.mid) }
        .mapNotNull { it.warTrack.track?.warPositions }
        .map { it.singleOrNull { it.playerId == preferencesRepository.currentUser?.mid } }
        .mapNotNull { it?.position.positionToPoints() }
    val trackPlayed = list.size
    val trackWon = list.filter { pair -> pair.warTrack.displayedDiff.contains('+')}.size
    val trackTie = list.filter { pair -> pair.warTrack.displayedDiff == "0" }.count()
    val trackLoss = list.filter { pair -> pair.warTrack.displayedDiff.contains('-') }.count()
    val winRate = "${(trackWon*100) / trackPlayed} %"
    val teamScore = list.map { pair -> pair.warTrack }.map { it.teamScore }.sum() / list.size
    val playerScore = playerScoreList.takeIf { it.isNotEmpty() }?.let {  (playerScoreList.sum() / playerScoreList.size).pointsToPosition() } ?: 0
    val highestVictory = list.getVictory()
    val loudestDefeat = list.getDefeat()

}