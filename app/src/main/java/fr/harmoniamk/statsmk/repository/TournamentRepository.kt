package fr.harmoniamk.statsmk.repository

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import fr.harmoniamk.statsmk.model.local.MKTournament
import fr.harmoniamk.statsmk.datasource.TournamentDataSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@ExperimentalCoroutinesApi
@FlowPreview
interface TournamentRepositoryInterface {
    fun getAll(): Flow<List<MKTournament>>
    fun insert(MKTournament: MKTournament): Flow<Unit>
    fun delete(MKTournament: MKTournament): Flow<Unit>
    fun update(id: Int, points: Int): Flow<Unit>
    fun getCurrent(): Flow<MKTournament?>
    fun getbyId(id: Int): Flow<MKTournament?>
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
    override fun getAll(): Flow<List<MKTournament>> = dataSource.getAll()
    override fun insert(MKTournament: MKTournament): Flow<Unit> = dataSource.insert(MKTournament)
    override fun delete(MKTournament: MKTournament): Flow<Unit> = dataSource.delete(MKTournament)
    override fun update(id: Int, points: Int): Flow<Unit> = dataSource.update(id, points)
    override fun getCurrent(): Flow<MKTournament?> = dataSource.getCurrent()
    override fun getbyId(id: Int): Flow<MKTournament?> = dataSource.getById(id)
    override fun incrementTrackNumber(id: Int): Flow<Unit> = dataSource.incrementTrackNumber(id)
    override fun incrementTops(id: Int): Flow<Unit> = dataSource.incrementTops(id)
}