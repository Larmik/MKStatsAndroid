package fr.harmoniamk.statsmk.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import fr.harmoniamk.statsmk.database.converters.*
import fr.harmoniamk.statsmk.database.dao.MKCLightPlayerDao
import fr.harmoniamk.statsmk.database.dao.MKCTeamDao
import fr.harmoniamk.statsmk.database.dao.TopicDao
import fr.harmoniamk.statsmk.database.dao.WarDao
import fr.harmoniamk.statsmk.database.entities.MKCLightPlayerEntity
import fr.harmoniamk.statsmk.database.entities.MKCTeamEntity
import fr.harmoniamk.statsmk.database.entities.TopicEntity
import fr.harmoniamk.statsmk.database.entities.WarEntity
import kotlinx.coroutines.FlowPreview

@TypeConverters(value = [WarTrackConverter::class, WarPositionConverter::class, ShockConverter::class, PenaltyConverter::class, ListConverter::class])
@Database(entities = [WarEntity::class, TopicEntity::class, MKCTeamEntity::class, MKCLightPlayerEntity::class], version = 7)
abstract class MKDatabase : RoomDatabase() {

    abstract fun warDao(): WarDao
    abstract fun topicDao(): TopicDao
    abstract fun mkcTeamDao(): MKCTeamDao
    abstract fun mkcLightPlayerDao(): MKCLightPlayerDao

    @FlowPreview
    companion object {

        @Volatile
        private var instance: MKDatabase? = null

        @Synchronized
        fun getInstance(context: Context): MKDatabase {
            return instance ?: synchronized(this) {
                val newInstance = Room.databaseBuilder(
                    context.applicationContext,
                    MKDatabase::class.java,
                    "mk_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()

                instance = newInstance
                newInstance
            }
        }


    }
}