package fr.harmoniamk.statsmk.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import fr.harmoniamk.statsmk.model.network.MKCFullTeam
import fr.harmoniamk.statsmk.model.network.SecondaryTeam

@Entity
class MKCRosterEntity(
    @PrimaryKey
    @ColumnInfo(name = "id") val teamId: String,
    @ColumnInfo(name = "primary_team_id") val primaryTeamId: Int?,
    @ColumnInfo(name = "primary_team_name") val primaryTeamName: String?,
    @ColumnInfo(name = "secondary_teams") val secondaryTeams: List<SecondaryTeam>?,
    @ColumnInfo(name = "founding_date") val foundingDate: String,
    @ColumnInfo(name = "team_category") val category: String,
    @ColumnInfo(name = "team_name") val name: String,
    @ColumnInfo(name = "team_tag") val tag: String,
    @ColumnInfo(name = "team_color") val color: Int,
    @ColumnInfo(name = "team_description") val description: String,
    @ColumnInfo(name = "team_logo") val logo: String,
    @ColumnInfo(name = "main_language") val language: String,
    @ColumnInfo(name = "recruitment_status") val recruitmentStatus: String,
    @ColumnInfo(name = "team_status") val status: String,
    @ColumnInfo(name = "is_historical") val historical: Int,
) {
    constructor(team: MKCFullTeam) : this(
        teamId = team.id,
        primaryTeamId = team.primary_team_id,
        primaryTeamName = team.primary_team_name,
        secondaryTeams = team.secondary_teams,
        foundingDate = team.founding_date.date,
        category = team.team_category,
        name = team.team_name,
        tag = team.team_tag,
        color = team.team_color,
        description = team.team_description,
        logo = team.team_logo,
        language = team.main_language,
        recruitmentStatus = team.recruitment_status,
        status = team.team_status,
        historical = team.is_historical
    )
}