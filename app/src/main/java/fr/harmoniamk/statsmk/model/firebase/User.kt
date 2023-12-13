package fr.harmoniamk.statsmk.model.firebase

import android.os.Parcelable
import fr.harmoniamk.statsmk.model.network.MKCLightPlayer
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User(
    val mid: String,
    var name: String? = null,
    var currentWar: String? = null,
    var role: Int? = null,
    var picture: String? = null,
    var mkcId: String? = null,
    var discordId: String?
) : Parcelable {


    constructor(player: MKCLightPlayer?, mid: String?, discordId: String?) : this(
        mid = mid ?: player?.mkcId.orEmpty(),
        name = player?.name,
        currentWar = player?.currentWar,
        role = player?.role,
        picture = player?.picture,
        mkcId = player?.mkcId,
        discordId = discordId
    )

}