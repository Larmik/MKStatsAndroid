package fr.harmoniamk.statsmk.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class MKCTeamEntity(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "founding_date") val founding_date: String?,
    @ColumnInfo(name = "team_name") val team_name: String?,
    @ColumnInfo(name = "team_tag") val team_tag: String?,
    @ColumnInfo(name = "team_color") val team_color: String?,
    @ColumnInfo(name = "recruitment_status") val recruitment_status: String?,
    @ColumnInfo(name = "team_status") val team_status: String?,
    @ColumnInfo(name = "is_shadow") val is_shadow: String?,
    @ColumnInfo(name = "player_count") val player_count: String?,
)