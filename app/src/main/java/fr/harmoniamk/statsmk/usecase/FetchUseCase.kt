package fr.harmoniamk.statsmk.usecase

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.harmoniamk.statsmk.extension.withName
import fr.harmoniamk.statsmk.model.firebase.Tag
import fr.harmoniamk.statsmk.model.firebase.User
import fr.harmoniamk.statsmk.model.local.MKWar
import fr.harmoniamk.statsmk.model.network.MKCFullPlayer
import fr.harmoniamk.statsmk.model.network.MKCFullTeam
import fr.harmoniamk.statsmk.model.network.MKCTeam
import fr.harmoniamk.statsmk.model.network.MKPlayer
import fr.harmoniamk.statsmk.model.network.NetworkResponse
import fr.harmoniamk.statsmk.repository.AuthenticationRepositoryInterface
import fr.harmoniamk.statsmk.repository.DatabaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.MKCentralRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.zip
import javax.inject.Inject
import javax.inject.Singleton


interface FetchUseCaseInterface {
    fun fetchPlayer(mkcId: String? = null): Flow<NetworkResponse<MKCFullPlayer>>
    fun fetchTeam(): Flow<NetworkResponse<MKCFullTeam>>
    fun fetchTeams(): Flow<Unit>
    fun fetchPlayers(forceUpdate: Boolean): Flow<MKCFullTeam>
    fun fetchAllies(forceUpdate: Boolean): Flow<Unit>
    fun fetchWars(): Flow<List<MKWar>>
    fun fetchTags(): Flow<Unit>
    fun purgePlayers(): Flow<Unit>
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

    private val finalRoster = mutableListOf<MKPlayer>()
    private val finalRosterWithCurrentWar = mutableListOf<MKPlayer>()

    private val users = mutableListOf<User>()
    private val players = mutableListOf<MKPlayer>()

    override fun fetchPlayer(mkcId: String?): Flow<NetworkResponse<MKCFullPlayer>> =
        when (mkcId) {
            null ->
                firebaseRepository.getUser(authenticationRepository.user?.uid.orEmpty())
                    .filterNotNull()
                    .onEach { it.role?.let { role -> preferencesRepository.role = role } }
                    .flatMapLatest {  mkCentralRepository.getPlayer(it.mkcId.orEmpty()) }
            else ->  mkCentralRepository.getPlayer(mkcId)
        }.onEach {
            preferencesRepository.mkcPlayer = (it as? NetworkResponse.Success)?.response
            firebaseRepository.getCoffees().firstOrNull()?.let { list ->
                var total = 0
                list.filter { coffee -> coffee.userId == authenticationRepository.user?.uid }.forEach { coffee ->
                    when (coffee.productId) {
                        "a_coffee" -> total += coffee.quantity
                        "three_coffees" -> total += coffee.quantity * 3
                        "five_coffees" -> total += coffee.quantity * 5
                        "ten_coffees" -> total += coffee.quantity * 10
                    }
                }
                preferencesRepository.coffees = total
            }
        }


    override fun fetchTeam(): Flow<NetworkResponse<MKCFullTeam>> = mkCentralRepository
        .getTeam(preferencesRepository.mkcPlayer?.current_teams?.firstOrNull()?.team_id.toString())
        .onEach { preferencesRepository.mkcTeam = (it as? NetworkResponse.Success)?.response }

    override fun fetchTeams(): Flow<Unit>  {
        val teamList = mutableListOf<MKCTeam>()
        val teamFlow = mkCentralRepository.getTeams("150cc").zip(mkCentralRepository.getTeams("historical")) { a, b ->
            teamList.addAll((a as? NetworkResponse.Success)?.response.orEmpty())
            teamList.addAll((b as? NetworkResponse.Success)?.response.orEmpty())
        }.flatMapLatest { firebaseRepository.getTeams() }
            .map {
                teamList.addAll(it.map { MKCTeam(it) })
                teamList.toList()
            }
        return teamFlow
            .zip(databaseRepository.getNewTeams()) { remoteDb, localDb ->
                when (localDb.size == remoteDb.size) {
                    true -> return@zip localDb
                    else -> {
                        databaseRepository.writeNewTeams(remoteDb).first()
                        return@zip remoteDb
                    }
                }
            }.map {  }
    }

    override fun fetchPlayers(forceUpdate: Boolean): Flow<MKCFullTeam> = firebaseRepository.getUsers()
        .zip(databaseRepository.getRoster()) { userList, playerList ->
            finalRoster.clear()
            finalRosterWithCurrentWar.clear()
            users.clear()
            players.clear()
            users.addAll(userList)
            players.addAll(playerList)
        }
        .mapNotNull { preferencesRepository.mkcTeam }
        .onEach { team ->
            when {
                team.primary_team_id != null -> {
                    addToRosterList(forceUpdate, team).firstOrNull()
                    mkCentralRepository.getTeam(team.primary_team_id.toString()).mapNotNull { (it as? NetworkResponse.Success)?.response }.firstOrNull()?.let {
                        addToRosterList(forceUpdate, it).firstOrNull()
                        it.secondary_teams?.forEach { team ->
                            mkCentralRepository.getTeam(team.id)
                                .mapNotNull { (it as? NetworkResponse.Success)?.response }
                                .firstOrNull()?.let {
                                    addToRosterList(forceUpdate, it).firstOrNull()
                            }
                        }
                    }
                }
                (team.secondary_teams?.size ?: 0) > 0 -> {
                    addToRosterList(forceUpdate, team).firstOrNull()
                    team.secondary_teams?.forEach { secondaryTeam ->
                        mkCentralRepository.getTeam(secondaryTeam.id)
                            .mapNotNull { (it as? NetworkResponse.Success)?.response }
                            .firstOrNull()?.let {
                                addToRosterList(forceUpdate, it).firstOrNull()
                            }
                    }
                }
                else -> addToRosterList(forceUpdate, team).firstOrNull()
            }
        }

