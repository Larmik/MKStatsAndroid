package fr.harmoniamk.statsmk.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import fr.harmoniamk.statsmk.database.entities.MKCTeamEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MKCTeamDao {

    @Query("SELECT * FROM MKCTeamEntity")
    fun getAll(): Flow<List<MKCTeamEntity>>

    @Query("SELECT * FROM MKCTeamEntity WHERE id=(:id) LIMIT 1")
    fun getById(id: String): Flow<MKCTeamEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun bulkInsert(teams: List<MKCTeamEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(team: MKCTeamEntity)

    @Delete
    suspend fun delete(team: MKCTeamEntity)

    @Query("DELETE FROM MKCTeamEntity")
    suspend fun clear()
}