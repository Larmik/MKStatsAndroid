package fr.harmoniamk.statsmk.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import fr.harmoniamk.statsmk.model.firebase.NewWar
import fr.harmoniamk.statsmk.model.firebase.NewWarTrack
import fr.harmoniamk.statsmk.model.firebase.Penalty
import fr.harmoniamk.statsmk.model.local.MKWar

@Entity
data class WarEntity(
    @PrimaryKey
    @ColumnInfo(name = "mid") val mid: String,
    @ColumnInfo(name = "name") val entityName: String?,
    @ColumnInfo(name = "playerHostId") val playerHostId: String?,
    @ColumnInfo(name = "teamHost") val teamHost: String?,
    @ColumnInfo(name = "teamOpponent") val teamOpponent: String?,
    @ColumnInfo(name = "createdDate") val createdDate: String?,
    @ColumnInfo(name = "warTracks") val warTracks: List<NewWarTrack>?,
    @ColumnInfo(name = "penalties") val penalties: List<Penalty>?,
    @ColumnInfo(name = "isOfficial") val isOfficial: Boolean?
) {

    fun toMKWar(): MKWar {
        val war = NewWar(
            mid = mid,
            playerHostId = playerHostId,
            teamHost = teamHost,
            teamOpponent = teamOpponent,
            createdDate = createdDate,
            warTracks = warTracks,
            penalties = penalties,
            isOfficial = isOfficial
        )
        return MKWar(war).apply { this.name = entityName }
    }
}