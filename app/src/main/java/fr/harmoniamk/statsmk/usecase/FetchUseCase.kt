package fr.harmoniamk.statsmk.usecase

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.harmoniamk.statsmk.extension.withName
import fr.harmoniamk.statsmk.model.firebase.Tag
import fr.harmoniamk.statsmk.model.firebase.User
import fr.harmoniamk.statsmk.model.local.MKWar
import fr.harmoniamk.statsmk.model.local.TeamType
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.async
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
import kotlin.coroutines.CoroutineContext


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
) : FetchUseCaseInterface, CoroutineScope {

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
        }.zip(mkCentralRepository.getTeams("200cc")) { a, b ->
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
            val type =  when {
                team.primary_team_id != null -> TeamType.MultiRoster(teamId = team.primary_team_id.toString().takeIf { it != team.id }, secondaryTeamsId = null)
                !team.secondary_teams.isNullOrEmpty() -> TeamType.MultiRoster(teamId = null, secondaryTeamsId = team.secondary_teams.map { it.id })
                else -> TeamType.SingleRoster(teamId = team.id)
            }
            when (type) {
                is TeamType.MultiRoster -> {
                    //Joueur dans roster secondaire,
                    // on fetch le roster principal et ses joueurs et on boucle
                    // sur son tableau secondaire en répétant l'opération
                    type.mainTeamId?.let {
                        mkCentralRepository.getTeam(it).mapNotNull { (it as? NetworkResponse.Success)?.response }.firstOrNull()?.let {
                            addToRosterList(forceUpdate, team = it).firstOrNull()
                            it.secondary_teams?.map { it.id }?.forEach {
                                mkCentralRepository.getTeam(team.id)
                                    .mapNotNull { (it as? NetworkResponse.Success)?.response }
                                    .firstOrNull()?.let {
                                        addToRosterList(forceUpdate, team = it).firstOrNull()
                                    }
                            }
                        }
                    }
                    // Joueur dans roster principal
                    //On commence par ajouter celui ci puis
                    // on fetch son tableau secondaire en bouclant dessus
                    type.secondaryTeamsId?.let { teamsId ->
                        addToRosterList(forceUpdate, team = team).firstOrNull()
                        teamsId.forEach {
                            mkCentralRepository.getTeam(it)
                                .mapNotNull { (it as? NetworkResponse.Success)?.response }
                                .firstOrNull()?.let {
                                    addToRosterList(forceUpdate, team = it).firstOrNull()
                                }
                        }
                    }
                }
                //Roster unique, on l'ajoute
                else -> addToRosterList(forceUpdate, team = team).firstOrNull()
            }
        }

    private fun addToRosterList(forceUpdate: Boolean, team: MKCFullTeam? = null, allyList: List<String>? = null) = flow<Unit> {
        val rosterList = team?.rosterList.orEmpty()
        val results = mutableListOf<Deferred<MKPlayer?>>()
        if (allyList?.size != players.filter { it.rosterId == "-1" }.size)
            allyList?.forEach {
                results += async {
                    val fbUser = users.singleOrNull { item -> item.mkcId == it }
                    val mkPlayer = when (forceUpdate || !players.map { it.mkcId }.contains(it)) {
                        false -> players.firstOrNull { player -> player.mkcId == it }?.copy(
                            role = fbUser?.role ?: 0,
                            isLeader = "",
                            currentWar = fbUser?.currentWar ?: "-1"
                        )
                        else -> {
                            val player = mkCentralRepository.getPlayer(it).firstOrNull() as? NetworkResponse.Success
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
                    return@async mkPlayer
                }
            }
        if (rosterList.size != players.filter { it.rosterId == team?.id }.size) {
            rosterList.forEach {
                results += async {
                    val fbUser = users.lastOrNull { item -> item.mkcId == it.mkcId.split(".").first() }
                    val mkcPlayer = when (forceUpdate || !players.map { it.mkcId }.contains(it.mkcId)) {
                        false -> players.firstOrNull { player -> player.mkcId == it.mkcId }?.copy(
                            role = fbUser?.role ?: it.role,
                            isLeader = it.isLeader,
                            currentWar = fbUser?.currentWar ?: "-1",
                            rosterId = team?.id ?: "-1"
                        )
                        else -> {
                            val player = mkCentralRepository.getPlayer(it.mkcId).firstOrNull() as? NetworkResponse.Success
                            when (player?.response) {
                                null -> MKPlayer(fbUser)
                                else -> MKPlayer(
                                    player = player.response,
                                    user = fbUser,
                                    rosterId = team?.id ?: "-1"
                                )
                            }
                        }
                    }
                    return@async mkcPlayer
                }
            }
        }
        results.forEach {
            val player = it.await()
            player?.let {  finalRoster.add(it) }
        }
    }

    override fun fetchAllies(forceUpdate: Boolean): Flow<Unit> {
        val team = preferencesRepository.mkcTeam
        val type: TeamType =  when {
            team?.primary_team_id != null -> TeamType.MultiRoster(teamId = team.primary_team_id.toString().takeIf { it != team.id }, secondaryTeamsId = null)
            !team?.secondary_teams.isNullOrEmpty() -> TeamType.MultiRoster(teamId = team?.id, secondaryTeamsId = team?.secondary_teams?.map { it.id })
            else -> TeamType.SingleRoster(teamId = team?.id.toString())
        }
        val alliesFlow = when  {
            type.mainTeamId != team?.id -> firebaseRepository.getAllies(type.mainTeamId.orEmpty()).zip(firebaseRepository.getAllies(team?.id.orEmpty())) { a, b -> a + b }
            else -> firebaseRepository.getAllies(team?.id.orEmpty())
        }

        return alliesFlow
            .onEach {
                addToRosterList(forceUpdate, allyList =  it).firstOrNull()
                finalRosterWithCurrentWar.clear()
                finalRoster.forEach { player ->
                    val currentWar = users.firstOrNull { it.mkcId == player.mkcId }?.currentWar
                    finalRosterWithCurrentWar.add(player.apply { this.currentWar = currentWar ?: "-1" })
                }
            }.flatMapLatest { databaseRepository.writeRoster(finalRosterWithCurrentWar) }
    }

    override fun fetchWars(): Flow<List<MKWar>>  {
        val team = preferencesRepository.mkcTeam
        val type: TeamType =  when {
            team?.primary_team_id != null -> TeamType.MultiRoster(teamId = team.primary_team_id.toString().takeIf { it != team.id }, secondaryTeamsId = null)
            !team?.secondary_teams.isNullOrEmpty() -> TeamType.MultiRoster(teamId = team?.id, secondaryTeamsId = team?.secondary_teams?.map { it.id })
            else -> TeamType.SingleRoster(teamId = team?.id.toString())
        }
        val warFlow = when {
            type.mainTeamId != team?.id -> firebaseRepository.getNewWars(team?.id.orEmpty()).zip(firebaseRepository.getNewWars(type.mainTeamId.orEmpty())) { a, b -> a + b }
            else -> firebaseRepository.getNewWars(team?.id.orEmpty())
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

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO

}