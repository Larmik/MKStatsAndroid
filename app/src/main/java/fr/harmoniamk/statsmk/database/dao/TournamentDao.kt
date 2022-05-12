package fr.harmoniamk.statsmk.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import fr.harmoniamk.statsmk.model.local.MKTournament
import kotlinx.coroutines.flow.Flow

@Dao
interface TournamentDao {

    @Query("SELECT * FROM mktournament")
    fun getAll(): Flow<List<MKTournament>>

    @Query("SELECT * FROM mktournament WHERE mid=(:id)")
    fun getById(id: Int): Flow<MKTournament?>

    @Query("SELECT * FROM mktournament WHERE trackPlayed<trackCount LIMIT 1")
    fun getCurrent(): Flow<MKTournament?>

    @Insert(onConflict = REPLACE)
    fun insert(MKTournament: MKTournament)

    @Delete
    fun delete(MKTournament: MKTournament)

    @Query("UPDATE mktournament SET points=(:points) WHERE mid=(:id)")
    fun update(id: Int, points: Int)

    @Query("UPDATE mktournament SET trackPlayed = trackPlayed+1, updated_date=(:date) WHERE mid=(:id)")
    fun incrementTrackNumber(id: Int, date: String)

    @Query("UPDATE mktournament SET tops = tops+1 WHERE mid=(:id)")
    fun incrementTops(id: Int)

}