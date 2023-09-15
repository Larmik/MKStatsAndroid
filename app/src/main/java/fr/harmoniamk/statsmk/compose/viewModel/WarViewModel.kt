package fr.harmoniamk.statsmk.compose.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.extension.formatToDate
import fr.harmoniamk.statsmk.extension.get
import fr.harmoniamk.statsmk.extension.safeSubList
import fr.harmoniamk.statsmk.extension.withLineUpAndOpponent
import fr.harmoniamk.statsmk.extension.withName
import fr.harmoniamk.statsmk.model.firebase.Team
import fr.harmoniamk.statsmk.model.firebase.WarDispo
import fr.harmoniamk.statsmk.model.local.MKWar
import fr.harmoniamk.statsmk.repository.AuthenticationRepositoryInterface
import fr.harmoniamk.statsmk.repository.DatabaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.NetworkRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import fr.harmoniamk.statsmk.repository.RemoteConfigRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.Calendar
import java.util.Date
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

    val sharedCurrentWar = _sharedCurrentWar.asStateFlow()
    val sharedLastWars = _sharedLastWars.asStateFlow()
    val sharedTeam = _sharedTeam.asStateFlow()

    //A faire plus tard
    private val _sharedDispos = MutableStateFlow<List<WarDispo>?>(null)
    val sharedDispos = _sharedDispos.asStateFlow()
    private val _sharedNextScheduledWar = MutableSharedFlow<WarDispo>()


    private var currentWar: MKWar? = null
    private var scheduledWar: WarDispo? = null
    private val dispoList = mutableListOf<WarDispo>()

    init { refresh() }

    fun refresh() {
        _sharedTeam.value = preferencesRepository.currentTeam
        firebaseRepository.takeIf { preferencesRepository.currentTeam != null }?.listenToCurrentWar()
            ?.onEach { current ->
                firebaseRepository.getNewWars(preferencesRepository.currentTeam?.mid ?: "")
                    .flatMapLatest { it.map { MKWar(it) }.withName(databaseRepository) }
                    .onEach { databaseRepository.writeWars(it) }
                    .firstOrNull()
                    ?.filter { war -> war.war?.teamHost == preferencesRepository.currentTeam?.mid }
                    ?.sortedByDescending { it.war?.createdDate?.formatToDate() }
                    ?.safeSubList(0, 5)
                    ?.let {
                        _sharedLastWars.emit(it)
                        currentWar = current.takeIf { networkRepository.networkAvailable }
                        _sharedCurrentWar.value = currentWar
                    }
            }?.launchIn(viewModelScope)

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