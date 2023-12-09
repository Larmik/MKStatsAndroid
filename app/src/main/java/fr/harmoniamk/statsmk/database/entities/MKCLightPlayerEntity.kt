package fr.harmoniamk.statsmk.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class MKCLightPlayerEntity(
    @PrimaryKey
    @ColumnInfo(name = "player_id") val player_id: String,
    @ColumnInfo(name = "display_name") val display_name: String?,
    @ColumnInfo(name = "custom_field_name") val custom_field_name: String?,
    @ColumnInfo(name = "custom_field") val custom_field: String?,
    @ColumnInfo(name = "player_status") val player_status: String?,
    @ColumnInfo(name = "registered_since") val registered_since: String?,
    @ColumnInfo(name = "registered_since_human") val registered_since_human: String?,
    @ColumnInfo(name = "country_code") val country_code: String?,
    @ColumnInfo(name = "country_name") val country_name: String?,
    @ColumnInfo(name = "team_leader") val team_leader: String?,
)