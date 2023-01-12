package fr.harmoniamk.statsmk.fragment.reauthUser

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.model.firebase.AuthUserResponse
import fr.harmoniamk.statsmk.repository.AuthenticationRepositoryInterface
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@HiltViewModel
class ReauthUserViewModel @Inject constructor(private val firebaseRepository: FirebaseRepositoryInterface, private val preferencesRepository: PreferencesRepositoryInterface, private val authenticationRepository: AuthenticationRepositoryInterface) : ViewModel() {

    private val _sharedNext = MutableSharedFlow<Unit>()
    private val _sharedToast = MutableSharedFlow<String>()
    private val _sharedGoToReset = MutableSharedFlow<Unit>()
    private val _sharedButtonEnabled = MutableSharedFlow<Boolean>()
    val sharedButtonEnabled = _sharedButtonEnabled.asSharedFlow()
    val sharedNext = _sharedNext.asSharedFlow()
    val sharedToast = _sharedToast.asSharedFlow()
    val sharedGoToReset = _sharedGoToReset.asSharedFlow()

    fun bind(onPassword: Flow<String>, onConnect: Flow<Unit>, onResetPassword: Flow<Unit>) {
        var password: String? = null

        onPassword.onEach {
            password = it
            _sharedButtonEnabled.emit(!password.isNullOrEmpty())
        }.launchIn(viewModelScope)

        val connectUser = onConnect
            .mapNotNull { password }
            .flatMapLatest { authenticationRepository.reauthenticate(preferencesRepository.authEmail.toString(), it) }
            .shareIn(viewModelScope, SharingStarted.Lazily)

        connectUser
            .mapNotNull { (it as? AuthUserResponse.Success)?.user?.uid }
            .flatMapLatest { firebaseRepository.getUser(it) }
            .mapNotNull { it?.team }
            .flatMapLatest { firebaseRepository.getTeam(it) }
            .onEach {
                preferencesRepository.currentTeam = it
                preferencesRepository.firstLaunch = false
                _sharedNext.emit(Unit)
            }
            .launchIn(viewModelScope)

        connectUser
            .mapNotNull { (it as? AuthUserResponse.Error)?.message }
            .bind(_sharedToast, viewModelScope)
        onResetPassword.bind(_sharedGoToReset, viewModelScope)


    }
}