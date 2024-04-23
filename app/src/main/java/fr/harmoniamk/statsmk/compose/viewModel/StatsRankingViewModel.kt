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
import fr.harmoniamk.statsmk.enums.SortType
import fr.harmoniamk.statsmk.extension.isTrue
import fr.harmoniamk.statsmk.extension.toSortedMaps
import fr.harmoniamk.statsmk.extension.toSortedOpponents
import fr.harmoniamk.statsmk.extension.toSortedPlayers
import fr.harmoniamk.statsmk.extension.withFullStats
import fr.harmoniamk.statsmk.extension.withFullTeamStats
import fr.harmoniamk.statsmk.fragment.stats.opponentRanking.OpponentRankingItemViewModel
import fr.harmoniamk.statsmk.fragment.stats.playerRanking.PlayerRankingItemViewModel
import fr.harmoniamk.statsmk.model.firebase.NewWarTrack
import fr.harmoniamk.statsmk.model.local.MKWar
import fr.harmoniamk.statsmk.model.local.WarStats
import fr.harmoniamk.statsmk.repository.DatabaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

sealed class StatsRankingState(val title: Int, val placeholderRes: Int, val sort: Sort) {
    class PlayerRankingState : StatsRankingState(
        title = R.string.statistiques_des_joueurs,
        placeholderRes = R.string.rechercher_un_joueur,
        sort = Sort.PlayerSort()
    )

    class OpponentRankingState : StatsRankingState(
        title = R.string.statistiques_des_adversaires,
        placeholderRes = R.string.rechercher_un_advsersaire,
        sort = Sort.PlayerSort()
    )

    class MapsRankingState : StatsRankingState(
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
            val factory: Factory =
                EntryPointAccessors.fromApplication<ViewModelFactoryProvider>(context = LocalContext.current).statsRankingViewModel
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
        fun create(
            @Assisted("userId") userId: String?,
            @Assisted("teamId") teamId: String?
        ): StatsRankingViewModel
    }

    private val _sharedList = MutableStateFlow<List<RankingItemViewModel>?>(null)
    private val _sharedUserId = MutableStateFlow<String?>(null)
    private val _sharedTeamId = MutableStateFlow<String?>(null)
    private val _sharedGoToStats = MutableSharedFlow<RankingItemViewModel?>()
    private val _sharedBottomsheetValue = MutableStateFlow<MKBottomSheetState?>(null)
    private val _sharedIndivEnabled = MutableStateFlow(false)
    private val _sharedSubtitle = MutableStateFlow<String?>(null)

    val sharedGoToStats = _sharedGoToStats.asSharedFlow()
    val sharedList = _sharedList.asStateFlow()
    val sharedBottomSheetValue = _sharedBottomsheetValue.asStateFlow()
    val sharedUserId = _sharedUserId.asStateFlow()
    val sharedTeamId = _sharedTeamId.asStateFlow()
    val sharedIndivEnabled = _sharedIndivEnabled.asStateFlow()
    val sharedSubtitle = _sharedSubtitle.asStateFlow()

    private val warList = mutableListOf<MKWar>()
    private var sortType: SortType? = null
    private var periodic: String = "All"
    private val onlyIndiv = !userId.isNullOrEmpty()

    private var finalUserId: String? = null
    private val players = mutableListOf<PlayerRankingItemViewModel>()
    private val opponents = mutableListOf<OpponentRankingItemViewModel>()
    private val maps = mutableListOf<NewWarTrack>()

    private fun initWars() {
        databaseRepository.getWars()
            .onEach {
                warList.clear()
                warList.addAll(it)
            }.launchIn(viewModelScope)
    }

    private fun initMaps() {
        flowOf(warList
            .filter { war -> periodic == Periodics.All.name
                || (periodic == Periodics.Week.name && war.isThisWeek)
                || (periodic == Periodics.Month.name && war.isThisMonth)
            }
            .filter {
                (!onlyIndiv
                    && (!teamId.isNullOrEmpty() && it.hasTeam(teamId))
                    || (teamId.isNullOrEmpty() && it.hasTeam(preferencesRepository.mkcTeam, preferencesRepository.rosterOnly) )
                    ) || (onlyIndiv && it.hasPlayer(finalUserId) )
            }
        )
        .onEach { list ->
            list.mapNotNull { it.war?.warTracks }.forEach { maps.addAll(it) }
            _sharedList.value = maps.toSortedMaps(sortType, finalUserId.takeIf { _sharedIndivEnabled.value || onlyIndiv })
        }.launchIn(viewModelScope)
    }

    private fun initOpponents() {
        databaseRepository.getNewTeams()
            .map { it.filterNot { team -> team.team_id == preferencesRepository.mkcTeam?.id.toString() } }
            .mapNotNull { it.sortedBy { it.team_name } }
            .flatMapLatest {
                it.withFullTeamStats(
                    wars = warList,
                    databaseRepository = databaseRepository
                )
            }
            .mapNotNull { it.map { OpponentRankingItemViewModel(it.first, it.second) } }
            .onEach {
                opponents.addAll(it.toSortedOpponents(sortType))
                _sharedList.value = opponents.filter { vm ->
                    (vm.userId == null && vm.stats.warStats.list.any { war ->
                        war.hasTeam(
                            preferencesRepository.mkcTeam,
                            preferencesRepository.rosterOnly
                        )
                    }) || (vm.userId != null && vm.stats.warStats.list.any { war ->
                        war.hasPlayer(
                            vm.userId
                        )
                    })
                }.filter { vm -> vm.stats.warStats.warsPlayed > 1 }
            }.launchIn(viewModelScope)
    }

