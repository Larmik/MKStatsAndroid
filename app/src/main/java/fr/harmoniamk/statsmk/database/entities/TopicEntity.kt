package fr.harmoniamk.statsmk.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class TopicEntity(
    @PrimaryKey
    @ColumnInfo(name = "topic") val topic: String,
)