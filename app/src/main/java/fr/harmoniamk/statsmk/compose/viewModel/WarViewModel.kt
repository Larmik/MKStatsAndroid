package fr.harmoniamk.statsmk.compose.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.compose.ui.CurrentWar
import fr.harmoniamk.statsmk.enums.UserRole
import fr.harmoniamk.statsmk.extension.formatToDate
import fr.harmoniamk.statsmk.extension.safeSubList
import fr.harmoniamk.statsmk.model.firebase.WarDispo
import fr.harmoniamk.statsmk.model.local.MKWar
import fr.harmoniamk.statsmk.model.network.MKCFullTeam
import fr.harmoniamk.statsmk.repository.AuthenticationRepositoryInterface
import fr.harmoniamk.statsmk.repository.DatabaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.NetworkRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import fr.harmoniamk.statsmk.usecase.FetchUseCaseInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.zip
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@HiltViewModel
class WarViewModel @Inject constructor(
    private val firebaseRepository: FirebaseRepositoryInterface,
    preferencesRepository: PreferencesRepositoryInterface,
    private val authenticationRepository: AuthenticationRepositoryInterface,
    private val databaseRepository: DatabaseRepositoryInterface,
    private val networkRepository: NetworkRepositoryInterface,
    private val fetchUseCase: FetchUseCaseInterface
) : ViewModel() {

    private val _sharedCurrentWars = MutableStateFlow<List<CurrentWar>>(listOf())
    private val _sharedLoading = MutableStateFlow(true)
    private val _sharedLastWars = MutableStateFlow<Map<String, List<MKWar>>?>(null)
    private val _sharedTeam = MutableStateFlow<MKCFullTeam?>(null)
    private val _sharedCreateWarVisible = MutableStateFlow(false)

    val sharedCurrentWars = _sharedCurrentWars.asStateFlow()
    val sharedLastWars = _sharedLastWars.asStateFlow()
    val sharedLoading = _sharedLoading.asStateFlow()
    val sharedTeam = _sharedTeam.asStateFlow()
    val sharedCreateWarVisible = _sharedCreateWarVisible.asStateFlow()

    //A faire plus tard
    private val _sharedDispos = MutableStateFlow<List<WarDispo>?>(null)
    private val _sharedNextScheduledWar = MutableSharedFlow<WarDispo>()
    val sharedDispos = _sharedDispos.asStateFlow()
    val sharedNextScheduledWar = _sharedNextScheduledWar.asSharedFlow()
    private var scheduledWar: WarDispo? = null
    private val dispoList = mutableListOf<WarDispo>()


    init {
        preferencesRepository.mkcTeam?.takeIf { networkRepository.networkAvailable }?.let { team ->
            _sharedTeam.value = team
            val isAdmin = authenticationRepository.userRole >= UserRole.ADMIN.ordinal

            val currentWarsFlow = when {
                team.primary_team_id != null -> firebaseRepository.listenToCurrentWar(team.id)
                    .zip(firebaseRepository.listenToCurrentWar(team.primary_team_id.toString())) { first, second -> listOf(first, second) }
                !team.secondary_teams.isNullOrEmpty() -> firebaseRepository.listenToCurrentWar(team.id)
                    .zip(firebaseRepository.listenToCurrentWar(team.secondary_teams.getOrNull(0)?.id.toString())) { first, second -> listOf(first, second) }
                else -> firebaseRepository.listenToCurrentWar(team.id).map { listOf(it) }
            }.shareIn(viewModelScope, SharingStarted.Eagerly)

            currentWarsFlow
                .filter { it.filterNotNull().isEmpty() }
                .onEach {
                    _sharedLoading.value = false
                    _sharedCreateWarVisible.value = isAdmin
                    _sharedCurrentWars.value = listOf()
                }.launchIn(viewModelScope)

            currentWarsFlow
                .debounce(500)
                .map { it.filterNotNull() }
                .filter { it.isNotEmpty() }
                .onEach { wars ->
                    _sharedLoading.value = true
                    _sharedCreateWarVisible.value = isAdmin && wars.none { it.hasTeam(team.id) }
                    val currentWars = mutableListOf<CurrentWar>()
                    wars.forEach { war ->
                        val refreshPlayers = fetchUseCase.fetchPlayers(forceUpdate = false)
                            .flatMapLatest { fetchUseCase.fetchAllies(false) }
                            .takeIf { war.warTracks.isNullOrEmpty() }
                        when (refreshPlayers) {
                            null -> {
                                databaseRepository
                                    .takeIf { _sharedLoading.value }
                                    ?.getNewTeam(war.war?.teamHost.orEmpty())
                                    ?.firstOrNull()?.let {
                                        currentWars.add(CurrentWar(it.team_name, war))
                                        if (currentWars.size == wars.size) {
                                            _sharedLoading.value = false
                                            _sharedCurrentWars.value = currentWars
                                        }
                                    }
                            }

                            else -> refreshPlayers.onEach {
                                databaseRepository
                                    .takeIf { _sharedLoading.value }
                                    ?.getNewTeam(war.war?.teamHost.orEmpty())
                                    ?.firstOrNull()?.let {
                                        currentWars.add(CurrentWar(it.team_name, war))
                                        if (currentWars.size == wars.size) {
                                            _sharedLoading.value = false
                                            _sharedCurrentWars.value = currentWars
                                        }
                                    }
                            }.launchIn(viewModelScope)
                        }
                    }
                }.launchIn(viewModelScope)
        }

        databaseRepository.getWars()
            .map { it.groupBy { war -> war.war?.teamHost } }
            .onEach {
                val lastResults = mutableMapOf<String, List<MKWar>>()
                it.forEach { map ->
                    val teamName =
                        databaseRepository.getNewTeam(map.key).firstOrNull()?.team_name.orEmpty()
                    val lastWars =
                        map.value.sortedByDescending { it.war?.createdDate?.formatToDate() }
                            .safeSubList(0, 5)
                    lastResults[teamName] = lastWars
                    if (it.size == lastResults.size)
                        _sharedLastWars.value = lastResults
                }

            }.launchIn(viewModelScope)


        /*firebaseRepository.getDispos()
          .onEach {
              dispoList.clear()
              dispoList.addAll(it)
              val hour = Date().get(Calendar.HOUR_OF_DAY)
              dispoList.forEach {
                  if (it.lineUp != null && it.opponentId != null) {
                      it.withLineUpAndOpponent(databaseRepository).firstOrNull()?.let {
                          scheduledWar = it
                          _sharedNextScheduledWar.emit(it)
                      }
                  }
              }
              _sharedDispos.value = it
          }.launchIn(viewModelScope)*/
    }
}