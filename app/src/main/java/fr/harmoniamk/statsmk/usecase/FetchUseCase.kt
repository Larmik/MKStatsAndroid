package fr.harmoniamk.statsmk.usecase

import android.util.Log
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.harmoniamk.statsmk.extension.withName
import fr.harmoniamk.statsmk.model.firebase.User
import fr.harmoniamk.statsmk.model.local.MKWar
import fr.harmoniamk.statsmk.model.network.MKCLightPlayer
import fr.harmoniamk.statsmk.model.network.MKCTeam
import fr.harmoniamk.statsmk.repository.DatabaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.MKCentralRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
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
    fun fetch(id: String): Flow<List<MKWar>>
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
    private val databaseRepository: DatabaseRepositoryInterface
) : FetchUseCaseInterface {

    private val finalRoster = mutableListOf<MKCLightPlayer>()
    private val finalRosterWithCurrentWar = mutableListOf<MKCLightPlayer>()
    private val allies = mutableListOf<String>()
    private val users = mutableListOf<User>()

    override fun fetch(id: String) = firebaseRepository.getUser(id)
        .onEach { Log.d("MKDebugOnly", "fetchUser $id") }
        .onEach { preferencesRepository.role = it?.role ?: 0 }
        .flatMapLatest { mkCentralRepository.getPlayer(it?.mkcId.orEmpty()) }
        .onEach { preferencesRepository.mkcPlayer = it }
        .flatMapLatest { mkCentralRepository.getTeam(it?.current_teams?.firstOrNull()?.team_id.toString()) }
        .onEach { preferencesRepository.mkcTeam = it }
        .flatMapLatest { firebaseRepository.getUsers() }
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
            if (allies.size + list.size != players?.size) {
                finalRoster.clear()
                list.forEach {
                    val mkcPlayer = mkCentralRepository.getPlayer(it.mkcId).first()
                    val fbUser =
                        users.singleOrNull { item -> item.mkcId == it.mkcId.split(".").first() }
                    finalRoster.add(
                        MKCLightPlayer(
                            mkcPlayer,
                            fbUser?.role ?: 0,
                            0,
                            it.isLeader,
                            fbUser?.currentWar ?: "-1"
                        )
                    )
                }
                allies.forEach { allyId ->
                    val mkPlayer = mkCentralRepository.getPlayer(allyId).firstOrNull()
                    val fbUser = users.singleOrNull { item -> item.mkcId == allyId }
                    when {
                        mkPlayer != null -> finalRoster.add(
                            MKCLightPlayer(
                                mkPlayer,
                                fbUser?.role ?: 0,
                                1,
                                "",
                                fbUser?.currentWar ?: "-1"
                            )
                        )
                        fbUser != null -> finalRoster.add(MKCLightPlayer(fbUser))
                    }
                }
            }
        }
        .onEach {
            finalRosterWithCurrentWar.clear()
            finalRoster.forEach { player ->
                val currentWar = users.singleOrNull { it.mkcId == player.mkcId }?.currentWar
                finalRosterWithCurrentWar.add(player.apply { this.currentWar = currentWar ?: "-1" })
            }
        }.flatMapLatest { databaseRepository.writeRoster(finalRosterWithCurrentWar) }
        .flatMapLatest { mkCentralRepository.teams }
        .onEach { Log.d("MKDebugOnly", "fetchTeams") }
        .flatMapLatest { databaseRepository.writeNewTeams(it) }
        .flatMapLatest { firebaseRepository.getTeams() }
        .flatMapLatest { databaseRepository.writeNewTeams(it.map { MKCTeam(it) }) }
        .flatMapLatest { firebaseRepository.getNewWars(preferencesRepository.mkcTeam?.id.orEmpty()) }
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