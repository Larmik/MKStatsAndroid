package fr.harmoniamk.statsmk.features.manageTeams

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.database.firebase.model.Team
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class ManageTeamsViewModel @Inject constructor(private val firebaseRepository: FirebaseRepositoryInterface): ViewModel() {

    private val _sharedTeams = MutableSharedFlow<List<Team>>()
    private val _sharedAddTeam = MutableSharedFlow<Unit>()
    val sharedTeams = _sharedTeams.asSharedFlow()
    val sharedAddTeam = _sharedAddTeam.asSharedFlow()

    fun bind(onAddTeam: Flow<Unit>) {
        firebaseRepository.getTeams()
            .bind(_sharedTeams, viewModelScope)
        onAddTeam.bind(_sharedAddTeam, viewModelScope)
    }

}