package fr.harmoniamk.statsmk.model.firebase

import android.os.Parcelable
import fr.harmoniamk.statsmk.database.entities.TeamEntity
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Team(
    val mid: String,
    var name: String? = null,
    var shortName: String? = null,
    var hasLeader: Boolean? = null,
    var picture: String? = null
): Parcelable {
    constructor(entity: TeamEntity) : this(entity.mid, entity.name, entity.shortName)

    fun toEntity() = TeamEntity(this.mid, this.name, this.shortName)
}