package fr.harmoniamk.statsmk.fragment.connectUser

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
class ConnectUserViewModel @Inject constructor(private val firebaseRepository: FirebaseRepositoryInterface, private val preferencesRepository: PreferencesRepositoryInterface, private val authenticationRepository: AuthenticationRepositoryInterface) : ViewModel() {

    private val _sharedNext = MutableSharedFlow<Unit>()
    private val _sharedGoToSignup = MutableSharedFlow<Unit>()
    private val _sharedToast = MutableSharedFlow<String>()
    private val _sharedGoToReset = MutableSharedFlow<Unit>()

    val sharedNext = _sharedNext.asSharedFlow()
    val sharedToast = _sharedToast.asSharedFlow()
    val sharedGoToSignup = _sharedGoToSignup.asSharedFlow()
    val sharedGoToReset = _sharedGoToReset.asSharedFlow()

    fun bind(onEmail: Flow<String>, onPassword: Flow<String>, onConnect: Flow<Unit>, onSignupClick: Flow<Unit>, onResetPassword: Flow<Unit>) {
        var password: String? = null
        var email: String? = null

        onPassword.onEach {
            password = it
        }.launchIn(viewModelScope)
        onEmail.onEach { email = it }.launchIn(viewModelScope)

        val connectUser = onConnect
            .mapNotNull { password }
            .flatMapLatest { authenticationRepository.signIn(email.toString(), password.toString()) }
            .shareIn(viewModelScope, SharingStarted.Lazily)

        connectUser
            .mapNotNull { (it as? AuthUserResponse.Success)?.user?.uid }
            .onEach { preferencesRepository.userId = it }
            .flatMapLatest { firebaseRepository.getUser(it) }
            .filterNotNull()
            .onEach {
                preferencesRepository.authEmail = email
                preferencesRepository.authPassword = password
            }
            .mapNotNull { it.team }
            .flatMapLatest { firebaseRepository.getTeam(it) }
            .onEach {
                preferencesRepository.currentTeam = it
                preferencesRepository.firstLaunch = false
                _sharedNext.emit(Unit)
            }.launchIn(viewModelScope)

        connectUser
            .mapNotNull { (it as? AuthUserResponse.Error)?.message }
            .bind(_sharedToast, viewModelScope)

        onSignupClick.bind(_sharedGoToSignup, viewModelScope)
        onResetPassword.bind(_sharedGoToReset, viewModelScope)

    }
}