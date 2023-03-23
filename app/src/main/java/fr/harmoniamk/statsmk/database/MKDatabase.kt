package fr.harmoniamk.statsmk.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import fr.harmoniamk.statsmk.database.dao.TeamDao
import fr.harmoniamk.statsmk.database.dao.UserDao
import fr.harmoniamk.statsmk.database.entities.TeamEntity
import fr.harmoniamk.statsmk.database.entities.UserEntity
import kotlinx.coroutines.FlowPreview

@Database(entities = [UserEntity::class, TeamEntity::class], version = 1)
abstract class MKDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun teamDao(): TeamDao

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
                ).allowMainThreadQueries()
                    .build()

                instance = newInstance
                newInstance
            }
        }
    }
}