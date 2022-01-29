package fr.harmoniamk.statsmk.repository

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import fr.harmoniamk.statsmk.database.model.PlayedTrack
import fr.harmoniamk.statsmk.database.model.Tournament
import fr.harmoniamk.statsmk.datasource.PlayedTrackDataSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@ExperimentalCoroutinesApi
@FlowPreview
interface PlayedTrackRepositoryInterface {
    fun getAll(): Flow<List<PlayedTrack>>
    fun getById(id: Int): Flow<PlayedTrack>
    fun getByTmId(id: Int): Flow<List<PlayedTrack>>
    fun deleteByTmId(id: Int): Flow<Unit>
    fun insert(track: PlayedTrack): Flow<Unit>
    fun updatePosition(id: Int, newPos: Int): Flow<Unit>
    fun updateTrack(id: Int, newTrack: Int): Flow<Unit>
}

@FlowPreview
@ExperimentalCoroutinesApi
@Module
@InstallIn(ApplicationComponent::class)
interface PlayedTrackRepositoryModule {
    @Binds
    fun bindRepository(impl: PlayedTrackRepository): PlayedTrackRepositoryInterface
}

@FlowPreview
@ExperimentalCoroutinesApi
class PlayedTrackRepository @Inject constructor(private val dataSource: PlayedTrackDataSource) : PlayedTrackRepositoryInterface {
    override fun getAll(): Flow<List<PlayedTrack>> = dataSource.getAll()
    override fun getById(id: Int): Flow<PlayedTrack> = dataSource.getById(id)
    override fun getByTmId(id: Int): Flow<List<PlayedTrack>> = dataSource.getByTmId(id)
    override fun deleteByTmId(id: Int): Flow<Unit> = dataSource.deleteByTmId(id)
    override fun insert(track: PlayedTrack): Flow<Unit> = dataSource.insert(track)
    override fun updatePosition(id: Int, newPos: Int): Flow<Unit> = dataSource.updatePosition(id, newPos)
    override fun updateTrack(id: Int, newTrack: Int): Flow<Unit> = dataSource.updateTrack(id, newTrack)

}