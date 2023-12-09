package fr.harmoniamk.statsmk.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import fr.harmoniamk.statsmk.database.entities.MKCLightPlayerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MKCLightPlayerDao {

    @Query("SELECT * FROM MKCLightPlayerEntity")
    fun getAll(): Flow<List<MKCLightPlayerEntity>>

    @Query("SELECT * FROM MKCLightPlayerEntity WHERE player_id=(:id) LIMIT 1")
    fun getById(id: String): Flow<MKCLightPlayerEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun bulkInsert(teams: List<MKCLightPlayerEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(team: MKCLightPlayerEntity)

    @Delete
    suspend fun delete(team: MKCLightPlayerEntity)

    @Query("DELETE FROM MKCTeamEntity")
    suspend fun clear()

}