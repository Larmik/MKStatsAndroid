package fr.harmoniamk.statsmk.model.network

import androidx.annotation.Keep
import com.squareup.moshi.JsonClass

@Keep
@JsonClass(generateAdapter = true)
data class MKCPlayerList(
    val first: Int,
    val last: Int,
    val total: Int,
    val page: Int,
    val hasMore: Boolean,
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
data class MKCLightPlayer(
    val player_id: String,
    val display_name: String,
    val custom_field_name: String,
    val custom_field: String,
    val player_status: String,
    val registered_since: String,
    val registered_since_human: String,
    val country_code: String,
    val country_name: String,
    val team_leader: String
)

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
    val discord_privacy: String,
    val discord_tag: String,
    val switch_fc: String,
    val nnid: String?,
    val fc_3ds: String?,
    val mktour_fc: String?,
    val profile_picture: String,
    val profile_picture_border_color: Int,
    val profile_message: String,
    val is_supporter: Boolean,
    val is_administrator: Boolean,
    val is_moderator: Boolean,
    val is_global_event_admin: Boolean,
    val is_global_event_mod: Boolean,
    val is_event_admin: Boolean,
    val is_event_mod: Boolean,
    val current_teams: List<MKCLightTeam>
)