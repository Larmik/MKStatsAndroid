package fr.harmoniamk.statsmk.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class UserEntity
    (
    @PrimaryKey
    @ColumnInfo(name = "mid") val mid: String,
    @ColumnInfo(name = "currentWar") val currentWar: String?,
    @ColumnInfo(name = "name") val name: String?,
    @ColumnInfo(name = "role") val role: Int?,
    @ColumnInfo(name = "team") val team: String?,
    @ColumnInfo(name = "picture") val picture: String?,
    @ColumnInfo(name = "formerTeams") val formerTeams: List<String>?,
    @ColumnInfo(name = "friendCode") val friendCode: String?
    )
