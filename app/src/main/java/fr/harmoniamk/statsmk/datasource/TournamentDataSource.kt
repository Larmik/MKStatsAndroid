package fr.harmoniamk.statsmk.datasource

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import fr.harmoniamk.statsmk.database.MKDatabase
import fr.harmoniamk.statsmk.model.local.MKTournament
import fr.harmoniamk.statsmk.extension.displayedString
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.util.*
import javax.inject.Inject

interface TournamentDataSourceInterface {
    fun getAll(): Flow<List<MKTournament>>
    fun insert(MKTournament: MKTournament): Flow<Unit>
    fun delete(MKTournament: MKTournament): Flow<Unit>
    fun update(id: Int, points: Int): Flow<Unit>
    fun getCurrent(): Flow<MKTournament?>
    fun getById(id: Int): Flow<MKTournament?>
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

    private val dao = MKDatabase.getInstance(context).tournamentDao()

    override fun getAll(): Flow<List<MKTournament>> = dao.getAll()
    override fun insert(MKTournament: MKTournament): Flow<Unit> = flowOf(dao.insert(MKTournament))
    override fun delete(MKTournament: MKTournament): Flow<Unit> = flowOf(dao.delete(MKTournament))
    override fun update(id: Int, points: Int): Flow<Unit> = flowOf(dao.update(id, points))
    override fun getCurrent(): Flow<MKTournament?> = dao.getCurrent()
    override fun getById(id: Int): Flow<MKTournament?> = dao.getById(id)
    override fun incrementTrackNumber(id: Int): Flow<Unit> = flowOf(dao.incrementTrackNumber(id, Date().displayedString()))
    override fun incrementTops(id: Int): Flow<Unit> = flowOf(dao.incrementTops(id))

}