package fr.harmoniamk.statsmk.model.firebase

import android.os.Parcelable
import com.google.firebase.auth.FirebaseUser
import fr.harmoniamk.statsmk.model.network.MKPlayer
import kotlinx.android.parcel.Parcelize


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

    constructor(player: MKPlayer?, mid: String?, discordId: String?) : this(
        mid = mid ?: player?.mkcId.orEmpty(),
        name = player?.name,
        currentWar = player?.currentWar,
        role = player?.role,
        picture = player?.picture,
        mkcId = player?.mkcId,
        discordId = discordId
    )

    constructor(user: FirebaseUser, picture: String) : this(
        mid = user.uid,
        name = user.displayName,
        currentWar = "-1",
        role = 0,
        picture = picture,
        mkcId = user.uid,
        discordId = null
    )

}