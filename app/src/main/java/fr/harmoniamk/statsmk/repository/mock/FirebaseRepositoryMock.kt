package fr.harmoniamk.statsmk.repository.mock

import fr.harmoniamk.statsmk.model.firebase.NewWar
import fr.harmoniamk.statsmk.model.firebase.Penalty
import fr.harmoniamk.statsmk.model.firebase.Shock
import fr.harmoniamk.statsmk.model.firebase.Team
import fr.harmoniamk.statsmk.model.firebase.User
import fr.harmoniamk.statsmk.model.firebase.WarDispo
import fr.harmoniamk.statsmk.model.local.MKWar
import fr.harmoniamk.statsmk.model.mock.mock
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FirebaseRepositoryMock(
    private val penalties: List<Penalty> = listOf(),
    private val shocks: List<Shock> = listOf(),


    ) : FirebaseRepositoryInterface {
    override fun writeUser(user: User): Flow<Unit> = flow {
        emit(Unit)
    }

    override fun writeNewWar(war: NewWar): Flow<Unit> = flow {
        emit(Unit)
    }

    override fun writeCurrentWar(war: NewWar): Flow<Unit> = flow {
        emit(Unit)
    }


    override fun writeDispo(dispo: WarDispo): Flow<Unit> = flow {
        emit(Unit)
    }

    override fun writeAlly(teamId: String, ally:String): Flow<Unit> = flow {
        emit(Unit)
    }

    override fun getUsers(): Flow<List<User>> = flow {
        emit(listOf())
    }

    override fun getUser(id: String): Flow<User?> = flow{
emit(null)    }

    override fun getTeams(): Flow<List<Team>> = flow {
        emit(listOf())
    }

    override fun getAllies(teamId: String): Flow<List<String>> = flow {
        emit(listOf())
    }

    override fun getNewWars(teamId: String): Flow<List<NewWar>> = flow {
        emit(listOf())
    }

    override fun getCurrentWar(): Flow<MKWar?> = flow {
        emit(null)
    }

    override fun getDispos(): Flow<List<WarDispo>> = flow {
        emit(listOf())
    }

    override fun listenToCurrentWar(): Flow<MKWar?> = flow {
        emit(MKWar(
            NewWar.mock(
                penalties = penalties,
                shocks = shocks
             )
            ).apply { this.name = "HR - Ev" }
        )
    }

    override fun deleteCurrentWar(): Flow<Unit> = flow {
        emit(Unit)
    }
}