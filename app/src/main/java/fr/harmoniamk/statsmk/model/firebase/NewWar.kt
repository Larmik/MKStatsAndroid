package fr.harmoniamk.statsmk.model.firebase

import android.os.Parcelable
import fr.harmoniamk.statsmk.database.entities.WarEntity
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue
import java.io.Serializable

@Parcelize
data class NewWar(
    val mid: String,
    val playerHostId: String? = null,
    val teamHost: String? = null,
    val teamOpponent: String? = null,
    val createdDate: String? = null,
    var warTracks: @RawValue List<NewWarTrack>? = null,
    var penalties:  @RawValue List<Penalty>? = null,
    val isOfficial: Boolean? = null
) : Serializable, Parcelable {

    companion object


    fun toEntity(name: String?) = WarEntity(
        mid = mid,
        entityName = name,
        playerHostId = playerHostId,
        teamHost = teamHost,
        teamOpponent = teamOpponent,
        createdDate = createdDate,
        warTracks = warTracks,
        penalties = penalties,
        isOfficial = isOfficial
    )

}