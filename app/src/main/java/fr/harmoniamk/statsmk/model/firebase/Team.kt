package fr.harmoniamk.statsmk.model.firebase

import android.os.Parcelable
import fr.harmoniamk.statsmk.database.entities.TeamEntity
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Team(
    val mid: String,
    var name: String? = null,
    var shortName: String? = null,
    var mkcId: String? = null
): Parcelable {
    constructor(entity: TeamEntity) : this(entity.mid, entity.name, entity.shortName, entity.mkcId)
    fun toEntity() = TeamEntity(this.mid, this.name, this.shortName, this.mkcId)
}