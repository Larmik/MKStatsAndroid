package fr.harmoniamk.statsmk.datasource

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import fr.harmoniamk.statsmk.database.room.MKDatabase
import fr.harmoniamk.statsmk.database.room.model.Tournament
import fr.harmoniamk.statsmk.extension.displayedString
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.util.*
import javax.inject.Inject

interface TournamentDataSourceInterface {
    fun getAll(): Flow<List<Tournament>>
    fun insert(tournament: Tournament): Flow<Unit>
    fun delete(tournament: Tournament): Flow<Unit>
    fun update(id: Int, points: Int): Flow<Unit>
    fun getCurrent(): Flow<Tournament?>
    fun getById(id: Int): Flow<Tournament?>
    fun incrementTrackNumber(id: Int): Flow<Unit>
    fun incrementTops(id: Int): Flow<Unit>

}

@FlowPreview
@ExperimentalCoroutinesApi
@Module
@InstallIn(ApplicationComponent::class)
interface TournamentDataSourceModule {
    @Binds
    fun bind(impl: TournamentDataSource): TournamentDataSourceInterface
}

@FlowPreview
@ExperimentalCoroutinesApi
class TournamentDataSource @Inject constructor(@ApplicationContext private val context: Context) :
    TournamentDataSourceInterface {

    val dao = MKDatabase.getInstance(context).tournamentDao()

    override fun getAll(): Flow<List<Tournament>> = dao.getAll()
    override fun insert(tournament: Tournament): Flow<Unit> = flowOf(dao.insert(tournament))
    override fun delete(tournament: Tournament): Flow<Unit> = flowOf(dao.delete(tournament))
    override fun update(id: Int, points: Int): Flow<Unit> = flowOf(dao.update(id, points))
    override fun getCurrent(): Flow<Tournament?> = dao.getCurrent()
    override fun getById(id: Int): Flow<Tournament?> = dao.getById(id)
    override fun incrementTrackNumber(id: Int): Flow<Unit> = flowOf(dao.incrementTrackNumber(id, Date().displayedString()))
    override fun incrementTops(id: Int): Flow<Unit> = flowOf(dao.incrementTops(id))

}