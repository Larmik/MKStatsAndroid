package fr.harmoniamk.statsmk.compose.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.extension.isTrue
import fr.harmoniamk.statsmk.extension.positionToPoints
import fr.harmoniamk.statsmk.extension.withFullStats
import fr.harmoniamk.statsmk.extension.withFullTeamStats
import fr.harmoniamk.statsmk.model.local.MKStats
import fr.harmoniamk.statsmk.model.local.MKWar
import fr.harmoniamk.statsmk.model.local.MKWarPosition
import fr.harmoniamk.statsmk.model.local.MKWarTrack
import fr.harmoniamk.statsmk.model.local.MapDetails
import fr.harmoniamk.statsmk.model.local.MapStats
import fr.harmoniamk.statsmk.model.local.Stats
import fr.harmoniamk.statsmk.model.network.MKPlayer
import fr.harmoniamk.statsmk.repository.DatabaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class Periodics{
    Week, Month, All
}

sealed class StatsType(val title: Int) {
    class IndivStats(val userId: String) : StatsType(R.string.statistiques_du_joueur)
    class TeamStats : StatsType(R.string.statistiques_de_l_quipe)
    class OpponentStats(
        val teamId: String,
        val userId: String? = null) : StatsType(R.string.statistiques_de_l_quipe)
    class MapStats(
        val userId: String? = null,
        val teamId: String? = null,
        val periodic: String = "All",
        val trackIndex: Int
    ) : StatsType(R.string.statistiques_circuit)
}

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class StatsViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepositoryInterface,
    private val databaseRepository: DatabaseRepositoryInterface
) : ViewModel() {

    private val _sharedStats = MutableStateFlow<MKStats?>(null)
    private val _sharedSubtitle = MutableStateFlow<String?>(null)
    private val _sharedWarDetailsClick = MutableSharedFlow<Unit>()
    private val _sharedTrackDetailsClick = MutableSharedFlow<Unit>()
    private val _sharedPeriodEnabled = MutableStateFlow(Periodics.All.name)

    val sharedStats = _sharedStats.asStateFlow()
    val sharedSubtitle = _sharedSubtitle.asStateFlow()
    val sharedPeriodEnabled = _sharedPeriodEnabled.asStateFlow()
    val sharedWarDetailsClick = _sharedWarDetailsClick.asSharedFlow()
    val sharedTrackDetailsClick = _sharedTrackDetailsClick.asSharedFlow()

    private val users = mutableListOf<MKPlayer>()
    private val wars = mutableListOf<MKWar>()
    private var item: Stats? = null
    var onlyIndiv =  preferencesRepository.mkcTeam?.id == null

    fun init(type: StatsType, periodic: String) {
        _sharedPeriodEnabled.value = (type as? StatsType.MapStats)?.periodic ?: periodic
        val warFlow = databaseRepository.getRoster()
            .onEach {
                users.clear()
                users.addAll(it)
            }
            .flatMapLatest { databaseRepository.getWars() }
            .map {
                when  {
                    type is StatsType.IndivStats -> it.filter { war ->
                        war.hasPlayer(type.userId.split(".").firstOrNull())
                                && (periodic == Periodics.All.name
                                || periodic == Periodics.Week.name && war.isThisWeek
                                || periodic == Periodics.Month.name && war.isThisMonth)
                    }
                    type is StatsType.TeamStats -> it.filter {
                        war -> war.hasTeam(preferencesRepository.mkcTeam)
                            && (periodic == Periodics.All.name
                            || periodic == Periodics.Week.name && war.isThisWeek
                            || periodic == Periodics.Month.name && war.isThisMonth)
                    }
                    (type as? StatsType.OpponentStats)?.userId != null -> it.filter { war -> war.hasTeam((type as? StatsType.OpponentStats)?.teamId) && war.hasPlayer((type as? StatsType.OpponentStats)?.userId?.split(".")?.firstOrNull()) }
                    type is StatsType.OpponentStats -> it.filter { war -> war.hasTeam((type as? StatsType.OpponentStats)?.teamId) }
                    else -> it
                }
            }
            .filterNot { it.isEmpty() }
            .onEach {
                wars.clear()
                wars.addAll(it)
            }
            .flatMapLatest {
                when {
                    type is StatsType.IndivStats -> it.withFullStats(databaseRepository, type.userId.split(".").firstOrNull())
                    type is StatsType.OpponentStats -> databaseRepository.getNewTeam(type.teamId)
                        .filterNotNull()
                        .flatMapLatest { it.withFullTeamStats(wars, databaseRepository, type.userId?.split(".")?.firstOrNull(), isIndiv = true) }
                    else -> it.withFullStats(databaseRepository)
                }
            }
            .shareIn(viewModelScope, SharingStarted.Lazily)

        warFlow
            .filter { type is StatsType.OpponentStats }
            .onEach {
                item = it
                val finalList = mutableListOf<Pair<Int, String?>>()
                it.warStats.list.forEach { war ->
                    val positions = mutableListOf<Pair<MKPlayer?, Int>>()
                    war.warTracks?.forEach { warTrack ->
                        warTrack.track?.warPositions?.let { warPositions ->
                            val trackPositions = mutableListOf<MKWarPosition>()
                            warPositions.forEach { position ->
                                trackPositions.add(MKWarPosition(position = position, mkcPlayer = users.singleOrNull { it.mkcId ==  position.playerId }))
                            }
                            trackPositions.groupBy { it.mkcPlayer }.entries.forEach { entry ->
                                positions.add(Pair(entry.key, entry.value.map { pos -> pos.position.position.positionToPoints() }.sum()))
                            }
                        }
                    }
                    positions
                        .groupBy { it.first }
                        .map { Pair(it.key, it.value.map { it.second }.sum()) }
                        .filter { it.first?.mkcId == (type as? StatsType.OpponentStats)?.userId }
                        .map { Pair(it.second, war.war?.createdDate) }
                        .forEach { pair -> finalList.add(pair) }
                }
                _sharedStats.emit(it.apply {
                    this.highestPlayerScore = finalList.maxByOrNull { it.first }
                    this.lowestPlayerScore = finalList.minByOrNull { it.first }
                })
            }
            .flatMapLatest { databaseRepository.getNewUser((type as? StatsType.OpponentStats)?.userId).zip(databaseRepository.getNewTeam((type as? StatsType.OpponentStats)?.teamId)) { user, team ->
                _sharedSubtitle.value = when {
                    user != null && team != null -> "${user.name} vs ${team.team_name}"
                    else -> team?.team_name.orEmpty()
                }
            } }
            .launchIn(viewModelScope)

        warFlow
            .filter { type is StatsType.IndivStats || type is StatsType.TeamStats }
            .onEach {
                _sharedStats.emit(it)
                _sharedSubtitle.value = when (type) {
                    is StatsType.IndivStats -> users.singleOrNull { it.mkcId == (type as? StatsType.IndivStats)?.userId }?.name
                    else -> preferencesRepository.mkcTeam?.team_name
                }
            }.launchIn(viewModelScope)

        warFlow
            .filter { type is StatsType.MapStats }
            .map {
                onlyIndiv = (type as StatsType.MapStats).userId != null || preferencesRepository.mkcTeam?.id == null
                when {
                    onlyIndiv -> wars.filter { war -> war.hasPlayer(type.userId?.split(".")?.firstOrNull()) }
                    type.userId != null && type.teamId?.takeIf { it.isNotEmpty() } != null -> wars.filter { war -> war.hasPlayer(type.userId.split(".").firstOrNull()) && war.hasTeam(type.teamId) }
                    type.teamId?.takeIf { it.isNotEmpty() } != null -> wars.filter { war -> war.hasTeam(type.teamId) }
                    else -> wars.filter { war ->  war.hasTeam(preferencesRepository.mkcTeam) }
                }
            }
            .filter {
                (!onlyIndiv && it.map { war -> war.hasTeam(preferencesRepository.mkcTeam) }.any { it })
                        || onlyIndiv
            }
            .mapNotNull { list -> list
                .filter {  (onlyIndiv && it.hasPlayer((type as StatsType.MapStats).userId?.split(".")?.firstOrNull())) || !onlyIndiv && it.hasTeam(preferencesRepository.mkcTeam) }
                .filter {  periodic == Periodics.All.name || (periodic == Periodics.Week.name && it.isThisWeek) || periodic == Periodics.Month.name && it.isThisMonth }
            }
            .map {
                val finalList = mutableListOf<MapDetails>()
                it.forEach { mkWar ->
                    mkWar.warTracks?.filter { track -> track.index == (type as StatsType.MapStats).trackIndex }?.forEach { track ->
                        val position = track.track?.warPositions?.singleOrNull { it.playerId == (type as StatsType.MapStats).userId }?.position?.takeIf { (type as StatsType.MapStats).userId != null }
                        finalList.add(MapDetails(
                            war = mkWar,
                            warTrack = MKWarTrack(track.track),
                            position = position
                        ))
                    }
                }
                finalList
            }
            .filter { it.isNotEmpty() }
            .onEach {
                val mapDetailsList = mutableListOf<MapDetails>()
                mapDetailsList.addAll(it
                    .filter { !onlyIndiv || (onlyIndiv && it.war.war?.warTracks?.any { MKWarTrack(it).hasPlayer((type as StatsType.MapStats).userId?.split(".")?.firstOrNull()) }.isTrue) }
                    .filter { (type as StatsType.MapStats).teamId?.takeIf { it.isNotEmpty() } == null || it.war.hasTeam(type.teamId) }

                )
                _sharedStats.value = MapStats(
                    list = mapDetailsList,
                    isIndiv = onlyIndiv && (type as StatsType.MapStats).userId != null,
                    userId = (type as StatsType.MapStats).userId?.split(".")?.firstOrNull()
                )
            }
            .flatMapLatest { databaseRepository.getNewUser((type as? StatsType.MapStats)?.userId).zip(databaseRepository.getNewTeam((type as? StatsType.MapStats)?.teamId)) { user, team ->
                _sharedSubtitle.value = when {
                    user != null && team != null -> "${user.name} vs ${team.team_name}"
                    user != null  -> user.name
                    team != null -> team.team_name
                    else -> null
                }
            } }
            .launchIn(viewModelScope)
    }

    fun onDetailsWarClick() {
        viewModelScope.launch {
            _sharedWarDetailsClick.emit(Unit)
        }
    }
    fun onDetailsTrackClick() {
        viewModelScope.launch {
            _sharedTrackDetailsClick.emit(Unit)
        }
    }

}