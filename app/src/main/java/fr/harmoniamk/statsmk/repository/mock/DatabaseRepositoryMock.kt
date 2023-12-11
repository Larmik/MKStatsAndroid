package fr.harmoniamk.statsmk.repository.mock

import fr.harmoniamk.statsmk.database.entities.TopicEntity
import fr.harmoniamk.statsmk.model.firebase.Team
import fr.harmoniamk.statsmk.model.firebase.User
import fr.harmoniamk.statsmk.model.local.MKWar
import fr.harmoniamk.statsmk.model.network.MKCLightPlayer
import fr.harmoniamk.statsmk.model.network.MKCTeam
import fr.harmoniamk.statsmk.repository.DatabaseRepositoryInterface
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class DatabaseRepositoryMock : DatabaseRepositoryInterface {

    val teams = listOf(
        Team("12345", "Harmonia", "HR"),
        Team("67890", "Epines Volantes", "Ev"),

    )

    override fun getUsers(): Flow<List<User>> = flow {
        emit(listOf())
    }

    override fun getTeams(): Flow<List<Team>> = flow {
        emit(teams)
    }

    override fun getNewTeams(): Flow<List<MKCTeam>> = flow {
        emit(listOf())
    }


    override fun getRoster(): Flow<List<MKCLightPlayer>> = flow {
        emit(listOf())
    }


    override fun getWars(): Flow<List<MKWar>> = flow {
        emit(listOf())
    }

    override fun getTopics(): Flow<List<TopicEntity>> = flow {
        emit(listOf())
    }

    override fun getUser(id: String?): Flow<User?> = flow {
        emit(null)
    }

    override fun getTeam(id: String?): Flow<Team?> = flow {
        emit(teams.singleOrNull { it.mid == id })
    }

    override fun getNewTeam(id: String?): Flow<MKCTeam?> = flow {
        emit(null)
    }

    override fun getWar(id: String?): Flow<MKWar?> = flow {
        emit(null)
    }

    override fun writeUsers(list: List<User>): Flow<Unit> = flow {
        emit(Unit)
    }

    override fun writeRoster(list: List<MKCLightPlayer>): Flow<Unit> {
        TODO("Not yet implemented")
    }

    override fun writeTeams(list: List<Team>): Flow<Unit> = flow {
        emit(Unit)
    }

    override fun writeNewTeams(list: List<MKCTeam>): Flow<Unit> {
        TODO("Not yet implemented")
    }

    override fun writeWars(list: List<MKWar>): Flow<Unit> = flow {
        emit(Unit)
    }

    override fun writeUser(user: User): Flow<Unit> = flow {
        emit(Unit)
    }

    override fun writeTeam(team: Team): Flow<Unit> = flow {
        emit(Unit)
    }

    override fun writeWar(war: MKWar): Flow<Unit> = flow {
        emit(Unit)
    }

    override fun writeTopic(topic: TopicEntity): Flow<Unit> = flow {
        emit(Unit)
    }

    override fun deleteUser(user: User): Flow<Unit> = flow {
        emit(Unit)
    }

    override fun deleteTeam(team: Team): Flow<Unit> = flow {
        emit(Unit)
    }

    override fun deleteWar(war: MKWar): Flow<Unit> = flow {
        emit(Unit)
    }

    override fun deleteTopic(topic: String): Flow<Unit> = flow {
        emit(Unit)
    }

    override fun clearWars(): Flow<Unit> = flow {
        emit(Unit)
    }

    override fun clearUsers(): Flow<Unit> = flow {
        emit(Unit)
    }

    override fun clearTeams(): Flow<Unit> = flow {
        emit(Unit)
    }
}