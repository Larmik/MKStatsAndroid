package fr.harmoniamk.statsmk.repository

import android.util.Log
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.harmoniamk.statsmk.database.entities.TopicEntity
import fr.harmoniamk.statsmk.datasource.NewPlayerLocalDataSourceInterface
import fr.harmoniamk.statsmk.datasource.NewTeamLocalDataSourceInterface
import fr.harmoniamk.statsmk.datasource.TeamLocalDataSourceInterface
import fr.harmoniamk.statsmk.datasource.TopicLocalDataSourceInterface
import fr.harmoniamk.statsmk.datasource.UserLocalDataSourceInterface
import fr.harmoniamk.statsmk.datasource.WarLocalDataSourceInterface
import fr.harmoniamk.statsmk.model.firebase.Team
import fr.harmoniamk.statsmk.model.firebase.User
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

    fun getUsers(): Flow<List<User>>
    fun getTeams(): Flow<List<Team>>
    fun getNewTeams(): Flow<List<MKCTeam>>
    fun getRoster(): Flow<List<MKCLightPlayer>>
    fun getWars(): Flow<List<MKWar>>
    fun getTopics(): Flow<List<TopicEntity>>

    fun getUser(id: String?): Flow<User?>
    fun getTeam(id: String?): Flow<Team?>
    fun getNewTeam(id: String?): Flow<MKCTeam?>
    fun getWar(id: String?): Flow<MKWar?>

    fun writeUsers(list: List<User>): Flow<Unit>
    fun writeRoster(list: List<MKCLightPlayer>): Flow<Unit>
    fun writeTeams(list: List<Team>): Flow<Unit>
    fun writeNewTeams(list: List<MKCTeam>): Flow<Unit>
    fun writeWars(list: List<MKWar>): Flow<Unit>

    fun writeUser(user: User): Flow<Unit>
    fun writeTeam(team: Team): Flow<Unit>
    fun writeWar(war: MKWar): Flow<Unit>
    fun writeTopic(topic: TopicEntity): Flow<Unit>

    fun deleteUser(user: User): Flow<Unit>
    fun deleteTeam(team: Team): Flow<Unit>
    fun deleteWar(war: MKWar): Flow<Unit>
    fun deleteTopic(topic: String): Flow<Unit>

    fun clearWars(): Flow<Unit>
    fun clearUsers(): Flow<Unit>
    fun clearTeams(): Flow<Unit>
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
    private val userDataSource: UserLocalDataSourceInterface,
    private val teamDataSource: TeamLocalDataSourceInterface,
    private val newTeamLocalDataSource: NewTeamLocalDataSourceInterface,
    private val newPlayerLocalDataSource: NewPlayerLocalDataSourceInterface,
    private val warDataSource: WarLocalDataSourceInterface,
    private val topicDataSource: TopicLocalDataSourceInterface
) : DatabaseRepositoryInterface {


    override fun getUsers(): Flow<List<User>> {
        Log.d("MKDebugOnly", "DatabaseRepository getUsers")
        return userDataSource.getAll()
    }

    override fun getTeams(): Flow<List<Team>> {
        Log.d("MKDebugOnly", "DatabaseRepository getTeams")
        return teamDataSource.getAll()
    }

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

    override fun getTopics(): Flow<List<TopicEntity>> {
        Log.d("MKDebugOnly", "DatabaseRepository getTopics")
        return topicDataSource.getAll()
    }

    override fun getUser(id: String?): Flow<User?> = id?.let {
        Log.d("MKDebugOnly", "DatabaseRepository getUser $id")
        userDataSource.getById(it)
    } ?: flowOf(null)

    override fun getTeam(id: String?): Flow<Team?> = id?.let {
        Log.d("MKDebugOnly", "DatabaseRepository getTeam $id")
        teamDataSource.getById(it)
    } ?: flowOf(null)

    override fun getNewTeam(id: String?): Flow<MKCTeam?> = id?.let {
        Log.d("MKDebugOnly", "DatabaseRepository getNewTeam $id")
        newTeamLocalDataSource.getById(it)
    } ?: flowOf(null)

    override fun getWar(id: String?): Flow<MKWar?> = id?.let {
        Log.d("MKDebugOnly", "DatabaseRepository getWar $id")
        warDataSource.getById(it)
    } ?: flowOf(null)

    override fun writeUsers(list: List<User>): Flow<Unit> {
        Log.d("MKDebugOnly", "DatabaseRepository writeUsers")
        return userDataSource.insert(list)
    }

    override fun writeRoster(list: List<MKCLightPlayer>): Flow<Unit> {
        Log.d("MKDebugOnly", "DatabaseRepository writeNewUsers")
        return newPlayerLocalDataSource.bulkInsert(list)
    }

    override fun writeTeams(list: List<Team>): Flow<Unit> {
        Log.d("MKDebugOnly", "DatabaseRepository writeTeams")
        return teamDataSource.insert(list)
    }

    override fun writeNewTeams(list: List<MKCTeam>): Flow<Unit> {
        Log.d("MKDebugOnly", "DatabaseRepository writeNewTeams")
        return newTeamLocalDataSource.bulkInsert(list)
    }

    override fun writeWars(list: List<MKWar>): Flow<Unit> {
        Log.d("MKDebugOnly", "DatabaseRepository writeWars")
        return warDataSource.insert(list)
    }

    override fun writeUser(user: User): Flow<Unit> {
        Log.d("MKDebugOnly", "DatabaseRepository writeUser ${user.name}")
        return userDataSource.insert(user)
    }

    override fun writeTeam(team: Team): Flow<Unit> {
        Log.d("MKDebugOnly", "DatabaseRepository writeTeam ${team.mid}")
        return teamDataSource.insert(team)
    }

    override fun writeWar(war: MKWar): Flow<Unit> {
        Log.d("MKDebugOnly", "DatabaseRepository writeWar ${war.name}")
        return warDataSource.insert(war)
    }

    override fun writeTopic(topic: TopicEntity): Flow<Unit> {
        Log.d("MKDebugOnly", "DatabaseRepository writeTopic ${topic.topic}")
        return topicDataSource.insert(topic)
    }

    override fun deleteUser(user: User): Flow<Unit> {
        Log.d("MKDebugOnly", "DatabaseRepository deleteUser ${user.name}")
        return userDataSource.delete(user)
    }

    override fun deleteTeam(team: Team): Flow<Unit> {
        Log.d("MKDebugOnly", "DatabaseRepository deleteTeam ${team.mid}")
        return teamDataSource.delete(team)
    }

    override fun deleteWar(war: MKWar): Flow<Unit> {
        Log.d("MKDebugOnly", "DatabaseRepository deleteWar ${war.name}")
        return warDataSource.delete(war)
    }

    override fun deleteTopic(topic: String): Flow<Unit> {
        Log.d("MKDebugOnly", "DatabaseRepository delete topic $topic")
        return topicDataSource.delete(topic)
    }

    override fun clearWars(): Flow<Unit> {
        Log.d("MKDebugOnly", "DatabaseRepository clearWars")
        return warDataSource.clear()
    }

    override fun clearUsers(): Flow<Unit> {
        Log.d("MKDebugOnly", "DatabaseRepository clearUsers")
        return userDataSource.clear()
    }

    override fun clearTeams(): Flow<Unit> {
        Log.d("MKDebugOnly", "DatabaseRepository clearTeams")
        return teamDataSource.clear()
    }

}