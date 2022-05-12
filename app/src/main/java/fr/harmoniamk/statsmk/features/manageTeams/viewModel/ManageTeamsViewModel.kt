package fr.harmoniamk.statsmk.features.manageTeams.viewModel

import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.model.firebase.Team
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

    private val _sharedTeams = MutableSharedFlow<List<ManageTeamsItemViewModel>>()
    private val _sharedCurrentTeamName = MutableSharedFlow<String>()
    private val _sharedAddTeam = MutableSharedFlow<Unit>()
    private val _sharedTeamQuit = MutableSharedFlow<Unit>()
    private val _sharedAddTeamVisibility = MutableSharedFlow<Int>()
    private val _sharedOnEditClick = MutableSharedFlow<Team>()
    private val _sharedShowDialog = MutableSharedFlow<Boolean>()

    val sharedTeams = _sharedTeams.asSharedFlow()
    val sharedAddTeam = _sharedAddTeam.asSharedFlow()
    val sharedTeamQuit = _sharedTeamQuit.asSharedFlow()
    val sharedCurrentTeamName = _sharedCurrentTeamName.asSharedFlow()
    val sharedAddTeamVisibility = _sharedAddTeamVisibility.asSharedFlow()
    val sharedOnEditClick = _sharedOnEditClick.asSharedFlow()
    val sharedShowDialog = _sharedShowDialog.asSharedFlow()

    fun bind(onAddTeam: Flow<Unit>, onQuitTeam: Flow<Unit>, onEditClick: Flow<Team>) {
        firebaseRepository.getTeams()
            .map { list -> list.map { ManageTeamsItemViewModel(it, preferencesRepository) } }
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
            .onEach { delay(20) }
            .mapNotNull { it?.name }
            .bind(_sharedCurrentTeamName, viewModelScope)

        flowOf(preferencesRepository.currentUser)
            .onEach { delay(20) }
            .mapNotNull {
                when (it?.isAdmin) {
                    true -> View.VISIBLE
                    else -> View.INVISIBLE
                }
            }.bind(_sharedAddTeamVisibility, viewModelScope)

        onEditClick.onEach {
            _sharedOnEditClick.emit(it)
            _sharedShowDialog.emit(true)
        }.launchIn(viewModelScope)
    }

    fun bindDialog(onTeamEdit: Flow<Team>, onTeamDelete: Flow<Team>) {
        onTeamDelete
            .flatMapLatest { firebaseRepository.deleteTeam(it) }
            .flatMapLatest { firebaseRepository.getTeams() }
            .map { list -> list.map { ManageTeamsItemViewModel(it, preferencesRepository) } }
            .onEach {
                _sharedShowDialog.emit(false)
                _sharedTeams.emit(it)
            }.launchIn(viewModelScope)

        onTeamEdit
            .onEach {
                if (it.mid == preferencesRepository.currentTeam?.mid)
                    preferencesRepository.currentTeam = it
            }
            .flatMapLatest { firebaseRepository.writeTeam(it) }
            .flatMapLatest {  firebaseRepository.getTeams() }
            .map { list -> list.map { ManageTeamsItemViewModel(it, preferencesRepository) } }
            .onEach {
                _sharedShowDialog.emit(false)
                _sharedTeams.emit(it)
            }.launchIn(viewModelScope)
    }
}