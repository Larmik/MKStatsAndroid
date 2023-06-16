package fr.harmoniamk.statsmk.model.firebase

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.io.Serializable

@Parcelize
data class NewWarPositions(
    val mid: String? = null,
    var playerId: String? = null,
    val position: Int? = null
): Serializable, Parcelable {
    companion object
}