package fr.harmoniamk.statsmk.compose.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.repository.AuthenticationRepositoryInterface
import fr.harmoniamk.statsmk.repository.DatabaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@HiltViewModel
class EditUserViewModel @Inject constructor(
    private val authenticationRepository: AuthenticationRepositoryInterface,
    private val preferencesRepository: PreferencesRepositoryInterface,
    private val databaseRepository: DatabaseRepositoryInterface,
    private val firebaseRepository: FirebaseRepositoryInterface
): ViewModel() {

    private val _sharedDismiss = MutableSharedFlow<Unit>()
    val sharedDismiss = _sharedDismiss.asSharedFlow()

    fun onValidate(isEmail: Boolean, value: String) {
        when (isEmail) {
            true -> authenticationRepository.updateEmail(value)
                .onEach {
                    preferencesRepository.authEmail = value
                    _sharedDismiss.emit(Unit)
                }.launchIn(viewModelScope)
            else -> authenticationRepository.updateProfile(value, null)
                .flatMapLatest { databaseRepository.getUser(authenticationRepository.user?.uid) }
                .mapNotNull { it?.copy(name = value) }
                .flatMapLatest { firebaseRepository.writeUser(it) }
                .onEach { _sharedDismiss.emit(Unit) }
                .launchIn(viewModelScope)
        }
    }

}