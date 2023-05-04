package fr.harmoniamk.statsmk.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import fr.harmoniamk.statsmk.database.converters.*
import fr.harmoniamk.statsmk.database.dao.TeamDao
import fr.harmoniamk.statsmk.database.dao.UserDao
import fr.harmoniamk.statsmk.database.dao.WarDao
import fr.harmoniamk.statsmk.database.entities.TeamEntity
import fr.harmoniamk.statsmk.database.entities.UserEntity
import fr.harmoniamk.statsmk.database.entities.WarEntity
import kotlinx.coroutines.FlowPreview

@TypeConverters(value = [WarTrackConverter::class, WarPositionConverter::class, ShockConverter::class, PenaltyConverter::class, ListConverter::class])
@Database(entities = [UserEntity::class, TeamEntity::class, WarEntity::class], version = 2)
abstract class MKDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun teamDao(): TeamDao
    abstract fun warDao(): WarDao

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
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build()

                instance = newInstance
                newInstance
            }
        }


    }
}