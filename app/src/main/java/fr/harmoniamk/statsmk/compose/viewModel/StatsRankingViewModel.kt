package fr.harmoniamk.statsmk.compose.viewModel

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.EntryPointAccessors
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.application.MainApplication
import fr.harmoniamk.statsmk.compose.ViewModelFactoryProvider
import fr.harmoniamk.statsmk.model.local.RankingItemViewModel
import fr.harmoniamk.statsmk.compose.ui.MKBottomSheetState
import fr.harmoniamk.statsmk.enums.Maps
import fr.harmoniamk.statsmk.enums.PlayerSortType
import fr.harmoniamk.statsmk.enums.SortType
import fr.harmoniamk.statsmk.enums.TrackSortType
import fr.harmoniamk.statsmk.extension.isTrue
import fr.harmoniamk.statsmk.extension.sum
import fr.harmoniamk.statsmk.extension.withFullStats
import fr.harmoniamk.statsmk.extension.withFullTeamStats
import fr.harmoniamk.statsmk.fragment.stats.opponentRanking.OpponentRankingItemViewModel
import fr.harmoniamk.statsmk.fragment.stats.playerRanking.PlayerRankingItemViewModel
import fr.harmoniamk.statsmk.model.firebase.NewWarTrack
import fr.harmoniamk.statsmk.model.local.MKWar
import fr.harmoniamk.statsmk.model.local.MKWarTrack
import fr.harmoniamk.statsmk.model.local.TrackStats
import fr.harmoniamk.statsmk.repository.DatabaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

