package fr.harmoniamk.statsmk.database.dao

import androidx.room.*
import fr.harmoniamk.statsmk.database.entities.TeamEntity
import fr.harmoniamk.statsmk.database.entities.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TeamDao {

    @Query("SELECT * FROM TeamEntity")
    fun getAll(): Flow<List<TeamEntity>>

    @Query("SELECT * FROM TeamEntity WHERE mid=(:id) LIMIT 1")
    fun getById(id: String): Flow<TeamEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun bulkInsert(teams: List<TeamEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(team: TeamEntity)

    @Delete
    suspend fun delete(team: TeamEntity)
}