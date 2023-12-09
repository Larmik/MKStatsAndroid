package fr.harmoniamk.statsmk.model.network

import androidx.annotation.Keep
import com.squareup.moshi.JsonClass
import fr.harmoniamk.statsmk.database.entities.MKCTeamEntity
import fr.harmoniamk.statsmk.extension.displayedString
import fr.harmoniamk.statsmk.extension.formatToDate
import fr.harmoniamk.statsmk.extension.parseRoster

@Keep
@JsonClass(generateAdapter = true)
data class MKCTeamResponse(
    val count: Int,
    val data: List<MKCTeam>
)

@Keep
@JsonClass(generateAdapter = true)
data class MKCTeam(
    val team_id: Int,
    val team_name: String,
    val team_tag: String,
    val team_color: Int,
    val team_status: String,
    val recruitment_status: String,
    val is_shadow: Int,
    val player_count: Int,
    val founding_date: String,
    val founding_date_human: String
) {
    fun toEntity() = MKCTeamEntity(
        id = team_id.toString(),
        team_name = team_name,
        team_tag = team_tag,
        team_color = team_color.toString(),
        team_status = team_status,
        recruitment_status = recruitment_status,
        is_shadow = is_shadow.toString(),
        founding_date = founding_date,
        player_count = player_count.toString()

    )

    constructor(entity: MKCTeamEntity) : this(
        team_id = entity.id.toInt(),
        team_name = entity.team_name.orEmpty(),
        team_tag = entity.team_tag.orEmpty(),
        team_color = entity.team_color?.toInt() ?: 0,
        team_status = entity.team_status.orEmpty(),
        recruitment_status = entity.recruitment_status.orEmpty(),
        is_shadow = entity.is_shadow?.toInt() ?: 0,
        player_count = entity.player_count?.toInt() ?: 0,
        founding_date = entity.founding_date.orEmpty(),
        founding_date_human = entity.founding_date.orEmpty()
    )
}

@Keep
@JsonClass(generateAdapter = true)
data class MKCLightTeam(
    val mode_title: String,
    val mode_key: String,
    val mode: String,
    val team_id: Int,
    val team_name: String,
    val team_tag: String,
    val team_status: String,
)

@Suppress("UNCHECKED_CAST")
@Keep
@JsonClass(generateAdapter = true)
data class MKCFullTeam(
    val id: Int,
    val primary_team_id: Int?,
    val primary_team_name: String?,
    val secondary_teams: List<String>?,
    val founding_date: MKCDate,
    val founding_date_human: String,
    val team_category: String,
    val team_name: String,
    val team_tag: String,
    val team_color: Int,
    val team_description: String,
    val team_logo: String,
    val main_language: String,
    val recruitment_status: String,
    val team_status: String,
    val is_historical: Int,
    val rosters: Any
) {

    val logoUrl = "https://www.mariokartcentral.com/mkc/storage/$team_logo"
    val rosterList =
        (((rosters as? Map<*, *>)?.get("150cc") as? Map<*, *>)?.get("members") as? List<Map<*, *>>).parseRoster()
    val createdDate = founding_date.date.split(".").first().formatToDate("yyyy-MM-dd HH:mm:ss")
        ?.displayedString("dd MMMM yyyy")

}