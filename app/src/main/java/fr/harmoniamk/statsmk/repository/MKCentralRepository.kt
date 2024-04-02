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
import fr.harmoniamk.statsmk.model.network.NetworkResponse
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

interface MKCentralRepositoryInterface {
    fun getTeams(category: String): Flow<NetworkResponse<List<MKCTeam>>>
    fun getTeam(id: String): Flow<NetworkResponse<MKCFullTeam>>
    fun getPlayer(id: String): Flow<NetworkResponse<MKCFullPlayer>>
    fun searchPlayers(search: String): Flow<NetworkResponse<List<MKCPlayer>>>
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

    override fun getTeams(category: String): Flow<NetworkResponse<List<MKCTeam>>> = dataSource.getTeams(category)
    override fun getTeam(id: String): Flow<NetworkResponse<MKCFullTeam>> = dataSource.getTeam(id)
    override fun getPlayer(id: String): Flow<NetworkResponse<MKCFullPlayer>> = dataSource.getPlayer(id)
    override fun searchPlayers(search: String): Flow<NetworkResponse<List<MKCPlayer>>> = dataSource.searchPlayers(search)

}