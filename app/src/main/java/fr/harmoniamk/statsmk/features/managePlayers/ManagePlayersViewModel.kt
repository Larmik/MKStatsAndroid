package fr.harmoniamk.statsmk.features.managePlayers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.database.model.User
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
class ManagePlayersViewModel @Inject constructor(private val firebaseRepository: FirebaseRepositoryInterface, private val preferencesRepository: PreferencesRepositoryInterface) : ViewModel() {

   private val _sharedPlayers = MutableSharedFlow<List<User>>()
   private val _sharedTitle = MutableSharedFlow<String>()
   private val _sharedAddPlayer = MutableSharedFlow<Unit>()
    val sharedPlayers = _sharedPlayers.asSharedFlow()
    val sharedTitle = _sharedTitle.asSharedFlow()
    val sharedAddPlayer = _sharedAddPlayer.asSharedFlow()

    fun bind(onDelete: Flow<User>, onAdd: Flow<Unit>) {

        firebaseRepository.getUsers()
            .map { it.filter { player -> player.team == preferencesRepository.currentTeam?.mid } }
            .onEach {
                _sharedTitle.emit("Joueurs ${preferencesRepository.currentTeam?.shortName} (${it.size})")
                _sharedPlayers.emit(it)
            }
            .launchIn(viewModelScope)

        onAdd.bind(_sharedAddPlayer, viewModelScope)

        onDelete
            .flatMapLatest { firebaseRepository.deleteUser(it) }
            .flatMapLatest {  firebaseRepository.getUsers() }
            .map { it.filter { player -> player.team == preferencesRepository.currentTeam?.mid } }
            .bind(_sharedPlayers, viewModelScope)
    }

}