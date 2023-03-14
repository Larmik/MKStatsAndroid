package fr.harmoniamk.statsmk.fragment.stats.playerRanking

import android.os.Parcelable
import fr.harmoniamk.statsmk.model.firebase.User
import fr.harmoniamk.statsmk.model.local.Stats
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PlayerRankingItemViewModel(val user: User, val stats: Stats) : Parcelable {

    val playerName: String?
        get() = user.name
    val warsPlayedLabel: String
        get() = stats.warStats.warsPlayed.toString()
    val winrateLabel: String
        get() = "${(stats.warStats.warsWon*100)/stats.warStats.warsPlayed} %"
    val averageLabel: String
        get() = stats.averagePoints.toString()
    val picture: String?
        get() = user.picture
}