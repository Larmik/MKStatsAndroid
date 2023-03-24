package fr.harmoniamk.statsmk.fragment.connectUser

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.enums.WelcomeScreen
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.extension.withName
import fr.harmoniamk.statsmk.model.firebase.AuthUserResponse
import fr.harmoniamk.statsmk.model.local.MKWar
import fr.harmoniamk.statsmk.repository.AuthenticationRepositoryInterface
import fr.harmoniamk.statsmk.repository.DatabaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@HiltViewModel
class ConnectUserViewModel @Inject constructor(private val firebaseRepository: FirebaseRepositoryInterface, private val preferencesRepository: PreferencesRepositoryInterface, private val authenticationRepository: AuthenticationRepositoryInterface, private val databaseRepository: DatabaseRepositoryInterface) : ViewModel() {

    private val _sharedNext = MutableSharedFlow<Unit>()
    private val _sharedGoToSignup = MutableSharedFlow<Unit>()
    private val _sharedToast = MutableSharedFlow<String>()
    private val _sharedGoToReset = MutableSharedFlow<Unit>()
    private val _sharedButtonEnabled = MutableSharedFlow<Boolean>()
    private val _sharedLoading = MutableSharedFlow<String?>()

    val sharedNext = _sharedNext.asSharedFlow()
    val sharedToast = _sharedToast.asSharedFlow()
    val sharedGoToSignup = _sharedGoToSignup.asSharedFlow()
    val sharedGoToReset = _sharedGoToReset.asSharedFlow()
    val sharedButtonEnabled = _sharedButtonEnabled.asSharedFlow()
    val sharedLoading = _sharedLoading.asSharedFlow()

    fun bind(onEmail: Flow<String>, onPassword: Flow<String>, onConnect: Flow<Unit>, onSignupClick: Flow<Unit>, onResetPassword: Flow<Unit>) {
        var password: String? = null
        var email: String? = null

        onPassword.onEach {
            password = it
            _sharedButtonEnabled.emit(!password.isNullOrEmpty() && !email.isNullOrEmpty())
        }.launchIn(viewModelScope)

        onEmail.onEach {
            email = it
            _sharedButtonEnabled.emit(!password.isNullOrEmpty() && !email.isNullOrEmpty())
        }.launchIn(viewModelScope)

        val connectUser = onConnect
            .mapNotNull { password }
            .onEach { _sharedLoading.emit(null) }
            .flatMapLatest { authenticationRepository.signIn(email.toString(), password.toString()) }
            .shareIn(viewModelScope, SharingStarted.Lazily)

        connectUser
            .mapNotNull { (it as? AuthUserResponse.Success)?.user?.uid }
            .flatMapLatest { databaseRepository.getUser(it) }
            .onEach {
                preferencesRepository.authEmail = email
                preferencesRepository.authPassword = password
                it?.team?.takeIf { it != "-1" }?.let { team ->
                    preferencesRepository.currentTeam =
                        databaseRepository.getTeam(team).firstOrNull()
                }
                preferencesRepository.firstLaunch = false
                _sharedLoading.emit("Récupération des données en cours...")
            }.flatMapLatest { firebaseRepository.getNewWars() }
            .map { it.map { MKWar(it) } }
            .flatMapLatest { it.withName(databaseRepository) }
            .onEach {
                databaseRepository.warList.clear()
                databaseRepository.warList.addAll(it)
                _sharedNext.emit(Unit)
            }.launchIn(viewModelScope)


        connectUser
            .mapNotNull { (it as? AuthUserResponse.Error)?.message }
            .bind(_sharedToast, viewModelScope)

        onSignupClick.bind(_sharedGoToSignup, viewModelScope)
        onResetPassword.bind(_sharedGoToReset, viewModelScope)

    }
}