package fr.harmoniamk.statsmk.fragment.stats.opponentRanking

import android.os.Parcelable
import fr.harmoniamk.statsmk.model.local.RankingItemViewModel
import fr.harmoniamk.statsmk.model.local.Stats
import fr.harmoniamk.statsmk.model.network.MKCTeam
import kotlinx.android.parcel.Parcelize

@Parcelize
data class OpponentRankingItemViewModel(val team: MKCTeam?, override val stats: Stats, val userId: String? = null) : Parcelable, RankingItemViewModel {


    val warsPlayedLabel: String
        get() = stats.warStats.warsPlayed.toString()
    val winrateLabel: String
        get() = when (stats.warStats.warsPlayed) {
            0 -> "0 %"
            else -> "${(stats.warStats.warsWon*100)/stats.warStats.warsPlayed} %"
        }

    val averageLabel: String
        get() = when (userId != null) {
            true -> stats.averagePoints.toString()
            else -> stats.averagePointsLabel
        }

    val averageMapLabel: String
        get() = when (userId != null) {
            true -> stats.averagePlayerPosition.toString()
            else -> stats.averageMapPointsLabel
        }



}