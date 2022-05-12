package fr.harmoniamk.statsmk.database.dao

import androidx.room.*
import fr.harmoniamk.statsmk.model.local.MKTournamentTrack
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayedTrackDao {

    @Query("SELECT * FROM MKTournamentTrack")
    fun getAll(): Flow<List<MKTournamentTrack>>

    @Query("SELECT * FROM MKTournamentTrack WHERE tmId=(:id)")
    fun getByTmID(id: Int): Flow<List<MKTournamentTrack>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(track: MKTournamentTrack)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(track: MKTournamentTrack)

    @Query("DELETE FROM MKTournamentTrack WHERE tmId=(:id)")
    fun deleteByTmId(id: Int)

    @Query("SELECT * FROM MKTournamentTrack WHERE mid=(:id) LIMIT 1")
    fun getById(id: Int) : Flow<MKTournamentTrack>

    @Query("UPDATE MKTournamentTrack SET trackIndex=(:newTrack) WHERE mid=(:id)")
    fun updateTrack(id: Int, newTrack: Int)

    @Query("UPDATE MKTournamentTrack SET position=(:newPos) WHERE mid=(:id)")
    fun updatePosition(id: Int, newPos: Int)

}