package fr.harmoniamk.statsmk.fragment.stats.opponentRanking

import android.os.Parcelable
import fr.harmoniamk.statsmk.model.firebase.Team
import fr.harmoniamk.statsmk.model.local.Stats
import kotlinx.android.parcel.Parcelize

@Parcelize
data class OpponentRankingItemViewModel(val team: Team?, val stats: Stats, val userId: String? = null) : Parcelable {


    val teamName: String?
        get() = team?.name
    val warsPlayedLabel: String
        get() = stats.warStats.warsPlayed.toString()
    val winrateLabel: String
        get() = "${(stats.warStats.warsWon*100)/stats.warStats.warsPlayed} %"

    val averageLabel: String
        get() = when (userId) {
            null -> stats.averagePointsLabel
            else -> stats.averagePoints.toString()
        }

    val averageMapLabel: String
        get() = when (userId) {
            null -> stats.averageMapPointsLabel
            else -> stats.averagePlayerMapPoints.toString()
        }



}