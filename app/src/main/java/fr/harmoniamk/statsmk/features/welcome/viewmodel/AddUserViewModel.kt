package fr.harmoniamk.statsmk.features.welcome.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.model.firebase.User
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@HiltViewModel
class AddUserViewModel @Inject constructor(private val firebaseRepository: FirebaseRepositoryInterface, private val preferencesRepository: PreferencesRepositoryInterface) : ViewModel() {

    private val _sharedNext = MutableSharedFlow<Unit>()
    val sharedNext = _sharedNext.asSharedFlow()

    fun bind(onName: Flow<String>, onCode: Flow<String>, onNext: Flow<Unit>) {

        var name: String? = null
        var code: String? = null

        onName.onEach { name = it }.launchIn(viewModelScope)
        onCode.onEach { code = it }.launchIn(viewModelScope)

        onNext
            .filter { name != null && code != null }
            .flatMapLatest { firebaseRepository.writeUser(
                User(
                    mid = System.currentTimeMillis().toString(),
                    name = name,
                    accessCode = "$code-${firebaseRepository.deviceId}",
                    team = "-1",
                    currentWar = "-1")
            ) }
            .flatMapLatest { firebaseRepository.getUsers() }
            .map { it.singleOrNull { user -> user.accessCode == code } }
            .onEach {
                preferencesRepository.currentUser = it
                _sharedNext.emit(Unit)
            }.launchIn(viewModelScope)
    }

}