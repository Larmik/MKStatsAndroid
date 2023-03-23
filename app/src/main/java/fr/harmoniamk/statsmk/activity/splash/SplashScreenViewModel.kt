package fr.harmoniamk.statsmk.activity.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.enums.WelcomeScreen
import fr.harmoniamk.statsmk.model.firebase.AuthUserResponse
import fr.harmoniamk.statsmk.repository.AuthenticationRepositoryInterface
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@HiltViewModel
class SplashScreenViewModel @Inject constructor(private val preferencesRepository: PreferencesRepositoryInterface, private val authenticationRepository: AuthenticationRepositoryInterface, private val firebaseRepository: FirebaseRepositoryInterface) : ViewModel() {

    private val _sharedWelcomeScreen = MutableSharedFlow<WelcomeScreen>()

    val sharedWelcomeScreen = _sharedWelcomeScreen.asSharedFlow()

    fun bind() {
        val authenticate = flowOf(preferencesRepository.authEmail)
            .filterNotNull()
            .flatMapLatest {
                authenticationRepository.reauthenticate(
                    preferencesRepository.authEmail.toString(),
                    preferencesRepository.authPassword.toString()
                )
            }.shareIn(viewModelScope, SharingStarted.Lazily)

        flowOf(preferencesRepository.firstLaunch)
            .filter { it }
            .onEach {
                delay(1000)
                _sharedWelcomeScreen.emit(WelcomeScreen.WELCOME)
            }.launchIn(viewModelScope)

        flowOf(preferencesRepository.authEmail)
            .filterNot { preferencesRepository.firstLaunch }
            .filter { it == null }
            .onEach {
                delay(1000)
                _sharedWelcomeScreen.emit(WelcomeScreen.CONNECT)
            }.launchIn(viewModelScope)

        authenticate
            .mapNotNull { (it as? AuthUserResponse.Success)?.user?.uid }
            .flatMapLatest { firebaseRepository.getUser(it) }
            .mapNotNull { it?.team }
            .flatMapLatest { firebaseRepository.getTeam(it) }
            .onEach {
                preferencesRepository.currentTeam = it
                _sharedWelcomeScreen.emit(WelcomeScreen.HOME)
            }.launchIn(viewModelScope)


    }

}