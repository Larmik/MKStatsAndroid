package fr.harmoniamk.statsmk.model.firebase

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class WarDispo(
    val dispoHour: Int,
    var dispoPlayers: List<PlayerDispo>,
    var opponentId: String? = null,
    var lineUp: List<String>? = null,
    var details: String? = null,
    var host: String? = null
): Parcelable {
    var lineupNames: List<String>? = null
    var opponentName: String? = null
    var hostName: String? = null
}

@Parcelize
data class PlayerDispo(
    var players: List<String>?,
    val dispo: Int
): Parcelable {
    var playerNames: List<String>? = null
}

enum class Dispo {
    CAN, CAN_SUB, NOT_SURE, CANT
}