sealed class StatsRankingState(val title: Int, val placeholderRes: Int, val sort: Sort) {
    class PlayerRankingState: StatsRankingState(
        title = R.string.statistiques_des_joueurs,
        placeholderRes = R.string.rechercher_un_joueur,
        sort = Sort.PlayerSort()
    )
    class OpponentRankingState: StatsRankingState(
        title = R.string.statistiques_des_adversaires,
        placeholderRes = R.string.rechercher_un_advsersaire,
        sort = Sort.PlayerSort()
    )
    class MapsRankingState: StatsRankingState(
        title = R.string.statistiques_des_circuits,
        placeholderRes = R.string.rechercher_un_nom_ou_une_abr_viation,
        sort = Sort.TrackSort(),
    )
}

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class StatsRankingViewModel @AssistedInject constructor(
    @Assisted("userId") val userId: String?,
    @Assisted("teamId") val teamId: String?,
    private val databaseRepository: DatabaseRepositoryInterface,
    private val preferencesRepository: PreferencesRepositoryInterface
    ) : ViewModel() {

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun provideFactory(
            assistedFactory: Factory,
            userId: String?,
            teamId: String?
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return assistedFactory.create(userId, teamId) as T
            }
        }

        @Composable
        fun viewModel(userId: String?, teamId: String?): StatsRankingViewModel {
            val factory: Factory = EntryPointAccessors.fromApplication<ViewModelFactoryProvider>(context = LocalContext.current).statsRankingViewModel
            return androidx.lifecycle.viewmodel.compose.viewModel(
                factory = provideFactory(
                    assistedFactory = factory,
                   userId = userId,
                    teamId = teamId
                )
            )
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(@Assisted("userId") userId: String?, @Assisted("teamId") teamId: String?): StatsRankingViewModel
    }

    private val _sharedList = MutableStateFlow<List<RankingItemViewModel>>(listOf())
    private val _sharedUserId = MutableStateFlow<String?>(null)
    private val _sharedTeamId = MutableStateFlow<String?>(null)
    private val _sharedGoToStats = MutableSharedFlow<RankingItemViewModel?>()
    private val _sharedBottomsheetValue = MutableStateFlow<MKBottomSheetState?>(null)
    private var _sharedIndivEnabled = MutableStateFlow(false)
    private val _sharedUserName = MutableStateFlow<String?>(null)

    val sharedGoToStats = _sharedGoToStats.asSharedFlow()
    val sharedList = _sharedList.asStateFlow()
    val sharedBottomSheetValue = _sharedBottomsheetValue.asStateFlow()
    val sharedUserId = _sharedUserId.asStateFlow()
    val sharedTeamId = _sharedTeamId.asStateFlow()
    val sharedIndivEnabled = _sharedIndivEnabled.asStateFlow()
    val sharedUserName =_sharedUserName.asStateFlow()

    private val warList = mutableListOf<MKWar>()
    private var sortType: SortType? = null
    private val onlyIndiv = preferencesRepository.mkcTeam?.id == null || userId != null

    private val players = mutableListOf<PlayerRankingItemViewModel>()
    private val opponents = mutableListOf<OpponentRankingItemViewModel>()
    private val maps = mutableListOf<TrackStats>()

    fun init(state: StatsRankingState?, indivEnabled: Boolean, periodic: String, type: SortType? = null) {
        _sharedIndivEnabled.value = indivEnabled
        type?.let { this.sortType = it }
        when (state) {
            is StatsRankingState.PlayerRankingState -> {
                databaseRepository.getWars()
                    .onEach {
                        warList.clear()
                        warList.addAll(it)
                    }
                    .flatMapLatest { databaseRepository.getRoster() }
                    .mapNotNull { it.filter { it.rosterId != "-1" }.sortedBy { it.name } }
                    .onEach {
                        val temp = mutableListOf<PlayerRankingItemViewModel>()
                        it.forEach { user ->
                            val stats = warList.filter {
                                war -> war.hasPlayer(user.mkcId.split(".").first())
                                    && (periodic == Periodics.All.name
                                    || (periodic == Periodics.Week.name && war.isThisWeek)
                                    ||( periodic == Periodics.Month.name && war.isThisMonth)
                                            )
                            }.withFullStats(databaseRepository, userId = user.mkcId.split(".").first()).first()
                            temp.add(PlayerRankingItemViewModel(user, stats))
                        }
                        _sharedList.value = sortPlayers(sortType, temp)
                    }.launchIn(viewModelScope)
            }
            is StatsRankingState.OpponentRankingState -> {
                databaseRepository.getWars()
                    .map { it.filter { war -> periodic == Periodics.All.name
                            || (periodic == Periodics.Week.name && war.isThisWeek)
                            ||( periodic == Periodics.Month.name && war.isThisMonth)} }
                    .onEach {
                        warList.clear()
                        warList.addAll(it)
                    }
                    .flatMapLatest { databaseRepository.getNewTeams() }
                    .map { it.filterNot { team -> team.team_id == preferencesRepository.mkcTeam?.id.toString() } }
                    .mapNotNull { it.sortedBy { it.team_name } }
                    .flatMapLatest { it.withFullTeamStats(warList, databaseRepository, preferencesRepository.mkcPlayer?.id.toString().split(".").first().takeIf { indivEnabled.isTrue }) }
                    .mapNotNull { it.filter { vm -> (vm.userId == null && vm.stats.warStats.list.any { war -> war.hasTeam(preferencesRepository.mkcTeam) }) || vm.userId != null } }
                    .onEach {
                        _sharedList.value = sortTeams(sortType, it)
                    }.launchIn(viewModelScope)
            }
            is StatsRankingState.MapsRankingState -> {
                val finalUserId = userId?.split(".")?.first() ?: preferencesRepository.mkcPlayer?.id.toString().takeIf { indivEnabled }?.split(".")?.first()
                databaseRepository.getRoster()
                    .filterNotNull()
                    .onEach { _sharedUserName.value = it.singleOrNull { it.mkcId == userId }?.name }
                    .launchIn(viewModelScope)

                databaseRepository.getNewTeam(teamId)
                    .filterNotNull()
                    .onEach { _sharedUserName.value = it.team_name }
                    .launchIn(viewModelScope)

                databaseRepository.getWars()
                    .map {  it.filter { war -> periodic == Periodics.All.name
                            || (periodic == Periodics.Week.name && war.isThisWeek)
                            ||( periodic == Periodics.Month.name && war.isThisMonth)} }
                    .filter {
                        (!onlyIndiv
                                && (teamId?.takeIf { it.isNotEmpty() } != null && it.map { war -> war.hasTeam(teamId) }.any { it })
                                || (teamId.isNullOrEmpty() && it.map { war -> war.hasTeam(preferencesRepository.mkcTeam)}.any { it }))
                                || onlyIndiv
                    }
                    .mapNotNull { list -> list.filter {
                        (
                                (onlyIndiv && it.hasPlayer(finalUserId)) || !onlyIndiv)
                                && ((!indivEnabled.isTrue && (teamId?.takeIf { it.isNotEmpty() } == null && it.hasTeam(teamId))
                                || (teamId.isNullOrEmpty() && it.hasTeam(preferencesRepository.mkcTeam)))
                                || (indivEnabled.isTrue && it.hasPlayer(finalUserId))
                                || (teamId?.takeIf { it.isNotEmpty() } != null && it.hasTeam(teamId)))} }
                    .map { list ->
                        val allTracksPlayed = mutableListOf<NewWarTrack>()
                        list.filter { (teamId == null && it.hasTeam(preferencesRepository.mkcTeam)) || it.hasTeam(teamId) }.mapNotNull { it.war?.warTracks }.forEach {
                            allTracksPlayed.addAll(it)
                        }
                        allTracksPlayed
                    }
                    .map {
                        _sharedList.value = sortTracks(sortType, it, indivEnabled.isTrue, finalUserId.orEmpty())
                        _sharedTeamId.value =  teamId ?: preferencesRepository.mkcTeam?.id.toString()
                        _sharedUserId.value = finalUserId
                    }
                    .launchIn(viewModelScope)
            }
            else -> {}
        }
    }

    fun onClickOptions(state: StatsRankingState) {
        _sharedBottomsheetValue.value = MKBottomSheetState.FilterSort(state.sort, Filter.None())
    }

    fun dismissBottomSheet() {
        _sharedBottomsheetValue.value = null
    }

    fun onSorted(state: StatsRankingState, sortType: SortType, periodic: String) {
        init(state = state, indivEnabled = _sharedIndivEnabled.value, periodic = periodic, type = sortType)
    }

    fun onItemClick(item: RankingItemViewModel) {
        viewModelScope.launch {
            _sharedGoToStats.emit(item)
        }
    }

    fun onSearch(state: StatsRankingState, search: String) {
        (state as? StatsRankingState.PlayerRankingState)?.let {
            when (search.isNotEmpty()) {
                true ->  _sharedList.value = players.filter { it.user.name?.lowercase()?.contains(search.lowercase()).isTrue }
                else -> _sharedList.value = players
            }
        }
        (state as? StatsRankingState.OpponentRankingState)?.let {
            when (search.isNotEmpty()) {
                true ->  _sharedList.value = opponents.filter { it.team?.team_name?.lowercase()?.contains(search.lowercase()).isTrue || it.team?.team_tag?.lowercase()?.contains(search.lowercase()).isTrue }
                else -> _sharedList.value = opponents
            }
        }
        (state as? StatsRankingState.MapsRankingState)?.let {
            when (search.isNotEmpty()) {
                true ->  _sharedList.value = maps.filter {it.map?.name?.lowercase()?.contains(search.lowercase()).isTrue ||  MainApplication.instance?.applicationContext?.getString(it.map?.label ?: -1 )?.lowercase()?.contains(search.lowercase()).isTrue }
                else -> _sharedList.value = maps
            }
        }
    }

    private fun sortTracks(type: SortType?, list: List<NewWarTrack>, indivEnabled: Boolean, userId: String): List<TrackStats> {
        val pairList =  when (type) {
            TrackSortType.TOTAL_WIN -> list
                .filter { !indivEnabled || (indivEnabled && MKWarTrack(it).hasPlayer(userId)) }
                .groupBy { it.trackIndex }.toList()
                .sortedByDescending { it.second.filter { MKWarTrack(it).displayedDiff.contains('+') }.size }
            TrackSortType.WINRATE -> list
                .filter { !indivEnabled || (indivEnabled && MKWarTrack(it).hasPlayer(userId)) }
                .groupBy { it.trackIndex }.toList()
                .sortedByDescending { it.second.filter { MKWarTrack(it).displayedDiff.contains('+') }.size * 100 / it.second.size }
            TrackSortType.AVERAGE_DIFF -> list
                .filter { !indivEnabled || (indivEnabled && MKWarTrack(it).hasPlayer(userId)) }
                .groupBy { it.trackIndex }.toList()
                .sortedByDescending { it.second.map { MKWarTrack(it).diffScore }.sum() / it.second.size }
            else -> list
                .filter { !indivEnabled || (indivEnabled && MKWarTrack(it).hasPlayer(userId)) }
                .groupBy { it.trackIndex }.toList()
                .sortedByDescending { it.second.size }
        }.map {
            TrackStats(
                stats = null,
                map = Maps.values()[it.first ?: -1],
                trackIndex = it.first,
                totalPlayed = it.second.size,
                winRate = (it.second.filter { MKWarTrack(it).displayedDiff.contains('+') }.size * 100) / it.second.size
            )
        }
        maps.clear()
        maps.addAll(pairList)
        return pairList
    }

    private fun sortPlayers(type: SortType?, list: List<PlayerRankingItemViewModel>) : List<PlayerRankingItemViewModel> {
        val pairList =  when (type) {
            PlayerSortType.WINRATE -> list.sortedByDescending { (it.stats.warStats.warsWon*100)/it.stats.warStats.warsPlayed}
            PlayerSortType.TOTAL_WIN -> list.sortedByDescending { it.stats.warStats.warsPlayed }
            PlayerSortType.AVERAGE -> list.sortedByDescending { it.stats.averagePoints }
            else -> list.sortedBy { it.user.name.lowercase() }
        }
        val playerList =  pairList.filter { it.stats.warStats.warsPlayed > 0 }
        players.clear()
        players.addAll(playerList)
        return playerList
    }

    private fun sortTeams(type: SortType?, list: List<OpponentRankingItemViewModel>): List<OpponentRankingItemViewModel>  {
        val pairList = when (type) {
            PlayerSortType.WINRATE -> list.sortedByDescending { (it.stats.warStats.warsWon*100)/it.stats.warStats.warsPlayed}
            PlayerSortType.TOTAL_WIN -> list.sortedByDescending { it.stats.warStats.warsPlayed }
            PlayerSortType.AVERAGE -> list.sortedByDescending { it.stats.averagePoints }
            else -> list.sortedBy { it.team?.team_name }
        }.filter { vm -> vm.stats.warStats.warsPlayed > 1 }
        opponents.clear()
        opponents.addAll(pairList)
        return pairList
    }

}