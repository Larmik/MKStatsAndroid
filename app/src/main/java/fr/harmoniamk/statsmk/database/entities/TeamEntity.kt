package fr.harmoniamk.statsmk.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class TeamEntity(
    @PrimaryKey
    @ColumnInfo(name = "mid") val mid: String,
    @ColumnInfo(name = "name") val name: String?,
    @ColumnInfo(name = "shortName") val shortName: String?
)