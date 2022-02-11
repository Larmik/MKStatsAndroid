package fr.harmoniamk.statsmk.features.manageTeams

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.database.firebase.model.Team
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@HiltViewModel
class ManageTeamsViewModel @Inject constructor(private val preferencesRepository: PreferencesRepositoryInterface, private val firebaseRepository: FirebaseRepositoryInterface): ViewModel() {

    private val _sharedTeams = MutableSharedFlow<List<Team>>()
    private val _sharedCurrentTeamName = MutableSharedFlow<String>()
    private val _sharedAddTeam = MutableSharedFlow<Unit>()
    private val _sharedTeamQuit = MutableSharedFlow<Unit>()
    val sharedTeams = _sharedTeams.asSharedFlow()
    val sharedAddTeam = _sharedAddTeam.asSharedFlow()
    val sharedTeamQuit = _sharedTeamQuit.asSharedFlow()
    val sharedCurrentTeamName = _sharedCurrentTeamName.asSharedFlow()

    fun bind(onAddTeam: Flow<Unit>, onQuitTeam: Flow<Unit>) {
        firebaseRepository.getTeams()
            .bind(_sharedTeams, viewModelScope)
        onAddTeam.bind(_sharedAddTeam, viewModelScope)

        onQuitTeam
            .mapNotNull { preferencesRepository.currentUser?.apply { this.team = "-1" } }
            .onEach {
                preferencesRepository.currentTeam = null
                preferencesRepository.currentUser = it
            }
            .flatMapLatest { firebaseRepository.writeUser(it) }
            .bind(_sharedTeamQuit, viewModelScope)

        flowOf(preferencesRepository.currentTeam)
            .onEach { delay(50) }
            .mapNotNull { it?.name }
            .bind(_sharedCurrentTeamName, viewModelScope)


    }

}