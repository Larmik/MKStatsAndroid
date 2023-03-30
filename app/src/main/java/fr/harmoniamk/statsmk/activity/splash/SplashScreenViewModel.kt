package fr.harmoniamk.statsmk.activity.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.enums.WelcomeScreen
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.extension.withName
import fr.harmoniamk.statsmk.fragment.popup.PopupFragment
import fr.harmoniamk.statsmk.model.firebase.AuthUserResponse
import fr.harmoniamk.statsmk.model.local.MKWar
import fr.harmoniamk.statsmk.repository.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@HiltViewModel
class SplashScreenViewModel @Inject constructor(private val preferencesRepository: PreferencesRepositoryInterface, private val authenticationRepository: AuthenticationRepositoryInterface, private val firebaseRepository: FirebaseRepositoryInterface, private val databaseRepository: DatabaseRepositoryInterface, private val networkRepository: NetworkRepositoryInterface) : ViewModel() {

    private val _sharedWelcomeScreen = MutableSharedFlow<WelcomeScreen>()
    private val  _sharedShowPopup = MutableSharedFlow<Pair<WelcomeScreen, List<MKWar>>>()
    private val _sharedLoadingVisible = MutableSharedFlow<String>()

    val sharedWelcomeScreen = _sharedWelcomeScreen.asSharedFlow()
    val sharedShowPopup = _sharedShowPopup.asSharedFlow()
    val sharedLoadingVisible = _sharedLoadingVisible.asSharedFlow()

    fun bind() {

        val isConnected = flowOf(networkRepository.networkAvailable)
            .onEach { delay(100) }
            .shareIn(viewModelScope, SharingStarted.Lazily)

        isConnected
            .filterNot { it }
            .flatMapLatest { databaseRepository.getWars() }
            .map {
                val screen = when (preferencesRepository.authEmail) {
                    null -> WelcomeScreen.CONNECT
                    else -> WelcomeScreen.HOME

                }
                Pair(screen, it)
            }
            .bind(_sharedShowPopup, viewModelScope)

        isConnected
            .filter { it }
            .flatMapLatest {  firebaseRepository.getUsers() }
            .flatMapLatest { databaseRepository.writeUsers(it) }
            .flatMapLatest { firebaseRepository.getTeams() }
            .flatMapLatest { databaseRepository.writeTeams(it) }
            .onEach {
                _sharedLoadingVisible.emit("Authentification...")
                flowOf(preferencesRepository.authEmail)
                    .filterNotNull()
                    .flatMapLatest {
                        authenticationRepository.reauthenticate(
                            preferencesRepository.authEmail.toString(),
                            preferencesRepository.authPassword.toString()
                        )
                    }
                    .mapNotNull { (it as? AuthUserResponse.Success)?.user?.uid }
                    .flatMapLatest { databaseRepository.getUser(it) }
                    .onEach {
                        it?.team?.takeIf { it != "-1" }?.let { team ->
                            preferencesRepository.currentTeam = databaseRepository.getTeam(team).firstOrNull()
                        }
                    }
                    .onEach { _sharedLoadingVisible.emit("Récupération des statistiques...") }
                    .flatMapLatest { firebaseRepository.getNewWars() }
                    .map { list -> list.map { MKWar(it) } }
                    .flatMapLatest { it.withName(databaseRepository) }
                    .flatMapLatest { databaseRepository.writeWars(it) }
                    .onEach { _sharedWelcomeScreen.emit(WelcomeScreen.HOME) }
                    .launchIn(viewModelScope)

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
            }
            .launchIn(viewModelScope)




    }

}