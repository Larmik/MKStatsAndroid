package fr.harmoniamk.statsmk.model.firebase

import android.os.Parcelable
import fr.harmoniamk.statsmk.database.entities.UserEntity
import fr.harmoniamk.statsmk.model.network.MKCLightPlayer
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User(
    val mid: String,
    var name: String? = null,
    var team: String? = null,
    var currentWar: String? = null,
    var role: Int? = null,
    var picture: String? = null,
    var allyTeams: List<String>? = null,
    var mkcId: String? = null
) : Parcelable {

    constructor(entity: UserEntity) : this(
        mid = entity.mid,
        name = entity.name,
        team = entity.team,
        currentWar = entity.currentWar,
        role = entity.role,
        picture = entity.picture,
        allyTeams = entity.allyTeams,
        mkcId = entity.mkcId
    )

    constructor(player: MKCLightPlayer?, team: String?) : this(
        mid = player?.player_id.orEmpty(),
        name = player?.display_name,
        team = team,
        currentWar = "-1",
        role = 0,
        picture = "",
        allyTeams = null,
        mkcId = player?.player_id
    )

    fun toEntity() = UserEntity(
        mid,
        currentWar,
        name,
        role,
        team,
        picture,
        allyTeams,
        mkcId
    )

}