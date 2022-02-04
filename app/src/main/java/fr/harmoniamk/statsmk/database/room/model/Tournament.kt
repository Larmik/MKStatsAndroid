package fr.harmoniamk.statsmk.database.room.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity
data class Tournament(
    @PrimaryKey(autoGenerate = true) var mid: Int = 0,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "trackCount") val trackCount: Int? = 0,
    @ColumnInfo(name = "trackPlayed") val trackPlayed: Int? = 0,
    @ColumnInfo(name = "difficulty") val difficulty: Int,
    @ColumnInfo(name = "points") val points: Int = 0,
    @ColumnInfo(name = "tops") val tops: Int = 0,
    @ColumnInfo(name = "created_date") val createdDate: String,
    @ColumnInfo(name = "updated_date") val updatedDate: String
) : Serializable {
    @Ignore
    val parsedCC = when (difficulty) {
        1 -> "150CC"
        2 -> "Miroir"
        else -> "200CC"
    }

    @Ignore
    val isOver = trackPlayed == trackCount

    @Ignore
    val infos = "${trackCount} courses - $parsedCC"

    @Ignore
    val ratio: Int? = trackCount?.let {
        points / it
    }
    @Ignore
    val displayedScore: String = "Score: $points"

    fun displayedRemaining(played: Int): String = "Courses jouées: $played/$trackCount"

    fun displayedState(played: Int): String = if (played == trackCount) "Tournoi terminé" else "Tournoi en cours (${played}/${trackCount})"

}