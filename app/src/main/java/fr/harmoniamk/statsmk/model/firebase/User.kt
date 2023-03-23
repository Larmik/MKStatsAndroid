package fr.harmoniamk.statsmk.model.firebase

import android.os.Parcelable
import fr.harmoniamk.statsmk.database.entities.UserEntity
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User(
    val mid: String,
    var name: String? = null,
    var team: String? = null,
    var currentWar: String? = null,
    var role: Int? = null,
    var picture: String? = null,
): Parcelable {

    constructor(entity: UserEntity) : this(
        mid = entity.mid,
        name = entity.name,
        team = entity.team,
        currentWar = entity.currentWar,
        role = entity.role,
        picture = entity.picture
    )

    fun toEntity() = UserEntity(
        mid, currentWar, name, role, team, picture
    )

}