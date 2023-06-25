package fr.harmoniamk.statsmk.model.firebase

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class WarDispo(
    val dispoHour: Int,
    var dispoPlayers: List<PlayerDispo>? = null,
    var opponentId: String? = null,
    var lineUp: List<LineUp>? = null,
    var details: String? = null,
    var host: String? = null
): Parcelable {
    var lineupNames: List<String>? = null
    var opponentName: String? = null
    var hostName: String? = null

    override fun toString(): String {
        return "WarDispo($dispoHour, $dispoPlayers,$opponentId, $lineUp, $details, $host)"
    }
}

@Parcelize
data class PlayerDispo(
    var players: List<String>?,
    val dispo: Int
): Parcelable {
    var playerNames: List<String>? = null
    override fun toString(): String {
        return "PlayerDispo($players, $dispo)"
    }
}

enum class Dispo {
    CAN, CAN_SUB, NOT_SURE, CANT
}