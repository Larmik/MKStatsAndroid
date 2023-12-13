package fr.harmoniamk.statsmk.compose.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.extension.isTrue
import fr.harmoniamk.statsmk.extension.positionToPoints
import fr.harmoniamk.statsmk.extension.withFullStats
import fr.harmoniamk.statsmk.extension.withFullTeamStats
import fr.harmoniamk.statsmk.fragment.stats.opponentRanking.OpponentRankingItemViewModel
import fr.harmoniamk.statsmk.model.firebase.User
import fr.harmoniamk.statsmk.model.local.MKStats
import fr.harmoniamk.statsmk.model.local.MKWar
import fr.harmoniamk.statsmk.model.local.MKWarPosition
import fr.harmoniamk.statsmk.model.local.MKWarTrack
import fr.harmoniamk.statsmk.model.local.MapDetails
import fr.harmoniamk.statsmk.model.local.MapStats
import fr.harmoniamk.statsmk.model.local.Stats
import fr.harmoniamk.statsmk.model.local.TrackStats
import fr.harmoniamk.statsmk.model.network.MKCLightPlayer
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

sealed class StatsType(val title: Int) {
    class IndivStats(val userId: String) : StatsType(R.string.statistiques_du_joueur)
    class TeamStats : StatsType(R.string.statistiques_de_l_quipe)
    class OpponentStats(
        val teamId: String,
        val userId: String? = null) : StatsType(R.string.statistiques_de_l_quipe)
    class MapStats(
        val userId: String? = null,
        val teamId: String? = null,
        val isWeek: Boolean = false,
        val isMonth: Boolean = false,
        val trackIndex: Int
    ) : StatsType(R.string.statistiques_circuit)
    class PeriodicStats(val isWeek: Boolean = true) : StatsType(R.string.statistiques_p_riodiques)
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
    private val _sharedOwnTeamId = MutableStateFlow<String?>(null)
    private var _sharedWeekEnabled = MutableStateFlow(true)

    val sharedStats = _sharedStats.asStateFlow()
    val sharedSubtitle = _sharedSubtitle.asStateFlow()
    val sharedWeekEnabled = _sharedWeekEnabled.asStateFlow()
    val sharedWarDetailsClick = _sharedWarDetailsClick.asSharedFlow()
    val sharedTrackDetailsClick = _sharedTrackDetailsClick.asSharedFlow()
    val sharedOwnTeamId = _sharedOwnTeamId.asStateFlow()

    private var bestMap: TrackStats? = null
    private var worstMap: TrackStats? = null
    private var mostPlayedMap: TrackStats? = null
    private var highestVicory: MKWar? = null
    private var loudestDefeat: MKWar? = null
    private var mostPlayedTeam: OpponentRankingItemViewModel? = null
    private var mostDefeatedTeam: OpponentRankingItemViewModel? = null
    private var lessDefeatedTeam: OpponentRankingItemViewModel? = null

    private val users = mutableListOf<MKCLightPlayer>()
    private val wars = mutableListOf<MKWar>()
    private var item: Stats? = null
    var onlyIndiv =  preferencesRepository.mkcTeam?.id == null

