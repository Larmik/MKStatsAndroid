package fr.harmoniamk.statsmk.datasource

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import fr.harmoniamk.statsmk.database.room.MKDatabase
import fr.harmoniamk.statsmk.database.room.model.PlayedTrack
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

interface PlayedTrackDataSourceInterface {
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
interface PlayedTrackDataSourceModule {
    @Binds
    fun bind(impl: PlayedTrackDataSource): PlayedTrackDataSourceInterface
}

@FlowPreview
@ExperimentalCoroutinesApi
class PlayedTrackDataSource @Inject constructor(@ApplicationContext private val context: Context) :
    PlayedTrackDataSourceInterface {

    val dao = MKDatabase.getInstance(context).playedTrackDao()

    override fun getAll(): Flow<List<PlayedTrack>> = dao.getAll()
    override fun getById(id: Int): Flow<PlayedTrack> = dao.getById(id)
    override fun getByTmId(id: Int): Flow<List<PlayedTrack>> = dao.getByTmID(id)
    override fun deleteByTmId(id: Int): Flow<Unit> = flowOf(dao.deleteByTmId(id))
    override fun insert(track: PlayedTrack): Flow<Unit> = flowOf(dao.insert(track))
    override fun updatePosition(id: Int, newPos: Int): Flow<Unit> = flowOf(dao.updatePosition(id, newPos))
    override fun updateTrack(id: Int, newTrack: Int): Flow<Unit> = flowOf(dao.updateTrack(id, newTrack))

}