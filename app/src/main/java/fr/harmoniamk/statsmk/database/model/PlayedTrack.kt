package fr.harmoniamk.statsmk.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity
data class PlayedTrack(
    @PrimaryKey(autoGenerate = true) var mid: Int = 0,
    @ColumnInfo(name = "tmId") val tmId: Int? = null,
    @ColumnInfo(name = "trackIndex") val trackIndex: Int,
    @ColumnInfo(name = "position") val position: Int? = null,
    @ColumnInfo(name = "lap1time") val lap1time: Long? = null,
    @ColumnInfo(name = "lap2time") val lap2time: Long? = null,
    @ColumnInfo(name = "lap3time") val lap3time: Long? = null,
) : Serializable {
    @Ignore
    val points: Int? = when (position) {
        1 -> 15
        2 -> 12
        3 -> 10
        4 -> 9
        5 -> 8
        6 -> 7
        7 -> 6
        8 -> 5
        9 -> 4
        10 -> 3
        11 -> 2
        12 -> 1
        else -> null
    }

    @Ignore
    val isTimeTrial: Boolean =
        tmId == null &&
                position == null &&
                lap1time != null &&
                lap2time != null &&
                lap3time != null

    @Ignore
    val displayedPos = "$position."
}