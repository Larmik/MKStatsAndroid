package fr.harmoniamk.statsmk.database.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import fr.harmoniamk.statsmk.database.room.model.Tournament
import kotlinx.coroutines.flow.Flow

@Dao
interface TournamentDao {

    @Query("SELECT * FROM tournament")
    fun getAll(): Flow<List<Tournament>>

    @Query("SELECT * FROM tournament WHERE mid=(:id)")
    fun getById(id: Int): Flow<Tournament?>

    @Query("SELECT * FROM tournament WHERE trackPlayed<trackCount LIMIT 1")
    fun getCurrent(): Flow<Tournament?>

    @Insert(onConflict = REPLACE)
    fun insert(tournament: Tournament)

    @Delete
    fun delete(tournament: Tournament)

    @Query("UPDATE tournament SET points=(:points) WHERE mid=(:id)")
    fun update(id: Int, points: Int)

    @Query("UPDATE tournament SET trackPlayed = trackPlayed+1, updated_date=(:date) WHERE mid=(:id)")
    fun incrementTrackNumber(id: Int, date: String)

    @Query("UPDATE tournament SET tops = tops+1 WHERE mid=(:id)")
    fun incrementTops(id: Int)

}