package fr.harmoniamk.statsmk.fragment.settings.manageTeams

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.enums.UserRole
import fr.harmoniamk.statsmk.model.firebase.Team
import fr.harmoniamk.statsmk.repository.AuthenticationRepositoryInterface
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import java.util.*
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@HiltViewModel
class AddTeamViewModel @Inject constructor(private val firebaseRepository: FirebaseRepositoryInterface, private val preferencesRepository: PreferencesRepositoryInterface, private val authenticationRepository: AuthenticationRepositoryInterface) : ViewModel() {
    private val _sharedTeamAdded = MutableSharedFlow<Unit>()
    private val _sharedToast = MutableSharedFlow<String>()
    private val _sharedButtonEnabled = MutableSharedFlow<Boolean>()
    val sharedButtonEnabled = _sharedButtonEnabled.asSharedFlow()
    val sharedTeamAdded = _sharedTeamAdded.asSharedFlow()
    val sharedToast = _sharedToast.asSharedFlow()

    fun bind(teamWithLeader: Boolean, onTeamName: Flow<String>, onShortname: Flow<String>, onAddClick: Flow<Unit>) {
        var name: String? = null
        var shortName: String? = null
        val id = System.currentTimeMillis().toString()

        onTeamName.onEach {
            name = it
            _sharedButtonEnabled.emit(!name.isNullOrEmpty() && !shortName.isNullOrEmpty())
        }.launchIn(viewModelScope)
        onShortname.onEach {
            shortName = it
            _sharedButtonEnabled.emit(!name.isNullOrEmpty() && !shortName.isNullOrEmpty())
        }.launchIn(viewModelScope)

        val addClick =  onAddClick
            .filter { name != null && shortName != null }
            .flatMapLatest { firebaseRepository.getTeams() }
            .shareIn(viewModelScope, SharingStarted.Lazily)

        addClick
            .filterNot { it.map { team -> team.name?.toLowerCase(Locale.getDefault()) }.contains(name?.toLowerCase(Locale.getDefault())) }
            .filterNot { teamWithLeader }
            .map {
                val team = Team(
                    mid = id,
                    name = name,
                    shortName = shortName
                )
                if (teamWithLeader) preferencesRepository.currentTeam = team
                team
            }
            .flatMapLatest { firebaseRepository.writeTeam(it) }
            .onEach { _sharedTeamAdded.emit(Unit) }
            .launchIn(viewModelScope)

        addClick
            .filterNot { it.map { team -> team.name?.toLowerCase(Locale.getDefault()) }.contains(name?.toLowerCase(Locale.getDefault())) }
            .filter { teamWithLeader }
            .map {
                val team = Team(
                    mid = id,
                    name = name,
                    shortName = shortName,
                    hasLeader = true
                )
                preferencesRepository.currentTeam = team
                team
            }
            .flatMapLatest { firebaseRepository.writeTeam(it) }
            .flatMapLatest { firebaseRepository.getUser(authenticationRepository.user?.uid) }
            .filterNotNull()
            .flatMapLatest { firebaseRepository.writeUser(it.apply {
                this.role = UserRole.LEADER.ordinal
                this.team = id
            }) }
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