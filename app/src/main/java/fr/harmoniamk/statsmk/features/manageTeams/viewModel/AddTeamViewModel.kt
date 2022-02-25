package fr.harmoniamk.statsmk.features.manageTeams.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.database.model.Team
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class AddTeamViewModel @Inject constructor(private val firebaseRepository: FirebaseRepositoryInterface) : ViewModel() {
    private val _sharedTeamAdded = MutableSharedFlow<Unit>()

    val sharedTeamAdded = _sharedTeamAdded.asSharedFlow()

    fun bind(onTeamName: Flow<String>, onShortname: Flow<String>, onCode: Flow<String>, onAddClick: Flow<Unit>) {
        var name: String? = null
        var shortName: String? = null
        var code: String? = null

        onTeamName.onEach { name = it }.launchIn(viewModelScope)
        onShortname.onEach { shortName = it }.launchIn(viewModelScope)
        onCode.onEach { code = it }.launchIn(viewModelScope)

        onAddClick
            .filter { name != null && shortName != null && code != null }
            .flatMapLatest { firebaseRepository.writeTeam(
                Team(
                    mid = System.currentTimeMillis().toString(),
                    name = name,
                    shortName = shortName,
                    accessCode = code)
            ) }
            .onEach {
                _sharedTeamAdded.emit(Unit)
            }.launchIn(viewModelScope)

    }

}