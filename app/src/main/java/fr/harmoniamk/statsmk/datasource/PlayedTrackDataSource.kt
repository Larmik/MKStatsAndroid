package fr.harmoniamk.statsmk.datasource

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import fr.harmoniamk.statsmk.database.MKDatabase
import fr.harmoniamk.statsmk.model.local.MKTournamentTrack
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

interface PlayedTrackDataSourceInterface {
    fun getAll(): Flow<List<MKTournamentTrack>>
    fun getById(id: Int): Flow<MKTournamentTrack>
    fun getByTmId(id: Int): Flow<List<MKTournamentTrack>>
    fun deleteByTmId(id: Int): Flow<Unit>
    fun insert(track: MKTournamentTrack): Flow<Unit>
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

    override fun getAll(): Flow<List<MKTournamentTrack>> = dao.getAll()
    override fun getById(id: Int): Flow<MKTournamentTrack> = dao.getById(id)
    override fun getByTmId(id: Int): Flow<List<MKTournamentTrack>> = dao.getByTmID(id)
    override fun deleteByTmId(id: Int): Flow<Unit> = flowOf(dao.deleteByTmId(id))
    override fun insert(track: MKTournamentTrack): Flow<Unit> = flowOf(dao.insert(track))
    override fun updatePosition(id: Int, newPos: Int): Flow<Unit> = flowOf(dao.updatePosition(id, newPos))
    override fun updateTrack(id: Int, newTrack: Int): Flow<Unit> = flowOf(dao.updateTrack(id, newTrack))

}