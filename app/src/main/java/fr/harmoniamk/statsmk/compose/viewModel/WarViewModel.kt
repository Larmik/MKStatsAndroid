package fr.harmoniamk.statsmk.compose.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.enums.UserRole
import fr.harmoniamk.statsmk.extension.formatToDate
import fr.harmoniamk.statsmk.extension.safeSubList
import fr.harmoniamk.statsmk.model.firebase.Team
import fr.harmoniamk.statsmk.model.firebase.WarDispo
import fr.harmoniamk.statsmk.model.local.MKWar
import fr.harmoniamk.statsmk.repository.AuthenticationRepositoryInterface
import fr.harmoniamk.statsmk.repository.DatabaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.NetworkRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.zip
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@HiltViewModel
class WarViewModel @Inject constructor(
    private val firebaseRepository: FirebaseRepositoryInterface,
    private val preferencesRepository: PreferencesRepositoryInterface,
    private val authenticationRepository: AuthenticationRepositoryInterface,
    private val databaseRepository: DatabaseRepositoryInterface,
    private val networkRepository: NetworkRepositoryInterface
) : ViewModel() {

    private val _sharedCurrentWar = MutableStateFlow<MKWar?>(null)
    private val _sharedLastWars = MutableStateFlow<List<MKWar>?>(null)
    private val _sharedTeam = MutableStateFlow<Team?>(null)
    private val _sharedCreateWarVisible = MutableStateFlow(false)

    val sharedCurrentWar = _sharedCurrentWar.asStateFlow()
    val sharedLastWars = _sharedLastWars.asStateFlow()
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
        firebaseRepository.takeIf { preferencesRepository.mkcTeam != null }?.listenToCurrentWar()
            ?.onEach {
                if (networkRepository.networkAvailable) {
                    val isAdmin =  authenticationRepository.userRole.map { it >= UserRole.ADMIN.ordinal }.first()
                    _sharedCurrentWar.value = it
                    preferencesRepository.currentWar = it?.war
                    _sharedCreateWarVisible.value = it == null && isAdmin
                }
            }?.launchIn(viewModelScope)
    }

    fun refresh() {
        _sharedTeam.value = preferencesRepository.currentTeam
       databaseRepository.getWars()
           .map { it.filter { war -> war.hasTeam(preferencesRepository.mkcTeam?.id.toString()) } }
            .onEach {
                delay(100)
                it.sortedByDescending { it.war?.createdDate?.formatToDate() }
                .safeSubList(0, 5)
                .let { _sharedLastWars.emit(it) }
            }.launchIn(viewModelScope)
        firebaseRepository.getCurrentWar(preferencesRepository.mkcTeam?.id.toString())
            .zip( authenticationRepository.userRole.map { it >= UserRole.ADMIN.ordinal }) { war, isAdmin ->
                if (networkRepository.networkAvailable) {
                    _sharedCurrentWar.value = war
                    preferencesRepository.currentWar = war?.war
                    _sharedCreateWarVisible.value = war == null && isAdmin                }
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