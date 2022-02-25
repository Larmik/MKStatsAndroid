package fr.harmoniamk.statsmk.database.dao

import androidx.room.*
import fr.harmoniamk.statsmk.database.model.PlayedTrack
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayedTrackDao {

    @Query("SELECT * FROM PlayedTrack")
    fun getAll(): Flow<List<PlayedTrack>>

    @Query("SELECT * FROM PlayedTrack WHERE tmId=(:id)")
    fun getByTmID(id: Int): Flow<List<PlayedTrack>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(track: PlayedTrack)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(track: PlayedTrack)

    @Query("DELETE FROM PlayedTrack WHERE tmId=(:id)")
    fun deleteByTmId(id: Int)

    @Query("SELECT * FROM PlayedTrack WHERE mid=(:id) LIMIT 1")
    fun getById(id: Int) : Flow<PlayedTrack>

    @Query("UPDATE PlayedTrack SET trackIndex=(:newTrack) WHERE mid=(:id)")
    fun updateTrack(id: Int, newTrack: Int)

    @Query("UPDATE PlayedTrack SET position=(:newPos) WHERE mid=(:id)")
    fun updatePosition(id: Int, newPos: Int)

}