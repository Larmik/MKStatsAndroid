package fr.harmoniamk.statsmk.datasource

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import fr.harmoniamk.statsmk.database.MKDatabase
import fr.harmoniamk.statsmk.model.network.MKCLightPlayer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

interface NewPlayerLocalDataSourceInterface {
    fun getAll(): Flow<List<MKCLightPlayer>>
    fun getById(id: String): Flow<MKCLightPlayer>
    fun bulkInsert(players: List<MKCLightPlayer>): Flow<Unit>
    fun insert(player: MKCLightPlayer): Flow<Unit>
    fun update(player: MKCLightPlayer): Flow<Unit>
    fun clear(): Flow<Unit>
}

@FlowPreview
@ExperimentalCoroutinesApi
@Module
@InstallIn(SingletonComponent::class)
interface NewPlayerLocalDataSourceModule {
    @Singleton
    @Binds
    fun bind(impl: NewPlayerLocalDataSource): NewPlayerLocalDataSourceInterface
}

@FlowPreview
@ExperimentalCoroutinesApi
class NewPlayerLocalDataSource @Inject constructor(@ApplicationContext private val context: Context) :
    NewPlayerLocalDataSourceInterface {

    private val dao = MKDatabase.getInstance(context).mkcLightPlayerDao()

    override fun getAll(): Flow<List<MKCLightPlayer>> = dao.getAll().map { list -> list.map { MKCLightPlayer(it) } }
    override fun getById(id: String): Flow<MKCLightPlayer> = dao.getById(id).map { MKCLightPlayer(it) }
    override fun bulkInsert(players: List<MKCLightPlayer>): Flow<Unit> = flow { emit(dao.bulkInsert(players.map { it.toEntity() })) }

    override fun insert(player: MKCLightPlayer): Flow<Unit> = flow { emit(dao.insert(player.toEntity())) }
    override fun update(player: MKCLightPlayer): Flow<Unit>  = flow { emit(dao.update(player.toEntity())) }
    override fun clear(): Flow<Unit> = flow { emit(dao.clear())}


}