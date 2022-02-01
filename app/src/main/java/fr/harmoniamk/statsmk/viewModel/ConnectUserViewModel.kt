package fr.harmoniamk.statsmk.viewModel

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
class ConnectUserViewModel @Inject constructor(private val firebaseRepository: FirebaseRepositoryInterface, private val preferencesRepository: PreferencesRepositoryInterface) : ViewModel() {

    private val _sharedUser = MutableSharedFlow<User?>()
    private val _sharedNext = MutableSharedFlow<Unit>()

    val sharedUser = _sharedUser.asSharedFlow()
    val sharedNext = _sharedNext.asSharedFlow()

    fun bind(onCodeName: Flow<String>, onNameNextClick: Flow<Unit>) {
        var codeName: String? = null
        onCodeName
            .onEach { codeName = it }
            .flatMapLatest { firebaseRepository.getUsers() }
            .map {
                it.singleOrNull { user -> user.accessCode == codeName }
            }
            .bind(_sharedUser, viewModelScope)

        onNameNextClick
            .onEach { preferencesRepository.isConnected = true }
            .bind(_sharedNext, viewModelScope)
    }
}