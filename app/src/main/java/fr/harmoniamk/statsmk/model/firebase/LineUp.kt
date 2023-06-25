package fr.harmoniamk.statsmk.model.firebase

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class LineUp(val userId: String, val userDiscordId: String) : Parcelable {
}