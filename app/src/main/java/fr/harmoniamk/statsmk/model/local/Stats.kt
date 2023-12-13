package fr.harmoniamk.statsmk.model.local

import android.os.Parcelable
import fr.harmoniamk.statsmk.enums.Maps
import fr.harmoniamk.statsmk.extension.*
import fr.harmoniamk.statsmk.model.network.MKCTeam
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import java.util.Calendar
import java.util.Date

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
    val averagePlayerPosition: Int = (maps.map { it.playerScore }.sum() / (maps.takeIf { it.isNotEmpty() }?.size ?: 1)).pointsToPosition()
     val averageMapPointsLabel = averageMapPoints.trackScoreToDiff()
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
class TeamStats(val team: MKCTeam?, val totalPlayed: Int?): Parcelable {
    val teamName = team?.team_name
}

@Parcelize
class WarStats(val list : List<MKWar>): Parcelable {
    private val shockFeatureDate = Date().set(Calendar.MONTH, 3).set(Calendar.DAY_OF_MONTH, 22).set(Calendar.YEAR, 2023)
    val warsPlayed = list.count()
    val warsPlayedSinceShocks = list.filter { it.war?.createdDate?.formatToDate("dd/MM/yyyy - HH'h'mm")?.after(shockFeatureDate).isTrue }.size
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
    val playerPosition = playerScoreList.takeIf { it.isNotEmpty() }?.let {  (playerScoreList.sum() / playerScoreList.size).pointsToPosition() } ?: 0
    val topsTable = listOf(
        Pair("Top 6", list.filter { !isIndiv &&  it.warTrack.track?.warPositions?.mapNotNull { it.position }?.filter { it <= 6 }?.size == 6 }.size),
        Pair("Top 5", list.filter { !isIndiv && it.warTrack.track?.warPositions?.mapNotNull { it.position }?.filter { it <= 5 }?.size == 5 }.size),
        Pair("Top 4", list.filter { !isIndiv && it.warTrack.track?.warPositions?.mapNotNull { it.position }?.filter { it <= 4 }?.size == 4 }.size),
        Pair("Top 3", list.filter { !isIndiv && it.warTrack.track?.warPositions?.mapNotNull { it.position }?.filter { it <= 3 }?.size == 3 }.size),
        Pair("Top 2", list.filter { !isIndiv && it.warTrack.track?.warPositions?.mapNotNull { it.position }?.filter { it <= 2 }?.size == 2 }.size),
    )
    val bottomsTable = listOf(
        Pair("Bot 6", list.filter { !isIndiv && it.warTrack.track?.warPositions?.mapNotNull { it.position }?.filter { it >= 7 }?.size == 6 }.size),
        Pair("Bot 5", list.filter { !isIndiv && it.warTrack.track?.warPositions?.mapNotNull { it.position }?.filter { it >= 8 }?.size == 5 }.size),
        Pair("Bot 4", list.filter { !isIndiv && it.warTrack.track?.warPositions?.mapNotNull { it.position }?.filter { it >= 9  }?.size == 4 }.size),
        Pair("Bot 3", list.filter { !isIndiv && it.warTrack.track?.warPositions?.mapNotNull { it.position }?.filter { it >= 10 }?.size == 3 }.size),
        Pair("Bot 2", list.filter { !isIndiv && it.warTrack.track?.warPositions?.mapNotNull { it.position }?.filter { it >= 11 }?.size == 2 }.size),
    )

    val indivTopsTable = listOf(
        Pair("1", list.filter { isIndiv && it.warTrack.track?.warPositions?.singleOrNull { it.position == 1 }?.playerId == userId }.size),
        Pair("2", list.filter {isIndiv && it.warTrack.track?.warPositions?.singleOrNull { it.position == 2 }?.playerId == userId}.size),
        Pair("3", list.filter { isIndiv && it.warTrack.track?.warPositions?.singleOrNull { it.position == 3 }?.playerId == userId }.size),
        Pair("4", list.filter { isIndiv && it.warTrack.track?.warPositions?.singleOrNull { it.position == 4 }?.playerId == userId }.size),
        Pair("5", list.filter { isIndiv && it.warTrack.track?.warPositions?.singleOrNull { it.position == 5 }?.playerId == userId }.size),
        Pair("6", list.filter { isIndiv && it.warTrack.track?.warPositions?.singleOrNull { it.position == 6 }?.playerId == userId }.size),
    )
    val indivBottomsTable = listOf(
        Pair("7", list.filter { isIndiv && it.warTrack.track?.warPositions?.singleOrNull { it.position == 7 }?.playerId == userId }.size),
        Pair("8", list.filter { isIndiv && it.warTrack.track?.warPositions?.singleOrNull { it.position == 8 }?.playerId == userId }.size),
        Pair("9", list.filter { isIndiv && it.warTrack.track?.warPositions?.singleOrNull { it.position == 9 }?.playerId == userId }.size),
        Pair("10", list.filter { isIndiv && it.warTrack.track?.warPositions?.singleOrNull { it.position == 10 }?.playerId == userId }.size),
        Pair("11", list.filter { isIndiv && it.warTrack.track?.warPositions?.singleOrNull { it.position == 11 }?.playerId == userId }.size),
        Pair("12", list.filter { isIndiv && it.warTrack.track?.warPositions?.singleOrNull { it.position == 12 }?.playerId == userId }.size),
    )
    val shockCount = list.map { it.warTrack.track?.shocks?.filter { !isIndiv || (isIndiv && it.playerId == userId) }?.map { it.count }.sum() }.sum()

}