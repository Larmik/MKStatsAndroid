package fr.harmoniamk.statsmk.database.dao

import androidx.room.*
import fr.harmoniamk.statsmk.database.entities.WarEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WarDao {

    @Query("SELECT * FROM WarEntity")
    fun getAll(): Flow<List<WarEntity>>

    @Query("SELECT * FROM WarEntity WHERE mid=(:id) LIMIT 1")
    fun getById(id: String?): Flow<WarEntity>

    @Query("DELETE FROM WarEntity")
    suspend fun clear()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun bulkInsert(wars: List<WarEntity?>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(war: WarEntity?)

    @Delete
    suspend fun delete(war: WarEntity?)
}