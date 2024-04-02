package fr.harmoniamk.statsmk.repository.mock

import fr.harmoniamk.statsmk.database.entities.TopicEntity
import fr.harmoniamk.statsmk.model.local.MKWar
import fr.harmoniamk.statsmk.model.network.MKPlayer
import fr.harmoniamk.statsmk.model.network.MKCTeam
import fr.harmoniamk.statsmk.repository.DatabaseRepositoryInterface
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class DatabaseRepositoryMock : DatabaseRepositoryInterface {


    override fun getNewTeams(): Flow<List<MKCTeam>> = flow {
        emit(listOf())
    }


    override fun getRoster(): Flow<List<MKPlayer>> = flow {
        emit(listOf())
    }


    override fun getWars(): Flow<List<MKWar>> = flow {
        emit(listOf())
    }


    override fun getNewTeam(id: String?): Flow<MKCTeam?> = flow {
        emit(null)
    }

    override fun getNewUser(id: String?): Flow<MKPlayer?> = flow {
        emit(null)
    }

    override fun getWar(id: String?): Flow<MKWar?> = flow {
        emit(null)
    }


    override fun writeRoster(list: List<MKPlayer>): Flow<Unit> {
        TODO("Not yet implemented")
    }


    override fun writeNewTeams(list: List<MKCTeam>): Flow<Unit> {
        TODO("Not yet implemented")
    }

    override fun writeWars(list: List<MKWar>): Flow<Unit> = flow {
        emit(Unit)
    }

    override fun updateUser(user: MKPlayer): Flow<Unit> = flow {
        emit(Unit)
    }

    override fun writeUser(user: MKPlayer): Flow<Unit> = flow {
        emit(Unit)
    }

    override fun writeWar(war: MKWar): Flow<Unit> = flow {
        emit(Unit)
    }

    override fun writeTopic(topic: TopicEntity): Flow<Unit> = flow {
        emit(Unit)
    }

    override fun deleteTopic(topic: String): Flow<Unit> = flow {
        emit(Unit)
    }

    override fun clear(): Flow<Unit> = flow {
        emit(Unit)
    }

    override fun clearRoster(): Flow<Unit> = flow {
        emit(Unit)
    }


}