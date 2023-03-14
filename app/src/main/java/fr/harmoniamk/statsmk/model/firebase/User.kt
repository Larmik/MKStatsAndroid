package fr.harmoniamk.statsmk.model.firebase

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User(
    val mid: String?,
    var name: String? = null,
    var team: String? = null,
    var currentWar: String? = null,
    var role: Int? = null,
    var picture: String
): Parcelable