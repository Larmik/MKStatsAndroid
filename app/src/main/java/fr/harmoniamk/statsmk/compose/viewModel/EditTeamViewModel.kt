package fr.harmoniamk.statsmk.compose.viewModel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.enums.UserRole
import fr.harmoniamk.statsmk.model.firebase.Team
import fr.harmoniamk.statsmk.repository.AuthenticationRepositoryInterface
import fr.harmoniamk.statsmk.repository.DatabaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.zip
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@HiltViewModel
class EditTeamViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepositoryInterface,
    private val databaseRepository: DatabaseRepositoryInterface,
    private val firebaseRepository: FirebaseRepositoryInterface): ViewModel() {

    private val _sharedTeam = MutableStateFlow<Team?>(null)
    private val _sharedDismiss = MutableSharedFlow<Unit>()

    val sharedTeam = _sharedTeam.asStateFlow()
    val sharedDismiss = _sharedDismiss.asSharedFlow()

    fun refresh(teamId: String) {
        databaseRepository.getTeam(teamId)
            .onEach { _sharedTeam.value = it }
            .launchIn(viewModelScope)
    }

    fun onTeamEdited(name: String, tag: String) {
        _sharedTeam.value?.copy(name = name, shortName = tag)?.let { team ->
            firebaseRepository.writeTeam(team)
                .onEach {
                    if (team.mid == preferencesRepository.currentTeam?.mid)
                        preferencesRepository.currentTeam = team
                    _sharedDismiss.emit(Unit)
                }.launchIn(viewModelScope)
        }
    }
}