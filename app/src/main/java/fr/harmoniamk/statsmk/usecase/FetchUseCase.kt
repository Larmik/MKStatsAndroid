package fr.harmoniamk.statsmk.usecase

import android.util.Log
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.harmoniamk.statsmk.extension.withName
import fr.harmoniamk.statsmk.model.firebase.User
import fr.harmoniamk.statsmk.model.local.MKWar
import fr.harmoniamk.statsmk.model.network.MKCFullTeam
import fr.harmoniamk.statsmk.model.network.MKCLightPlayer
import fr.harmoniamk.statsmk.model.network.MKCTeam
import fr.harmoniamk.statsmk.repository.AuthenticationRepositoryInterface
import fr.harmoniamk.statsmk.repository.DatabaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.MKCentralRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.zip
import javax.inject.Inject
import javax.inject.Singleton

interface FetchUseCaseInterface {
    fun fetchPlayer(): Flow<MKCFullTeam>
    fun fetchTeams(): Flow<Unit>
    fun fetchPlayers(forceUpdate: Boolean): Flow<Unit>
    fun fetchWars(): Flow<List<MKWar>>
}

@FlowPreview
@ExperimentalCoroutinesApi
@Module
@InstallIn(SingletonComponent::class)
interface FetchUseCaseModule {
    @Binds
    @Singleton
    fun bindRepository(impl: FetchUseCase): FetchUseCaseInterface
}

@FlowPreview
@ExperimentalCoroutinesApi
class FetchUseCase @Inject constructor(
    private val firebaseRepository: FirebaseRepositoryInterface,
    private val preferencesRepository: PreferencesRepositoryInterface,
    private val mkCentralRepository: MKCentralRepositoryInterface,
    private val databaseRepository: DatabaseRepositoryInterface,
    private val authenticationRepository: AuthenticationRepositoryInterface
) : FetchUseCaseInterface {

    private val finalRoster = mutableListOf<MKCLightPlayer>()
    private val finalRosterWithCurrentWar = mutableListOf<MKCLightPlayer>()
    private val allies = mutableListOf<String>()
    private val users = mutableListOf<User>()



    override fun fetchPlayer(): Flow<MKCFullTeam>  =
        firebaseRepository.getUser(authenticationRepository.user?.uid.orEmpty())
    .onEach { Log.d("MKDebugOnly", "fetchUser") }
    .onEach { preferencesRepository.role = it?.role ?: 0 }
    .flatMapLatest { mkCentralRepository.getPlayer(it?.mkcId.orEmpty()) }
    .onEach { preferencesRepository.mkcPlayer = it }
    .flatMapLatest { mkCentralRepository.getTeam(it?.current_teams?.firstOrNull()?.team_id.toString()) }
    .onEach { preferencesRepository.mkcTeam = it }

    override fun fetchTeams(): Flow<Unit> = mkCentralRepository.teams
        .onEach { Log.d("MKDebugOnly", "fetchTeams") }
        .flatMapLatest { databaseRepository.writeNewTeams(it) }
        .flatMapLatest { mkCentralRepository.historicalTeams }
        .flatMapLatest { databaseRepository.writeNewTeams(it) }
        .flatMapLatest { firebaseRepository.getTeams() }
        .flatMapLatest { databaseRepository.writeNewTeams(it.map { MKCTeam(it) }) }

    override fun fetchPlayers(forceUpdate: Boolean): Flow<Unit> = firebaseRepository.getUsers()
        .onEach { Log.d("MKDebugOnly", "fetchPlayers") }
        .onEach {
            users.clear()
            users.addAll(it)
        }
        .flatMapLatest { firebaseRepository.getAllies() }
        .onEach {
            allies.clear()
            allies.addAll(it)
        }
        .mapNotNull { preferencesRepository.mkcTeam?.rosterList }
        .onEach { list ->
            val players = databaseRepository.getRoster().firstOrNull()
            Log.d("MKDebugOnly", "local roster size: ${players?.size}")
            Log.d("MKDebugOnly", "remote roster size: ${allies.size + list.size}")
            if (forceUpdate || allies.size + list.size != players?.size) {
                finalRoster.clear()
                list.forEach {
                    val fbUser = users.singleOrNull { item -> item.mkcId == it.mkcId.split(".").first() }
                    val mkcPlayer = when (forceUpdate) {
                        true -> players?.firstOrNull { player -> player.mkcId == it.mkcId }?.copy(
                            role = fbUser?.role ?: 0,
                            isAlly = 0,
                            isLeader = it.isLeader,
                            currentWar = fbUser?.currentWar ?: "-1"
                        )
                        else ->  {
                            when (val player = mkCentralRepository.getPlayer(it.mkcId).firstOrNull()) {
                                null -> MKCLightPlayer(fbUser)
                                else -> MKCLightPlayer(
                                    player,
                                    fbUser?.role ?: 0,
                                    1,
                                    "",
                                    fbUser?.currentWar ?: "-1"
                                )
                            }
                        }
                    }
                    mkcPlayer?.let {
                        finalRoster.add(it)
                    }
                }
                allies.forEach { allyId ->
                    val fbUser = users.singleOrNull { item -> item.mkcId == allyId }
                    val mkPlayer = when (forceUpdate) {
                        true -> players?.firstOrNull { player -> player.mkcId == allyId }?.copy(
                            role = fbUser?.role ?: 0,
                            isAlly = 1,
                            isLeader = "",
                            currentWar = fbUser?.currentWar ?: "-1"
                        )
                        else ->  {
                            when (val player = mkCentralRepository.getPlayer(allyId).firstOrNull()) {
                                null -> MKCLightPlayer(fbUser)
                                else -> MKCLightPlayer(
                                    player,
                                    fbUser?.role ?: 0,
                                    1,
                                    "",
                                    fbUser?.currentWar ?: "-1"
                                )
                            }
                        }
                    }
                    mkPlayer?.let {
                        finalRoster.add(it)
                    }
                }
            }
        }
        .onEach {
            delay(1000)
            finalRosterWithCurrentWar.clear()
            finalRoster.forEach { player ->
                val currentWar = users.firstOrNull { it.mkcId == player.mkcId }?.currentWar
                finalRosterWithCurrentWar.add(player.apply { this.currentWar = currentWar ?: "-1" })
            }
        }.flatMapLatest {
            databaseRepository.clearRoster()
            databaseRepository.writeRoster(finalRosterWithCurrentWar)
        }

    override fun fetchWars(): Flow<List<MKWar>> = firebaseRepository.getNewWars()
        .onEach { Log.d("MKDebugOnly", "fetchWars") }
        .zip(databaseRepository.getWars()) { remoteDb, localDb ->
            val finalLocalDb =
                localDb.filter { it.war?.teamHost == preferencesRepository.mkcTeam?.id }
            Log.d("MKDebugOnly", "local war size: ${finalLocalDb.size}")
            Log.d("MKDebugOnly", "remote war size: ${remoteDb.size}")
            when (finalLocalDb.size == remoteDb.size) {
                true -> localDb
                else -> remoteDb
                    .map { MKWar(it) }
                    .withName(databaseRepository)
                    .onEach { databaseRepository.writeWars(it).first() }
                    .first()
            }
        }

}