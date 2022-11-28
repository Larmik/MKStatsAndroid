package fr.harmoniamk.statsmk.fragment.home.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.extension.isTrue
import fr.harmoniamk.statsmk.model.local.MKWar
import fr.harmoniamk.statsmk.repository.AuthenticationRepositoryInterface
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
@FlowPreview
@ExperimentalCoroutinesApi
class StatsViewModel @Inject constructor(private val preferencesRepository: PreferencesRepositoryInterface, private val firebaseRepository: FirebaseRepositoryInterface, private val authenticationRepository: AuthenticationRepositoryInterface) : ViewModel() {

    private val _sharedToast = MutableSharedFlow<String>()
    private val _sharedIndiv = MutableSharedFlow<Unit>()
    private val _sharedTeam = MutableSharedFlow<Unit>()
    private val _sharedMap = MutableSharedFlow<Unit>()

    val sharedToast = _sharedToast.asSharedFlow()
    val sharedIndiv = _sharedIndiv.asSharedFlow()
    val sharedTeam = _sharedTeam.asSharedFlow()
    val sharedMap = _sharedMap.asSharedFlow()

    fun bind(onIndiv: Flow<Unit>, onTeam: Flow<Unit>, onMap: Flow<Unit>) {

        val indivClick = onIndiv.shareIn(viewModelScope, SharingStarted.Lazily)
        val mapClick = onMap.shareIn(viewModelScope, SharingStarted.Lazily)
        val teamClick = onTeam.shareIn(viewModelScope, SharingStarted.Lazily)

        indivClick
            .filter { preferencesRepository.currentTeam != null }
            .flatMapLatest { firebaseRepository.getNewWars() }
            .map {
                it.filter { newWar -> MKWar(newWar).hasPlayer(authenticationRepository.user?.uid)  }
            }
            .onEach {
                when (it.isEmpty()) {
                    true -> _sharedToast.emit("Vous devez avoir fait au moins une war pour avoir accès à cette fonctionnalité.")
                    else -> _sharedIndiv.emit(Unit)
                }
            }.launchIn(viewModelScope)

        teamClick
            .filter { preferencesRepository.currentTeam != null }
            .flatMapLatest { firebaseRepository.getNewWars() }
            .map { it.filter { newWar -> newWar.teamHost == preferencesRepository.currentTeam?.mid || newWar.teamOpponent == preferencesRepository.currentTeam?.mid } }
            .onEach {
                when (it.isEmpty()) {
                    true -> _sharedToast.emit("Votre équipe doit avoir fait au moins une war pour avoir accès à cette fonctionnalité.")
                    else -> _sharedTeam.emit(Unit)
                }
            }.launchIn(viewModelScope)



        mapClick.filter { preferencesRepository.currentTeam != null }.bind(_sharedMap, viewModelScope)

        flowOf(indivClick, teamClick, mapClick)
            .flattenMerge()
            .filter { preferencesRepository.currentTeam == null }
            .map { "Vous devez intégrer une équipe pour avoir accès à cette fonctionnalité" }
            .bind(_sharedToast, viewModelScope)
    }

}