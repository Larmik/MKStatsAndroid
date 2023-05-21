package fr.harmoniamk.statsmk.model.firebase

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class WarDispo(
    val dispoHour: String,
    val dispoPlayers: List<PlayerDispo>
): Parcelable

@Parcelize
data class PlayerDispo(
    val playerId: String,
    val dispo: Dispo
): Parcelable

enum class Dispo {
    CAN, CAN_SUB, NOT_SURE, CANT, NONE
}