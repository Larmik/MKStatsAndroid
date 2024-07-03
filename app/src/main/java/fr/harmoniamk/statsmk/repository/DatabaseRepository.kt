package fr.harmoniamk.statsmk.repository

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.harmoniamk.statsmk.database.entities.MKCRosterEntity
import fr.harmoniamk.statsmk.database.entities.TopicEntity
import fr.harmoniamk.statsmk.datasource.NewPlayerLocalDataSourceInterface
import fr.harmoniamk.statsmk.datasource.NewTeamLocalDataSourceInterface
import fr.harmoniamk.statsmk.datasource.RosterLocalDataSourceInterface
import fr.harmoniamk.statsmk.datasource.TopicLocalDataSourceInterface
import fr.harmoniamk.statsmk.datasource.WarLocalDataSourceInterface
import fr.harmoniamk.statsmk.model.local.MKWar
import fr.harmoniamk.statsmk.model.network.MKCFullTeam
import fr.harmoniamk.statsmk.model.network.MKCTeam
import fr.harmoniamk.statsmk.model.network.MKPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

interface DatabaseRepositoryInterface {
    fun getNewTeams(): Flow<List<MKCTeam>>
    fun getPlayers(): Flow<List<MKPlayer>>
    fun getWars(): Flow<List<MKWar>>
    fun getNewTeam(id: String?): Flow<MKCTeam?>
    fun getNewUser(id: String?): Flow<MKPlayer?>
    fun getWar(id: String?): Flow<MKWar?>
    fun getRosters(): Flow<List<MKCRosterEntity>>

    fun writePlayers(list: List<MKPlayer>): Flow<Unit>
    fun writeNewTeams(list: List<MKCTeam>): Flow<Unit>
    fun writeWars(list: List<MKWar>): Flow<Unit>
    fun updateUser(user: MKPlayer): Flow<Unit>
    fun writeUser(user: MKPlayer): Flow<Unit>
    fun writeWar(war: MKWar): Flow<Unit>
    fun writeTopic(topic: TopicEntity): Flow<Unit>
    fun writeRoster(roster: MKCFullTeam): Flow<Unit>
    fun deleteTopic(topic: String): Flow<Unit>
    fun clear(): Flow<Unit>
    fun clearRoster(): Flow<Unit>
}

@FlowPreview
@ExperimentalCoroutinesApi
@Module
@InstallIn(SingletonComponent::class)
interface DatabaseRepositoryModule {
    @Binds
    @Singleton
    fun bindRepository(impl: DatabaseRepository): DatabaseRepositoryInterface
}

@FlowPreview
@ExperimentalCoroutinesApi
class DatabaseRepository @Inject constructor(
    private val newTeamLocalDataSource: NewTeamLocalDataSourceInterface,
    private val newPlayerLocalDataSource: NewPlayerLocalDataSourceInterface,
    private val warDataSource: WarLocalDataSourceInterface,
    private val topicDataSource: TopicLocalDataSourceInterface,
    private val rosterLocalDataSource: RosterLocalDataSourceInterface
) : DatabaseRepositoryInterface {

    override fun getNewTeam(id: String?): Flow<MKCTeam?> = id?.let {
        newTeamLocalDataSource.getById(it).flowOn(Dispatchers.IO)
    } ?: flowOf(null)
    override fun getNewUser(id: String?): Flow<MKPlayer?> = id?.let {
        newPlayerLocalDataSource.getById(it).flowOn(Dispatchers.IO)
    } ?: flowOf(null)
    override fun getWar(id: String?): Flow<MKWar?> = id?.let {
        warDataSource.getById(it).flowOn(Dispatchers.IO)
    } ?: flowOf(null)

    override fun getRosters(): Flow<List<MKCRosterEntity>> = rosterLocalDataSource.getAll()

    override fun getNewTeams(): Flow<List<MKCTeam>> = newTeamLocalDataSource.getAll().flowOn(Dispatchers.IO)
    override fun getPlayers(): Flow<List<MKPlayer>> = newPlayerLocalDataSource.getAll().flowOn(Dispatchers.IO)
    override fun getWars(): Flow<List<MKWar>> = warDataSource.getAll().flowOn(Dispatchers.IO)
    override fun writePlayers(list: List<MKPlayer>): Flow<Unit> = newPlayerLocalDataSource.bulkInsert(list).flowOn(Dispatchers.IO)
    override fun writeNewTeams(list: List<MKCTeam>) = newTeamLocalDataSource.bulkInsert(list).flowOn(Dispatchers.IO)
    override fun writeWars(list: List<MKWar>) = warDataSource.insert(list).flowOn(Dispatchers.IO)
    override fun updateUser(user: MKPlayer) = newPlayerLocalDataSource.update(user).flowOn(Dispatchers.IO)
    override fun writeUser(user: MKPlayer) = newPlayerLocalDataSource.insert(user).flowOn(Dispatchers.IO)
    override fun writeWar(war: MKWar) = warDataSource.insert(war).flowOn(Dispatchers.IO)
    override fun writeTopic(topic: TopicEntity) = topicDataSource.insert(topic).flowOn(Dispatchers.IO)
    override fun writeRoster(roster: MKCFullTeam): Flow<Unit> = rosterLocalDataSource.insert(
        MKCRosterEntity(roster)
    )

    override fun deleteTopic(topic: String) = topicDataSource.delete(topic).flowOn(Dispatchers.IO)
    override fun clearRoster() = newPlayerLocalDataSource.clear().flowOn(Dispatchers.IO)
    override fun clear() = newTeamLocalDataSource.clear()
        .flatMapLatest { newPlayerLocalDataSource.clear() }
        .flatMapLatest { warDataSource.clear() }
        .flatMapLatest { topicDataSource.clear() }
        .flatMapLatest { rosterLocalDataSource.clear() }
        .flowOn(Dispatchers.IO)
}