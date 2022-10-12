package fr.harmoniamk.statsmk.fragment.manageTeams

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.model.firebase.Team
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import java.util.*
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class AddTeamViewModel @Inject constructor(private val firebaseRepository: FirebaseRepositoryInterface) : ViewModel() {
    private val _sharedTeamAdded = MutableSharedFlow<Unit>()
    private val _sharedToast = MutableSharedFlow<String>()

    val sharedTeamAdded = _sharedTeamAdded.asSharedFlow()
    val sharedToast = _sharedToast.asSharedFlow()

    fun bind(onTeamName: Flow<String>, onShortname: Flow<String>, onCode: Flow<String>, onAddClick: Flow<Unit>) {
        var name: String? = null
        var shortName: String? = null
        var code: String? = null

        onTeamName.onEach { name = it }.launchIn(viewModelScope)
        onShortname.onEach { shortName = it }.launchIn(viewModelScope)
        onCode.onEach { code = it }.launchIn(viewModelScope)

        val addClick =  onAddClick
            .filter { name != null && shortName != null }
            .flatMapLatest { firebaseRepository.getTeams() }
            .shareIn(viewModelScope, SharingStarted.Lazily)

        addClick
            .filterNot { it.map { team -> team.name?.toLowerCase(Locale.getDefault()) }.contains(name?.toLowerCase(Locale.getDefault())) }
            .flatMapLatest { firebaseRepository.writeTeam(
                Team(
                    mid = System.currentTimeMillis().toString(),
                    name = name,
                    shortName = shortName,
                    accessCode = code)
                )
            }
            .onEach { _sharedTeamAdded.emit(Unit) }
            .launchIn(viewModelScope)

        addClick
            .filter {
                it.map { team -> team.name?.toLowerCase(Locale.getDefault()) }.contains(name?.toLowerCase(Locale.getDefault()))
            }
            .onEach { _sharedToast.emit("Cette équipe existe déjà") }
            .launchIn(viewModelScope)

    }

}