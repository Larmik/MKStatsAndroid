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
import fr.harmoniamk.statsmk.model.network.MKCLightPlayer
import fr.harmoniamk.statsmk.model.network.MKCTeam
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

interface DatabaseRepositoryInterface {


    fun getNewTeams(): Flow<List<MKCTeam>>
    fun getRoster(): Flow<List<MKCLightPlayer>>
    fun getWars(): Flow<List<MKWar>>

    fun getNewTeam(id: String?): Flow<MKCTeam?>
    fun getNewUser(id: String?): Flow<MKCLightPlayer?>
    fun getWar(id: String?): Flow<MKWar?>

    fun writeRoster(list: List<MKCLightPlayer>): Flow<Unit>
    fun writeNewTeams(list: List<MKCTeam>): Flow<Unit>
    fun writeWars(list: List<MKWar>): Flow<Unit>
    fun updateUser(user: MKCLightPlayer): Flow<Unit>
    fun writeUser(user: MKCLightPlayer): Flow<Unit>

    fun writeWar(war: MKWar): Flow<Unit>
    fun writeTopic(topic: TopicEntity): Flow<Unit>

    fun deleteTopic(topic: String): Flow<Unit>

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
        return newTeamLocalDataSource.getAll()
    }

    override fun getRoster(): Flow<List<MKCLightPlayer>> {
        Log.d("MKDebugOnly", "DatabaseRepository getRoster")
        return newPlayerLocalDataSource.getAll()
    }

    override fun getWars(): Flow<List<MKWar>> {
        Log.d("MKDebugOnly", "DatabaseRepository getWars")
        return warDataSource.getAll()
    }


    override fun getNewTeam(id: String?): Flow<MKCTeam?> = id?.let {
        Log.d("MKDebugOnly", "DatabaseRepository getNewTeam $id")
        newTeamLocalDataSource.getById(it)
    } ?: flowOf(null)

    override fun getNewUser(id: String?): Flow<MKCLightPlayer?> = id?.let {
        Log.d("MKDebugOnly", "DatabaseRepository getNewUser $id")
        newPlayerLocalDataSource.getById(it)
    } ?: flowOf(null)

    override fun getWar(id: String?): Flow<MKWar?> = id?.let {
        Log.d("MKDebugOnly", "DatabaseRepository getWar $id")
        warDataSource.getById(it)
    } ?: flowOf(null)


    override fun writeRoster(list: List<MKCLightPlayer>): Flow<Unit> {
        Log.d("MKDebugOnly", "DatabaseRepository writeNewUsers")
        return newPlayerLocalDataSource.bulkInsert(list)
    }


    override fun writeNewTeams(list: List<MKCTeam>): Flow<Unit> {
        Log.d("MKDebugOnly", "DatabaseRepository writeNewTeams")
        return newTeamLocalDataSource.bulkInsert(list)
    }

    override fun writeWars(list: List<MKWar>): Flow<Unit> {
        Log.d("MKDebugOnly", "DatabaseRepository writeWars")
        return warDataSource.insert(list)
    }

    override fun updateUser(user: MKCLightPlayer): Flow<Unit> {
        Log.d("MKDebugOnly", "DatabaseRepository updateUser")
        return newPlayerLocalDataSource.update(user)
    }

    override fun writeUser(user: MKCLightPlayer): Flow<Unit> {
        Log.d("MKDebugOnly", "DatabaseRepository writeUser")
        return newPlayerLocalDataSource.insert(user)
    }


    override fun writeWar(war: MKWar): Flow<Unit> {
        Log.d("MKDebugOnly", "DatabaseRepository writeWar ${war.name}")
        return warDataSource.insert(war)
    }

    override fun writeTopic(topic: TopicEntity): Flow<Unit> {
        Log.d("MKDebugOnly", "DatabaseRepository writeTopic ${topic.topic}")
        return topicDataSource.insert(topic)
    }

    override fun deleteTopic(topic: String): Flow<Unit> {
        Log.d("MKDebugOnly", "DatabaseRepository delete topic $topic")
        return topicDataSource.delete(topic)
    }

}