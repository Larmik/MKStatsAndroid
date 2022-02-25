package fr.harmoniamk.statsmk.features.addWar.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.database.model.Team
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@HiltViewModel
class WarTeamViewModel @Inject constructor(private val firebaseRepository: FirebaseRepositoryInterface, private val preferencesRepository: PreferencesRepositoryInterface): ViewModel() {

    private val _sharedTeams = MutableSharedFlow<List<Team>>()
    private val _sharedTeamSelected = MutableSharedFlow<Team>()

    val sharedTeams = _sharedTeams.asSharedFlow()
    val sharedTeamSelected = _sharedTeamSelected.asSharedFlow()

    fun bind(onTeamClick: Flow<Team>) {
        onTeamClick.bind(_sharedTeamSelected, viewModelScope)
        firebaseRepository.getTeams()
            .map { it.filterNot { team -> team.mid == preferencesRepository.currentTeam?.mid } }
            .bind(_sharedTeams, viewModelScope)
    }
}