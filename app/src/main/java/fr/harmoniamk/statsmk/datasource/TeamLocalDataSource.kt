package fr.harmoniamk.statsmk.datasource

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import fr.harmoniamk.statsmk.database.MKDatabase
import fr.harmoniamk.statsmk.model.firebase.Team
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

interface TeamLocalDataSourceInterface {
    fun getAll(): Flow<List<Team>>
    fun getById(id: String): Flow<Team>
    fun insert(teams: List<Team>): Flow<Unit>
    fun insert(team: Team): Flow<Unit>
    fun delete(team: Team): Flow<Unit>
    fun clear(): Flow<Unit>
}

@FlowPreview
@ExperimentalCoroutinesApi
@Module
@InstallIn(SingletonComponent::class)
interface TeamLocalDataSourceModule {
    @Singleton
    @Binds
    fun bind(impl: TeamLocalDataSource): TeamLocalDataSourceInterface
}

@FlowPreview
@ExperimentalCoroutinesApi
class TeamLocalDataSource @Inject constructor(@ApplicationContext private val context: Context) :
    TeamLocalDataSourceInterface {

    private val dao = MKDatabase.getInstance(context).teamDao()

    override fun getAll(): Flow<List<Team>> = dao.getAll().map { list -> list.map { Team(it) } }

    override fun getById(id: String): Flow<Team> = dao.getById(id).filterNotNull().map {  Team(it)  }

    override fun insert(teams: List<Team>): Flow<Unit> = flow { emit(dao.bulkInsert(teams.map { it.toEntity() })) }

    override fun insert(team: Team): Flow<Unit> = flow { emit(dao.insert(team.toEntity())) }

    override fun delete(team: Team): Flow<Unit> = flow { emit(dao.delete(team.toEntity())) }

    override fun clear(): Flow<Unit> = flow { emit(dao.clear()) }

}