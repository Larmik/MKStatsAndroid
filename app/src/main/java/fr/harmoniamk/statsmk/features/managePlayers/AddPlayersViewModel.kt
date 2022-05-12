package fr.harmoniamk.statsmk.features.managePlayers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.model.firebase.User
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
class AddPlayersViewModel @Inject constructor(private val firebaseRepository: FirebaseRepositoryInterface, private val preferencesRepository: PreferencesRepositoryInterface) : ViewModel() {

    private val _sharedUserAdded = MutableSharedFlow<Unit>()
    val sharedUserAdded = _sharedUserAdded.asSharedFlow()

    fun bind(onName: Flow<String>, onPlayerAdded: Flow<Unit>) {
        var name: String? = null
        onName.onEach { name = it }.launchIn(viewModelScope)
        onPlayerAdded
            .mapNotNull { name }
            .map { User(
                mid = System.currentTimeMillis().toString(),
                name = it,
                team = preferencesRepository.currentTeam?.mid,
            ) }
            .flatMapLatest { firebaseRepository.writeUser(it) }
            .bind(_sharedUserAdded, viewModelScope)
    }

}