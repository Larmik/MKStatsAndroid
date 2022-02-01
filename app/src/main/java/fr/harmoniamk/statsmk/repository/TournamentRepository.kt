package fr.harmoniamk.statsmk.repository

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import fr.harmoniamk.statsmk.database.room.model.Tournament
import fr.harmoniamk.statsmk.datasource.TournamentDataSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@ExperimentalCoroutinesApi
@FlowPreview
interface TournamentRepositoryInterface {
    fun getAll(): Flow<List<Tournament>>
    fun insert(tournament: Tournament): Flow<Unit>
    fun delete(tournament: Tournament): Flow<Unit>
    fun update(id: Int, points: Int): Flow<Unit>
    fun getCurrent(): Flow<Tournament?>
    fun getbyId(id: Int): Flow<Tournament?>
    fun incrementTrackNumber(id: Int): Flow<Unit>
    fun incrementTops(id: Int): Flow<Unit>

}

@FlowPreview
@ExperimentalCoroutinesApi
@Module
@InstallIn(ApplicationComponent::class)
interface TournamentRepositoryModule {
    @Binds
    fun bindRepository(impl: TournamentRepository): TournamentRepositoryInterface
}

@FlowPreview
@ExperimentalCoroutinesApi
class TournamentRepository @Inject constructor(private val dataSource: TournamentDataSource) : TournamentRepositoryInterface {
    override fun getAll(): Flow<List<Tournament>> = dataSource.getAll()
    override fun insert(tournament: Tournament): Flow<Unit> = dataSource.insert(tournament)
    override fun delete(tournament: Tournament): Flow<Unit> = dataSource.delete(tournament)
    override fun update(id: Int, points: Int): Flow<Unit> = dataSource.update(id, points)
    override fun getCurrent(): Flow<Tournament?> = dataSource.getCurrent()
    override fun getbyId(id: Int): Flow<Tournament?> = dataSource.getById(id)
    override fun incrementTrackNumber(id: Int): Flow<Unit> = dataSource.incrementTrackNumber(id)
    override fun incrementTops(id: Int): Flow<Unit> = dataSource.incrementTops(id)
}