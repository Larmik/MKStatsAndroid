package fr.harmoniamk.statsmk.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import fr.harmoniamk.statsmk.database.entities.MKCRosterEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MKCRosterDao {

    @Query("SELECT * FROM MKCRosterEntity")
    fun getAll(): Flow<List<MKCRosterEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(roster: MKCRosterEntity)

    @Query("DELETE FROM MKCRosterEntity")
    suspend fun clear()
}