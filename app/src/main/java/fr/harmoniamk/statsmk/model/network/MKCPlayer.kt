package fr.harmoniamk.statsmk.model.network

import android.os.Parcelable
import androidx.annotation.Keep
import com.squareup.moshi.JsonClass
import fr.harmoniamk.statsmk.database.entities.MKCLightPlayerEntity
import fr.harmoniamk.statsmk.extension.displayedString
import fr.harmoniamk.statsmk.extension.formatToDate
import fr.harmoniamk.statsmk.model.firebase.User
import kotlinx.android.parcel.Parcelize

@Keep
@JsonClass(generateAdapter = true)
data class MKCPlayerList(
    val count: Int,
    val data: List<MKCPlayer>
)

@Keep
@JsonClass(generateAdapter = true)
data class MKCPlayer(
    val player_id: Int,
    val user_id: Int,
    val display_name: String,
    val custom_field: String?,
    val custom_field_name: String?,
    val switch_fc: String?,
    val nnid: String?,
    val fc_3ds: String?,
    val mktour_fc: String?,
    val player_status: String,
    val registered_at: String,
    val registered_at_human: String,
    val team_registered_at: String?,
    val team_registered_at_human: String?,
    val country_code: String,
    val country_name: String
)

@Keep
@JsonClass(generateAdapter = true)
data class MKCDate(
    val date: String, //"2020-06-03 13:22:52.000000"
    val timezone_type: Int,
    val timezone: String // "UTC"
)

@Keep
@JsonClass(generateAdapter = true)
@Parcelize
data class MKCLightPlayer(
    val mid: String,
    val mkcId: String,
    val name: String,
    val fc: String,
    val status: String,
    val registerDate: String,
    val country: String,
    val isLeader: String,
    val role: Int,
    var currentWar: String,
    val picture: String,
    val isAlly: Int
) : Parcelable {
    val flag = "https://www.mariokartcentral.com/mkc/images/flags/${country.lowercase()}.png"

    fun toEntity() = MKCLightPlayerEntity(
        mid, mkcId, name, fc, status, registerDate, country, isLeader, role, currentWar, picture, isAlly
    )

    constructor(user: User?) : this(
        mid = user?.mid.orEmpty(),
        mkcId = user?.mkcId.orEmpty(),
        name = user?.name.orEmpty(),
        fc = "",
        status = "",
        registerDate = "",
        country = "",
        isLeader = "",
        role = user?.role ?: 0,
        currentWar = user?.currentWar.orEmpty(),
        picture = user?.picture.orEmpty(),
        isAlly = 1
    )
    constructor(fullPlayer: MKCFullPlayer?) : this(
        mid = fullPlayer?.id.toString(),
        mkcId = fullPlayer?.id.toString(),
        name = fullPlayer?.display_name.orEmpty(),
        fc = fullPlayer?.switch_fc.orEmpty(),
        status = fullPlayer?.player_status.orEmpty(),
        registerDate = fullPlayer?.registered_at?.date.orEmpty(),
        country = fullPlayer?.country_code.orEmpty(),
        isLeader = "",
        role = 0,
        currentWar = "-1",
        picture = fullPlayer?.profile_picture.orEmpty(),
        isAlly = 1
    )

    constructor(entity: MKCLightPlayerEntity?) : this(
        mid = entity?.mid.orEmpty(),
        mkcId = entity?.mkcId.orEmpty(),
        name = entity?.name.orEmpty(),
        fc = entity?.fc.orEmpty(),
        status = entity?.status.orEmpty(),
        registerDate = entity?.registerDate.orEmpty(),
        country = entity?.country.orEmpty(),
        isLeader = entity?.isLeader.orEmpty(),
        role = entity?.role ?: 0,
        currentWar = entity?.currentWar.orEmpty(),
        picture = entity?.picture.orEmpty(),
        isAlly = entity?.isAlly ?: 0
    )

    constructor(player: MKCPlayer) : this (
        mid = player.player_id.toString(),
        mkcId = player.player_id.toString(),
        name = player.display_name,
        fc = player.switch_fc.orEmpty(),
        status = player.player_status,
        registerDate = player.registered_at,
        country = player.country_code,
        isLeader =  "0",
        role =  0,
        currentWar = "-1",
        picture = "",
        isAlly = 0
    )

    constructor(player: MKCFullPlayer?, role: Int, isAlly: Int, isLeader: String, currentWar: String) : this(
        mid = player?.id.toString(),
        mkcId = player?.id.toString(),
        name = player?.display_name.orEmpty(),
        fc = player?.switch_fc.orEmpty(),
        status = player?.player_status.orEmpty(),
        registerDate = player?.registered_at?.date.orEmpty(),
        country = player?.country_code.orEmpty(),
        isLeader = isLeader,
        role = role,
        currentWar = currentWar,
        picture = player?.profile_picture.orEmpty(),
        isAlly = isAlly
    )

    constructor(player: MKCLightPlayer, role: Int?, picture: String?) : this(
        mid = player.mid,
        mkcId = player.mkcId,
        name = player.name,
        fc = player.fc,
        status = player.status,
        registerDate = player.registerDate,
        country = player.country,
        isLeader = player.isLeader,
        role = role ?: 0,
        currentWar = player.currentWar,
        picture = picture.orEmpty(),
        isAlly = player.isAlly
    )
}

@Keep
@JsonClass(generateAdapter = true)
data class MKCFullPlayer(
    val id: Int,
    val user_id: Int,
    val registered_at: MKCDate,
    val registered_at_human: String,
    val display_name: String,
    val player_status: String,
    val is_banned: Boolean,
    val ban_reason: String?,
    val is_hidden: Int,
    val country_code: String,
    val country_name: String,
    val region: String?,
    val city: String?,
    val discord_privacy: String?,
    val discord_tag: String?,
    val switch_fc: String?,
    val nnid: String?,
    val fc_3ds: String?,
    val mktour_fc: String?,
    val profile_picture: String?,
    val profile_picture_border_color: Int,
    val profile_message: String?,
    val is_supporter: Boolean,
    val is_administrator: Boolean,
    val is_moderator: Boolean,
    val is_global_event_admin: Boolean,
    val is_global_event_mod: Boolean,
    val is_event_admin: Boolean,
    val is_event_mod: Boolean,
    val current_teams: List<MKCLightTeam>
) {
    val createdDate = registered_at.date.split(".").first().formatToDate("yyyy-MM-dd HH:mm:ss")
        ?.displayedString("dd MMMM yyyy")
    val flag = "https://www.mariokartcentral.com/mkc/images/flags/${country_code.lowercase()}.png"
}