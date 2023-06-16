package fr.harmoniamk.statsmk.model.firebase

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.io.Serializable

@Parcelize
data class NewWarTrack(
    val mid: String? = null,
    var trackIndex: Int? = null,
    var warPositions: List<NewWarPositions>? = null,
    var shocks: List<Shock>? = null
) : Serializable, Parcelable {
    companion object
}