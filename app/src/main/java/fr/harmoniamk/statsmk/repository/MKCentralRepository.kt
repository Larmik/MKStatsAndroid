package fr.harmoniamk.statsmk.repository

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.harmoniamk.statsmk.datasource.MKCentralNetworkDataSource
import fr.harmoniamk.statsmk.model.network.MKCFullPlayer
import fr.harmoniamk.statsmk.model.network.MKCFullTeam
import fr.harmoniamk.statsmk.model.network.MKCPlayer
import fr.harmoniamk.statsmk.model.network.MKCTeam
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

interface MKCentralRepositoryInterface {
    val teams: Flow<List<MKCTeam>>
    val historicalTeams: Flow<List<MKCTeam>>
    fun getTeam(id: String): Flow<MKCFullTeam>
    fun getPlayer(id: String): Flow<MKCFullPlayer?>
    fun searchPlayers(search: String): Flow<List<MKCPlayer>>
}

@FlowPreview
@ExperimentalCoroutinesApi
@Module
@InstallIn(SingletonComponent::class)
interface MKCentralRepositoryModule {
    @Binds
    @Singleton
    fun bindRepository(impl: MKCentralRepository): MKCentralRepositoryInterface
}

@FlowPreview
@ExperimentalCoroutinesApi
class MKCentralRepository @Inject constructor(private val dataSource: MKCentralNetworkDataSource) : MKCentralRepositoryInterface {

    override val teams: Flow<List<MKCTeam>> = dataSource.teams
    override val historicalTeams: Flow<List<MKCTeam>> = dataSource.historicalTeams

    override fun getTeam(id: String): Flow<MKCFullTeam> = dataSource.getTeam(id)

    override fun getPlayer(id: String): Flow<MKCFullPlayer?> = dataSource.getPlayer(id)
    override fun searchPlayers(search: String): Flow<List<MKCPlayer>> = dataSource.searchPlayers(search)

}