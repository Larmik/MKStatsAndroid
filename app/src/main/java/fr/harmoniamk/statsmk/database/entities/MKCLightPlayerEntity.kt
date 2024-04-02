package fr.harmoniamk.statsmk.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class MKCLightPlayerEntity(
    @PrimaryKey
    @ColumnInfo(name = "mid") val mid: String,
    @ColumnInfo(name = "mkcId") val mkcId: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "fc") val fc: String,
    @ColumnInfo(name = "status") val status: String,
    @ColumnInfo(name = "registerDate") val registerDate: String,
    @ColumnInfo(name = "country") val country: String,
    @ColumnInfo(name = "isLeader") val isLeader: String,
    @ColumnInfo(name = "role") val role: Int,
    @ColumnInfo(name = "currentWar") val currentWar: String,
    @ColumnInfo(name = "picture") val picture: String,
    @ColumnInfo(name = "rosterId") val rosterId: String,
)