package fr.harmoniamk.statsmk.model.firebase

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

data class Team(
    val mid: String,
    var name: String? = null,
    var shortName: String? = null,
)