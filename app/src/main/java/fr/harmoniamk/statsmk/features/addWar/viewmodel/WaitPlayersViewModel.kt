package fr.harmoniamk.statsmk.features.addWar.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.database.firebase.model.User
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
class WaitPlayersViewModel @Inject constructor(private val firebaseRepository: FirebaseRepositoryInterface, private val preferencesRepository: PreferencesRepositoryInterface) : ViewModel() {

    private val _sharedOnlinePlayers = MutableSharedFlow<List<User>>()
    private val  _sharedBack = MutableSharedFlow<Unit>()
    private val  _sharedQuit = MutableSharedFlow<Unit>()
    private val  _sharedCancel = MutableSharedFlow<Unit>()
    private val  _sharedWarName = MutableSharedFlow<String>()

    val sharedOnlinePlayers = _sharedOnlinePlayers.asSharedFlow()
    val sharedBack = _sharedBack.asSharedFlow()
    val sharedQuit = _sharedQuit.asSharedFlow()
    val sharedCancel = _sharedCancel.asSharedFlow()
    val sharedWarName = _sharedWarName.asSharedFlow()

    fun bind(onBackPress: Flow<Unit>) {
        firebaseRepository.listenToUsers().map {
            it.filter { user -> user.currentWar == preferencesRepository.currentUser?.currentWar }
        }.bind(_sharedOnlinePlayers, viewModelScope)

        flowOf(preferencesRepository.currentUser?.currentWar)
            .filterNotNull()
            .flatMapLatest { firebaseRepository.getWar(it) }
            .mapNotNull { it?.name }
            .bind(_sharedWarName, viewModelScope)

        onBackPress.bind(_sharedBack, viewModelScope)
    }

    fun bindDialog(onQuit: Flow<Unit>, onBack: Flow<Unit>) {
        onQuit.bind(_sharedQuit, viewModelScope)
        onBack.bind(_sharedCancel, viewModelScope)
    }

}