package fr.harmoniamk.statsmk.compose.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flattenMerge
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@HiltViewModel
class WarViewModel @Inject constructor(
    private val firebaseRepository: FirebaseRepositoryInterface,
    private val preferencesRepository: PreferencesRepositoryInterface,
    private val authenticationRepository: AuthenticationRepositoryInterface,
    private val databaseRepository: DatabaseRepositoryInterface,
    private val networkRepository: NetworkRepositoryInterface,
    private val fetchUseCase: FetchUseCaseInterface
) : ViewModel() {

    private val _sharedCurrentWar = MutableStateFlow<MKWar?>(null)
    private val _sharedCurrentWarState = MutableStateFlow<String?>(null)
    private val _sharedLastWars = MutableStateFlow<List<MKWar>?>(null)
    private val _sharedTeam = MutableStateFlow<MKCFullTeam?>(null)
    private val _sharedCreateWarVisible = MutableStateFlow(false)

    val sharedCurrentWar = _sharedCurrentWar.asStateFlow()
    val sharedLastWars = _sharedLastWars.asStateFlow()
    val sharedCurrentWarState = _sharedCurrentWarState.asStateFlow()
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
        flowOf(firebaseRepository.getCurrentWar(), firebaseRepository.listenToCurrentWar())
            .flattenMerge()
            .filter { networkRepository.networkAvailable }
            .onEach { war ->
                when (war) {
                    null -> {
                        val isAdmin = authenticationRepository.userRole >= UserRole.ADMIN.ordinal
                        _sharedCurrentWar.value = null
                        preferencesRepository.currentWar = null
                        _sharedCreateWarVisible.value = isAdmin
                    }
                    else -> {
                        _sharedCreateWarVisible.value = false
                        _sharedCurrentWarState.value = "Récupération de la war en cours..."
                        val refreshPlayers = fetchUseCase.fetchPlayers(forceUpdate = false)
                            .flatMapLatest { fetchUseCase.fetchAllies(false) }
                            .takeIf { war.warTracks.isNullOrEmpty() }
                        when (refreshPlayers) {
                            null -> {
                                _sharedCurrentWarState.value = null
                                _sharedCurrentWar.value = war
                                preferencesRepository.currentWar = war.war
                            }
                            else -> refreshPlayers.onEach {
                                _sharedCurrentWarState.value = null
                                _sharedCurrentWar.value = war
                                preferencesRepository.currentWar = war.war
                            }.launchIn(viewModelScope)
                        }
                    }
                }
            }
    }

    fun refresh() {
        _sharedCurrentWarState.value = null
        _sharedTeam.value = preferencesRepository.mkcTeam
        firebaseRepository.getCurrentWar()
            .filter { networkRepository.networkAvailable }
            .onEach {
                _sharedCurrentWar.value = it
                preferencesRepository.currentWar = it?.war
                _sharedCreateWarVisible.value =
                    it == null && authenticationRepository.userRole >= UserRole.ADMIN.ordinal
            }.launchIn(viewModelScope)

        databaseRepository.getWars()
            .onEach {
                delay(100)
                it.sortedByDescending { it.war?.createdDate?.formatToDate() }
                    .safeSubList(0, 5)
                    .let { _sharedLastWars.emit(it) }
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