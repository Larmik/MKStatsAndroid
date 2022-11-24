package fr.harmoniamk.statsmk.fragment.manageTeams

import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.enums.UserRole
import fr.harmoniamk.statsmk.model.firebase.Team
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.extension.isTrue
import fr.harmoniamk.statsmk.repository.AuthenticationRepositoryInterface
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import java.util.*
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@HiltViewModel
class ManageTeamsViewModel @Inject constructor(private val preferencesRepository: PreferencesRepositoryInterface, private val firebaseRepository: FirebaseRepositoryInterface, private val authenticationRepository: AuthenticationRepositoryInterface): ViewModel() {

    private val _sharedTeams = MutableSharedFlow<List<ManageTeamsItemViewModel>>()
    private val _sharedAddTeam = MutableSharedFlow<Unit>()
    private val _sharedAddTeamVisibility = MutableSharedFlow<Int>()
    private val _sharedOnEditClick = MutableSharedFlow<Team>()
    private val _sharedShowDialog = MutableSharedFlow<Boolean>()

    val sharedTeams = _sharedTeams.asSharedFlow()
    val sharedAddTeam = _sharedAddTeam.asSharedFlow()
    val sharedAddTeamVisibility = _sharedAddTeamVisibility.asSharedFlow()
    val sharedOnEditClick = _sharedOnEditClick.asSharedFlow()
    val sharedShowDialog = _sharedShowDialog.asSharedFlow()


    private val teams = mutableListOf<ManageTeamsItemViewModel>()

    fun bind(onAddTeam: Flow<Unit>, onEditClick: Flow<Team>, onSearch: Flow<String>) {
        firebaseRepository.getTeams()
            .map {
                teams.clear()
                teams.addAll(it.map { ManageTeamsItemViewModel(it, authenticationRepository) }); teams.sortedBy { it.name }
            }
            .bind(_sharedTeams, viewModelScope)

        onAddTeam.bind(_sharedAddTeam, viewModelScope)

        authenticationRepository.userRole
            .mapNotNull { it >= UserRole.ADMIN.ordinal }
            .mapNotNull {
                when (it) {
                    true -> View.VISIBLE
                    else -> View.GONE
                }
            }.bind(_sharedAddTeamVisibility, viewModelScope)

        onEditClick.onEach {
            _sharedOnEditClick.emit(it)
            _sharedShowDialog.emit(true)
        }.launchIn(viewModelScope)

        onSearch
            .map { searched ->
                teams.filter {
                    it.shortName?.toLowerCase(Locale.ROOT)
                        ?.contains(searched.toLowerCase(Locale.ROOT)).isTrue || it.name?.toLowerCase(
                        Locale.ROOT)?.contains(searched.toLowerCase(Locale.ROOT)) ?: true
                }
            }
            .bind(_sharedTeams, viewModelScope)
    }

    fun bindDialog(onTeamEdit: Flow<Team>, onTeamDelete: Flow<Team>) {
        onTeamDelete
            .flatMapLatest { firebaseRepository.deleteTeam(it) }
            .flatMapLatest { firebaseRepository.getTeams() }
            .map { list -> list.map { ManageTeamsItemViewModel(it, authenticationRepository) } }
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
            .map { list -> list.sortedBy { it.name }.map { ManageTeamsItemViewModel(it, authenticationRepository) } }
            .onEach {
                _sharedShowDialog.emit(false)
                _sharedTeams.emit(it)
            }.launchIn(viewModelScope)
    }

    fun bindAddDialog(onTeamAdded: Flow<Unit>) {
        onTeamAdded
            .flatMapLatest {  firebaseRepository.getTeams() }
            .map { list -> list.sortedBy { it.name }.map { ManageTeamsItemViewModel(it, authenticationRepository) } }
            .bind(_sharedTeams, viewModelScope)
    }

}