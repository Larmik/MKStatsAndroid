package fr.harmoniamk.statsmk.datasource

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import fr.harmoniamk.statsmk.database.MKDatabase
import fr.harmoniamk.statsmk.model.network.MKCTeam
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

interface NewTeamLocalDataSourceInterface {
    fun getAll(): Flow<List<MKCTeam>>
    fun getById(id: String) : Flow<MKCTeam>
    fun bulkInsert(teams: List<MKCTeam>): Flow<Unit>
    fun insert(team: MKCTeam): Flow<Unit>
    fun clear(): Flow<Unit>
}

@FlowPreview
@ExperimentalCoroutinesApi
@Module
@InstallIn(SingletonComponent::class)
interface NewTeamLocalDataSourceModule {
    @Singleton
    @Binds
    fun bind(impl: NewTeamLocalDataSource): NewTeamLocalDataSourceInterface
}

@FlowPreview
@ExperimentalCoroutinesApi
class NewTeamLocalDataSource @Inject constructor(@ApplicationContext private val context: Context) :
    NewTeamLocalDataSourceInterface {

    private val dao = MKDatabase.getInstance(context).mkcTeamDao()

    override fun getAll(): Flow<List<MKCTeam>> = dao.getAll().map { list -> list.map { MKCTeam(it) } }
    override fun getById(id: String) = dao.getById(id).filterNotNull().map { MKCTeam(it) }
    override fun bulkInsert(teams: List<MKCTeam>): Flow<Unit> = flow { emit(dao.bulkInsert(teams.map { it.toEntity() })) }
    override fun insert(team: MKCTeam): Flow<Unit> = flow { emit(dao.insert(team.toEntity())) }
    override fun clear(): Flow<Unit> = flow { emit(dao.clear())}

}