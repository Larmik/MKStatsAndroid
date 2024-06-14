package fr.harmoniamk.statsmk.model.firebase

import fr.harmoniamk.statsmk.model.network.MKPlayer


data class User(
    val mid: String,
    var name: String? = null,
    var currentWar: String? = null,
    var role: Int? = null,
    var picture: String? = null,
    var mkcId: String? = null,
    var discordId: String?,
    var rosterId: String? = null
)  {

    constructor(player: MKPlayer?, user: User?) : this(
        mid = user?.mid ?: player?.mkcId.orEmpty(),
        name = user?.name ?: player?.name,
        currentWar = player?.currentWar,
        role = user?.role ?: player?.role,
        picture = user?.picture ?: player?.picture,
        mkcId = user?.mkcId ?: player?.mkcId,
        discordId = user?.discordId
    )

}