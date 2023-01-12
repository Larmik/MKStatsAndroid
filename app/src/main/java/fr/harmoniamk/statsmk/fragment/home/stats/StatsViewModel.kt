package fr.harmoniamk.statsmk.fragment.home.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.extension.bind
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
    private val _sharedIndiv = MutableSharedFlow<List<MKWar>>()
    private val _sharedTeam = MutableSharedFlow<List<MKWar>>()
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
            .flatMapLatest { firebaseRepository.getNewWars() }
            .map {
                it.map { MKWar(it) }.filter { war -> war.hasPlayer(authenticationRepository.user?.uid)  }
            }
            .onEach {
                when (it.isEmpty()) {
                    true -> _sharedToast.emit("Vous devez avoir fait au moins une war pour avoir accès à cette fonctionnalité.")
                    else -> _sharedIndiv.emit(it)
                }
            }.launchIn(viewModelScope)

        teamClick
            .filter { preferencesRepository.currentTeam != null }
            .flatMapLatest { firebaseRepository.getNewWars() }
            .map { it.filter { newWar -> newWar.teamHost == preferencesRepository.currentTeam?.mid || newWar.teamOpponent == preferencesRepository.currentTeam?.mid }.map { MKWar(it) } }
            .onEach {
                when (it.isEmpty()) {
                    true -> _sharedToast.emit("Votre équipe doit avoir fait au moins une war pour avoir accès à cette fonctionnalité.")
                    else -> _sharedTeam.emit(it)
                }
            }.launchIn(viewModelScope)



        mapClick.bind(_sharedMap, viewModelScope)

        teamClick
            .filter { preferencesRepository.currentTeam == null }
            .map { "Vous devez intégrer une équipe pour avoir accès à cette fonctionnalité" }
            .bind(_sharedToast, viewModelScope)
    }

}