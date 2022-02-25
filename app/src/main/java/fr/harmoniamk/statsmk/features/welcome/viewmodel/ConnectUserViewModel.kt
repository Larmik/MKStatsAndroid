package fr.harmoniamk.statsmk.features.welcome.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
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
class ConnectUserViewModel @Inject constructor(private val firebaseRepository: FirebaseRepositoryInterface, private val preferencesRepository: PreferencesRepositoryInterface) : ViewModel() {

    private val _sharedNext = MutableSharedFlow<Unit>()
    private val _sharedNoCode = MutableSharedFlow<Unit>()

    val sharedNext = _sharedNext.asSharedFlow()
    val sharedNoCode = _sharedNoCode.asSharedFlow()

    fun bind(onCodeName: Flow<String>, onNoCodeClick: Flow<Unit>) {
        var codeName: String? = null
        onCodeName
            .onEach { codeName = it }
            .flatMapLatest { firebaseRepository.getUsers() }
            .mapNotNull { it.singleOrNull { user -> user.accessCode == codeName } }
            .onEach { preferencesRepository.currentUser = it }
            .mapNotNull { it.team }
            .flatMapLatest { firebaseRepository.getTeam(it) }
            .onEach {
                preferencesRepository.currentTeam = it
                _sharedNext.emit(Unit)
            }
            .launchIn(viewModelScope)

        onNoCodeClick.bind(_sharedNoCode, viewModelScope)
    }
}