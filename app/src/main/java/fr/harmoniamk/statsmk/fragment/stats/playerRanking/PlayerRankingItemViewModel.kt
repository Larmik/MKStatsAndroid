package fr.harmoniamk.statsmk.fragment.stats.playerRanking

import fr.harmoniamk.statsmk.model.local.RankingItemViewModel
import fr.harmoniamk.statsmk.model.local.Stats
import fr.harmoniamk.statsmk.model.network.MKPlayer

data class PlayerRankingItemViewModel(val user: MKPlayer, override val stats: Stats) : RankingItemViewModel {

    val warsPlayedLabel: String
        get() = stats.warStats.warsPlayed.toString()
    val winrateLabel: String
        get() = when (stats.warStats.warsPlayed) {
            0 -> "0 %"
            else -> "${(stats.warStats.warsWon*100)/stats.warStats.warsPlayed} %"
        }
    val averageLabel: String
        get() = stats.averagePoints.toString()
    val picture: String?
        get() = user.flag
}