    private fun initPlayers() {
        databaseRepository.getRoster()
            .mapNotNull { it.filter { it.rosterId != "-1" }.sortedBy { it.name } }
            .onEach { userList ->
                userList.forEach { user ->
                    warList.filter { war ->
                        war.hasPlayer(user.mkcId.split(".").first())
                                && (periodic == Periodics.All.name
                                || (periodic == Periodics.Week.name && war.isThisWeek)
                                || (periodic == Periodics.Month.name && war.isThisMonth))
                    }.withFullStats(
                        databaseRepository,
                        userId = user.mkcId.split(".").first()
                    )
                        .onEach {
                            players.add(PlayerRankingItemViewModel(user, it))
                            if (players.size == userList.size)
                                _sharedList.value = players.toSortedPlayers(sortType)
                        }
                        .launchIn(viewModelScope)
                }
            }.launchIn(viewModelScope)
    }

    fun init(state: StatsRankingState?, indivEnabled: Boolean, periodic: String, type: SortType? = null) {
        this.periodic = periodic
        this.sortType = type
        _sharedIndivEnabled.value = indivEnabled
        finalUserId = (userId ?: preferencesRepository.mkcPlayer?.id.toString()).split(".").firstOrNull().orEmpty()
        _sharedUserId.value = finalUserId.takeIf { indivEnabled || onlyIndiv }
        _sharedTeamId.value = teamId
        viewModelScope.launch {
            if (warList.isEmpty()) {
                initWars()
                delay(500)
            }
            when (state) {
                is StatsRankingState.PlayerRankingState -> {
                    if (players.isEmpty()) initPlayers()
                    else _sharedList.value = players.toSortedPlayers(type)
                }

                is StatsRankingState.OpponentRankingState -> {
                    if (opponents.isEmpty()) initOpponents()
                    else _sharedList.value = opponents.toSortedOpponents(type)
                        .map {
                            OpponentRankingItemViewModel(
                                team = it.team,
                                stats = it.stats.copy(
                                    warStats = WarStats(
                                         list = it.stats.warStats.list.filter {
                                             war -> (indivEnabled && war.hasPlayer(finalUserId.orEmpty()) || !indivEnabled)
                                         }
                                    )
                                ),
                                userId = finalUserId.takeIf { indivEnabled })
                        }.filter { vm -> vm.stats.warStats.warsPlayed > 1 }
                }

                is StatsRankingState.MapsRankingState -> {
                    if (maps.isEmpty()) initMaps()
                    else _sharedList.value = maps.toSortedMaps(type, finalUserId.takeIf { indivEnabled || onlyIndiv })
                }
                else -> {}
            }
        }
    }

    fun onClickOptions(state: StatsRankingState) {
        _sharedBottomsheetValue.value = MKBottomSheetState.FilterSort(state.sort, Filter.None())
    }

    fun dismissBottomSheet() {
        _sharedBottomsheetValue.value = null
    }

    fun onSorted(state: StatsRankingState, sortType: SortType) {
        init(
            state = state,
            indivEnabled = _sharedIndivEnabled.value,
            periodic = periodic,
            type = sortType
        )
    }

    fun onItemClick(item: RankingItemViewModel) {
        viewModelScope.launch {
            _sharedGoToStats.emit(item)
        }
    }

    fun onSearch(state: StatsRankingState, search: String) {
        (state as? StatsRankingState.PlayerRankingState)?.let {
            when (search.isNotEmpty()) {
                true -> _sharedList.value = players.filter {
                    it.user.name.lowercase().contains(search.lowercase())
                }

                else -> _sharedList.value = players
            }
        }
        (state as? StatsRankingState.OpponentRankingState)?.let {
            when (search.isNotEmpty()) {
                true -> _sharedList.value = opponents.filter {
                    it.team?.team_name?.lowercase()
                        ?.contains(search.lowercase()).isTrue || it.team?.team_tag?.lowercase()
                        ?.contains(search.lowercase()).isTrue
                }

                else -> _sharedList.value = opponents
            }
        }
        (state as? StatsRankingState.MapsRankingState)?.let {
            when (search.isNotEmpty()) {
                true -> _sharedList.value =
                    maps.toSortedMaps(sortType, finalUserId?.takeIf { _sharedIndivEnabled.value })
                        .filter {
                            it.map?.name?.lowercase()
                                ?.contains(search.lowercase()).isTrue || MainApplication.instance?.applicationContext?.getString(
                                it.map?.label ?: -1
                            )?.lowercase()?.contains(search.lowercase()).isTrue
                        }
                else -> _sharedList.value =
                    maps.toSortedMaps(sortType, finalUserId?.takeIf { _sharedIndivEnabled.value })
            }
        }
    }

}