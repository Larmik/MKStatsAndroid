package fr.harmoniamk.statsmk.model.network

import androidx.annotation.Keep
import com.squareup.moshi.JsonClass
import fr.harmoniamk.statsmk.database.entities.MKCLightPlayerEntity

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
) {
    val flag = "https://www.mariokartcentral.com/mkc/images/flags/${country_code.lowercase()}.png"

    fun toEntity() = MKCLightPlayerEntity(
        player_id = player_id,
        display_name = display_name,
        custom_field_name = custom_field_name,
        custom_field = custom_field,
        player_status = player_status,
        registered_since = registered_since,
        registered_since_human = registered_since_human,
        country_code = country_code,
        country_name = country_name,
        team_leader = team_leader
    )

    constructor(entity: MKCLightPlayerEntity) : this(
        player_id = entity.player_id,
        display_name = entity.display_name.orEmpty(),
        custom_field_name = entity.custom_field_name.orEmpty(),
        custom_field = entity.custom_field.orEmpty(),
        player_status = entity.player_status.orEmpty(),
        registered_since = entity.registered_since.orEmpty(),
        registered_since_human = entity.registered_since_human.orEmpty(),
        country_code = entity.country_code.orEmpty(),
        country_name = entity.country_name.orEmpty(),
        team_leader = entity.team_leader.orEmpty()
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
    val discord_privacy: String,
    val discord_tag: String,
    val switch_fc: String,
    val nnid: String?,
    val fc_3ds: String?,
    val mktour_fc: String?,
    val profile_picture: String?,
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