    private  fun addToRosterList(forceUpdate: Boolean, team: MKCFullTeam) = flow<Unit> {
        val rosterList = team.rosterList.orEmpty()
        if (rosterList.size != players.filter { it.rosterId == team.id }.size) {
            rosterList.forEach {
                val fbUser = users.lastOrNull { item -> item.mkcId == it.mkcId.split(".").first() }
                val mkcPlayer = when (forceUpdate || !players.map { it.mkcId }.contains(it.mkcId)) {
                    false -> players.firstOrNull { player -> player.mkcId == it.mkcId }?.copy(
                        role = fbUser?.role ?: it.role,
                        isLeader = it.isLeader,
                        currentWar = fbUser?.currentWar ?: "-1",
                        rosterId = team.id
                    )
                    else -> {
                        val player = mkCentralRepository.getPlayer(it.mkcId).firstOrNull() as? NetworkResponse.Success
                        when (player?.response) {
                            null -> MKPlayer(fbUser)
                            else -> MKPlayer(
                                player = player.response,
                                user = fbUser,
                                rosterId = team.id
                            )
                        }
                    }
                }
                mkcPlayer?.let { finalRoster.add(it) }
            }
        }
    }

    override fun fetchAllies(forceUpdate: Boolean): Flow<Unit> {
        val alliesFlow = when {
            preferencesRepository.mkcTeam?.primary_team_id != null -> firebaseRepository.getAllies(preferencesRepository.mkcTeam?.primary_team_id.toString())
            (preferencesRepository.mkcTeam?.secondary_teams?.size ?: 0) > 0 -> firebaseRepository.getAllies(preferencesRepository.mkcTeam?.secondary_teams!!.getOrNull(0)?.id.toString())
            else -> firebaseRepository.getAllies(preferencesRepository.mkcTeam?.id.orEmpty())
        }
        return alliesFlow
            .onEach {
                if (it.size != players.filter { it.rosterId == "-1" }.size) {
                    it.forEach { allyId ->
                        val fbUser = users.singleOrNull { item -> item.mkcId == allyId }
                        val mkPlayer = when (forceUpdate || !players.map { it.mkcId }.contains(allyId)) {
                            false -> players.firstOrNull { player -> player.mkcId == allyId }?.copy(
                                role = fbUser?.role ?: 0,
                                isLeader = "",
                                currentWar = fbUser?.currentWar ?: "-1"
                            )
                            else -> {
                                val player = mkCentralRepository.getPlayer(allyId).firstOrNull() as? NetworkResponse.Success
                                when (player?.response) {
                                    null -> MKPlayer(fbUser)
                                    else -> MKPlayer(
                                        player = player.response,
                                        user = fbUser,
                                        rosterId = "-1"
                                    )
                                }
                            }
                        }
                        mkPlayer?.let { finalRoster.add(it) }
                    }
                }
            }
            .onEach {
                finalRosterWithCurrentWar.clear()
                finalRoster.forEach { player ->
                    val currentWar = users.firstOrNull { it.mkcId == player.mkcId }?.currentWar
                    finalRosterWithCurrentWar.add(player.apply { this.currentWar = currentWar ?: "-1" })
                }
            }.flatMapLatest { databaseRepository.writeRoster(finalRosterWithCurrentWar) }
    }

    override fun fetchWars(): Flow<List<MKWar>>  {
        val warFlow = when {
            preferencesRepository.mkcTeam?.primary_team_id != null -> firebaseRepository.getNewWars(preferencesRepository.mkcTeam?.primary_team_id.toString()).zip(firebaseRepository.getNewWars(preferencesRepository.mkcTeam?.id.orEmpty())) { a, b -> a + b }
            (preferencesRepository.mkcTeam?.secondary_teams?.size ?: 0) > 0 -> firebaseRepository.getNewWars(
                preferencesRepository.mkcTeam?.secondary_teams!!.getOrNull(0)?.id.toString()).zip(firebaseRepository.getNewWars(preferencesRepository.mkcTeam?.id.orEmpty())) { a, b -> a + b }
            else -> firebaseRepository.getNewWars(preferencesRepository.mkcTeam?.id.orEmpty())
        }
        return warFlow
            .zip(databaseRepository.getWars()) { remoteDb, localDb ->
                val finalLocalDb = localDb.filter { it.hasTeam(preferencesRepository.mkcTeam) }
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

    override fun fetchTags(): Flow<Unit> = mkCentralRepository.getTeams("150cc")
        .mapNotNull { (it as? NetworkResponse.Success)?.response }
        .map { it.map { Tag(it.team_tag, it.team_id) } }
        .zip(firebaseRepository.getTeams()) { a, b ->  a + b.map { Tag(it.shortName.orEmpty(), it.mid)  } }
        .flatMapLatest { firebaseRepository.writeTags(it) }

    override fun purgePlayers(): Flow<Unit> =
        firebaseRepository.getUsers()
            .map { it.filter { user -> user.mid == user.mkcId && (user.discordId?.length ?: 0) < 10 } }
            .flatMapLatest { firebaseRepository.deleteUser(it) }

}