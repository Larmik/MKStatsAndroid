package fr.harmoniamk.statsmk.database.dao

import androidx.room.*
import fr.harmoniamk.statsmk.database.entities.TopicEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TopicDao {

    @Query("SELECT * FROM TopicEntity")
    fun getAll(): Flow<List<TopicEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(topic: TopicEntity)

    @Query("DELETE FROM TopicEntity WHERE topic=(:topic)")
    suspend fun delete(topic: String)

    @Query("DELETE FROM TopicEntity")
    suspend fun clear()

}