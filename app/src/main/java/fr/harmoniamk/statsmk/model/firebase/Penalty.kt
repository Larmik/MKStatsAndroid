package fr.harmoniamk.statsmk.model.firebase

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.io.Serializable

@Parcelize
data class Penalty(val teamId: String, val amount: Int): Parcelable, Serializable {
    var teamName: String? = null
}