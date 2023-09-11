package fr.harmoniamk.statsmk.fragment.stats.opponentRanking

import android.os.Parcelable
import fr.harmoniamk.statsmk.compose.RankingItemViewModel
import fr.harmoniamk.statsmk.model.firebase.Team
import fr.harmoniamk.statsmk.model.local.Stats
import kotlinx.android.parcel.Parcelize

@Parcelize
data class OpponentRankingItemViewModel(val team: Team?, override val stats: Stats, val userId: String? = null, val isIndiv: Boolean = false) : Parcelable, RankingItemViewModel {


    val teamName: String?
        get() = team?.name
    val warsPlayedLabel: String
        get() = stats.warStats.warsPlayed.toString()
    val winrateLabel: String
        get() = when (stats.warStats.warsPlayed) {
            0 -> "0 %"
            else -> "${(stats.warStats.warsWon*100)/stats.warStats.warsPlayed} %"
        }

    val averageLabel: String
        get() = when (userId != null && isIndiv) {
            true -> stats.averagePoints.toString()
            else -> stats.averagePointsLabel
        }

    val averageMapLabel: String
        get() = when (userId != null && isIndiv) {
            true -> stats.averagePlayerMapPoints.toString()
            else -> stats.averageMapPointsLabel
        }



}