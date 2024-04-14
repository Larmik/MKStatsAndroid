package fr.harmoniamk.statsmk.model.firebase

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.io.Serializable

data class Shock(var playerId: String?, var count: Int): Serializable