    fun init(type: StatsType, isWeek: Boolean) {
        _sharedWeekEnabled.value = isWeek
        val warFlow = databaseRepository.getRoster()
            .onEach {
                users.clear()
                users.addAll(it)
            }
            .flatMapLatest { databaseRepository.getWars() }
            .map {
                when  {
                    type is StatsType.IndivStats -> it.filter { war -> war.hasPlayer(type.userId.split(".").firstOrNull()) }
                    type is StatsType.TeamStats -> it.filter { war -> war.hasTeam(preferencesRepository.mkcTeam?.id.toString()) }
                    (type as? StatsType.OpponentStats)?.userId != null -> it.filter { war -> war.hasTeam((type as? StatsType.OpponentStats)?.teamId) && war.hasPlayer((type as? StatsType.OpponentStats)?.userId?.split(".")?.firstOrNull()) }
                    type is StatsType.OpponentStats -> it.filter { war -> war.hasTeam((type as? StatsType.OpponentStats)?.teamId) }
                    type is StatsType.PeriodicStats && isWeek -> it.filter { war -> war.hasTeam(preferencesRepository.mkcTeam?.id.toString()) && war.isThisWeek  }
                    type is StatsType.PeriodicStats -> it.filter { war -> war.hasTeam(preferencesRepository.mkcTeam?.id.toString()) && war.isThisMonth  }
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
                    type is StatsType.OpponentStats -> databaseRepository.getNewTeam(type.teamId).filterNotNull().flatMapLatest { it.withFullTeamStats(wars, databaseRepository, type.userId?.split(".")?.firstOrNull(), isIndiv = true) }
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
                    val positions = mutableListOf<Pair<MKCLightPlayer?, Int>>()
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
            .flatMapLatest { databaseRepository.getNewTeam((type as StatsType.OpponentStats).teamId) }
            .onEach { _sharedSubtitle.value = it?.team_name }
            .launchIn(viewModelScope)

        warFlow
            .filter { type is StatsType.IndivStats || type is StatsType.TeamStats || type is StatsType.PeriodicStats }
            .onEach {
                bestMap = it.bestPlayerMap
                worstMap = it.worstPlayerMap
                mostPlayedMap = it.mostPlayedMap
                highestVicory = it.warStats.highestVictory
                loudestDefeat = it.warStats.loudestDefeat
                _sharedOwnTeamId.value = preferencesRepository.mkcTeam?.id?.takeIf { type is StatsType.TeamStats || type is StatsType.PeriodicStats}
                _sharedStats.emit(it)
                _sharedSubtitle.value = when (type) {
                    is StatsType.IndivStats -> users.singleOrNull { it.mkcId == (type as? StatsType.IndivStats)?.userId }?.name
                    else -> preferencesRepository.mkcTeam?.team_name
                }
            }
            .flatMapLatest {
                when (type) {
                    is StatsType.IndivStats -> listOfNotNull(
                        it.mostPlayedTeam?.team,
                        it.mostDefeatedTeam?.team,
                        it.lessDefeatedTeam?.team
                    ).withFullTeamStats(wars, databaseRepository, type.userId.split(".").firstOrNull())
                    else -> listOfNotNull(
                        it.mostPlayedTeam?.team,
                        it.mostDefeatedTeam?.team,
                        it.lessDefeatedTeam?.team
                    ).withFullTeamStats(wars, databaseRepository)
                }
            }
            .onEach {
                mostPlayedTeam = it.getOrNull(0)
                mostDefeatedTeam = it.getOrNull(1)
                lessDefeatedTeam = it.getOrNull(2)
            }.launchIn(viewModelScope)

        warFlow
            .filter { type is StatsType.MapStats }
            .map {
                onlyIndiv = (type as StatsType.MapStats).userId != null || preferencesRepository.mkcTeam?.id == null
                when {
                    onlyIndiv -> wars.filter { war -> war.hasPlayer(type.userId?.split(".")?.firstOrNull()) }
                    type.userId != null && type.teamId != null -> wars.filter { war -> war.hasPlayer(type.userId.split(".").firstOrNull()) && war.hasTeam(type.teamId) }
                    else -> wars.filter { war -> war.hasTeam(type.teamId ?: preferencesRepository.mkcTeam?.id) }
                }
            }
            .filter {
                (!onlyIndiv && it.mapNotNull { war -> war.war?.teamHost}.contains(preferencesRepository.mkcTeam?.id)
                        || it.map {war -> war.war?.teamOpponent}.contains(preferencesRepository.mkcTeam?.id))
                        || onlyIndiv
            }
            .mapNotNull { list -> list
                .filter {  (onlyIndiv && it.hasPlayer((type as StatsType.MapStats).userId?.split(".")?.firstOrNull())) || !onlyIndiv && it.hasTeam(preferencesRepository.mkcTeam?.id) }
                .filter {  !(type as StatsType.MapStats).isWeek.isTrue || (type.isWeek.isTrue && it.isThisWeek) }
                .filter {  !(type as StatsType.MapStats).isMonth.isTrue || (type.isMonth.isTrue && it.isThisMonth) }
            }
            .map {
                val finalList = mutableListOf<MapDetails>()
                it.forEach { mkWar ->
                    mkWar.warTracks?.filter { track -> track.index == (type as StatsType.MapStats).trackIndex }?.forEach { track ->
                        val position = track.track?.warPositions?.singleOrNull { it.playerId == (type as StatsType.MapStats).userId }?.position?.takeIf { (type as StatsType.MapStats).userId != null }
                        finalList.add(MapDetails(mkWar, MKWarTrack(track.track), position))
                    }
                }
                finalList
            }
            .filter { it.isNotEmpty() }
            .onEach {
                val mapDetailsList = mutableListOf<MapDetails>()
                mapDetailsList.addAll(it
                    .filter { !onlyIndiv || (onlyIndiv && it.war.war?.warTracks?.any { MKWarTrack(it).hasPlayer((type as StatsType.MapStats).userId?.split(".")?.firstOrNull()) }.isTrue) }
                    .filter { (type as StatsType.MapStats).teamId == null || it.war.hasTeam(type.teamId) }

                )
                _sharedStats.value = MapStats(mapDetailsList, onlyIndiv && (type as StatsType.MapStats).userId != null, (type as StatsType.MapStats).userId?.split(".")?.firstOrNull())
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