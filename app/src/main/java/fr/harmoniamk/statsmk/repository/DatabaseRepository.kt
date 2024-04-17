package fr.harmoniamk.statsmk.repository

import android.util.Log
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.harmoniamk.statsmk.database.entities.TopicEntity
import fr.harmoniamk.statsmk.datasource.NewPlayerLocalDataSourceInterface
import fr.harmoniamk.statsmk.datasource.NewTeamLocalDataSourceInterface
import fr.harmoniamk.statsmk.datasource.TopicLocalDataSourceInterface
import fr.harmoniamk.statsmk.datasource.WarLocalDataSourceInterface
import fr.harmoniamk.statsmk.model.local.MKWar
import fr.harmoniamk.statsmk.model.network.MKPlayer
import fr.harmoniamk.statsmk.model.network.MKCTeam
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
    fun getRoster(): Flow<List<MKPlayer>>
    fun getWars(): Flow<List<MKWar>>

    fun getNewTeam(id: String?): Flow<MKCTeam?>
    fun getNewUser(id: String?): Flow<MKPlayer?>
    fun getWar(id: String?): Flow<MKWar?>

    fun writeRoster(list: List<MKPlayer>): Flow<Unit>
    fun writeNewTeams(list: List<MKCTeam>): Flow<Unit>
    fun writeWars(list: List<MKWar>): Flow<Unit>
    fun updateUser(user: MKPlayer): Flow<Unit>
    fun writeUser(user: MKPlayer): Flow<Unit>

    fun writeWar(war: MKWar): Flow<Unit>
    fun writeTopic(topic: TopicEntity): Flow<Unit>

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
    private val topicDataSource: TopicLocalDataSourceInterface
) : DatabaseRepositoryInterface {




    override fun getNewTeams(): Flow<List<MKCTeam>> {
        Log.d("MKDebugOnly", "DatabaseRepository getNewTeams")
        return newTeamLocalDataSource.getAll().flowOn(Dispatchers.IO)
    }

    override fun getRoster(): Flow<List<MKPlayer>> {
        Log.d("MKDebugOnly", "DatabaseRepository getRoster")
        return newPlayerLocalDataSource.getAll().flowOn(Dispatchers.IO)
    }

    override fun getWars(): Flow<List<MKWar>> {
        Log.d("MKDebugOnly", "DatabaseRepository getWars")
        return warDataSource.getAll().flowOn(Dispatchers.IO)
    }


    override fun getNewTeam(id: String?): Flow<MKCTeam?> = id?.let {
        Log.d("MKDebugOnly", "DatabaseRepository getNewTeam $id")
        newTeamLocalDataSource.getById(it).flowOn(Dispatchers.IO)
    } ?: flowOf(null)

    override fun getNewUser(id: String?): Flow<MKPlayer?> = id?.let {
        Log.d("MKDebugOnly", "DatabaseRepository getNewUser $id")
        newPlayerLocalDataSource.getById(it).flowOn(Dispatchers.IO)
    } ?: flowOf(null)

    override fun getWar(id: String?): Flow<MKWar?> = id?.let {
        Log.d("MKDebugOnly", "DatabaseRepository getWar $id")
        warDataSource.getById(it).flowOn(Dispatchers.IO)
    } ?: flowOf(null)


    override fun writeRoster(list: List<MKPlayer>): Flow<Unit> {
        Log.d("MKDebugOnly", "DatabaseRepository write ${list.size} New players")
        return newPlayerLocalDataSource.bulkInsert(list).flowOn(Dispatchers.IO)
    }


    override fun writeNewTeams(list: List<MKCTeam>): Flow<Unit> {
        Log.d("MKDebugOnly", "DatabaseRepository writeNewTeams")
        return newTeamLocalDataSource.bulkInsert(list).flowOn(Dispatchers.IO)
    }

    override fun writeWars(list: List<MKWar>): Flow<Unit> {
        Log.d("MKDebugOnly", "DatabaseRepository writeWars")
        return warDataSource.insert(list).flowOn(Dispatchers.IO)
    }

    override fun updateUser(user: MKPlayer): Flow<Unit> {
        Log.d("MKDebugOnly", "DatabaseRepository updateUser")
        return newPlayerLocalDataSource.update(user).flowOn(Dispatchers.IO)
    }

    override fun writeUser(user: MKPlayer): Flow<Unit> {
        Log.d("MKDebugOnly", "DatabaseRepository writeUser")
        return newPlayerLocalDataSource.insert(user).flowOn(Dispatchers.IO)
    }


    override fun writeWar(war: MKWar): Flow<Unit> {
        Log.d("MKDebugOnly", "DatabaseRepository writeWar ${war.name}")
        return warDataSource.insert(war).flowOn(Dispatchers.IO)
    }

    override fun writeTopic(topic: TopicEntity): Flow<Unit> {
        Log.d("MKDebugOnly", "DatabaseRepository writeTopic ${topic.topic}")
        return topicDataSource.insert(topic).flowOn(Dispatchers.IO)
    }

    override fun deleteTopic(topic: String): Flow<Unit> {
        Log.d("MKDebugOnly", "DatabaseRepository delete topic $topic")
        return topicDataSource.delete(topic).flowOn(Dispatchers.IO)
    }

    override fun clear(): Flow<Unit> = newTeamLocalDataSource.clear()
        .flatMapLatest { newPlayerLocalDataSource.clear() }
        .flatMapLatest { warDataSource.clear() }
        .flatMapLatest { topicDataSource.clear() }

    override fun clearRoster(): Flow<Unit> = newPlayerLocalDataSource.clear()

}