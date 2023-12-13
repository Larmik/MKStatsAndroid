package fr.harmoniamk.statsmk.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.BuildConfig
import fr.harmoniamk.statsmk.enums.WelcomeScreen
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.model.firebase.AuthUserResponse
import fr.harmoniamk.statsmk.model.firebase.User
import fr.harmoniamk.statsmk.model.local.MKWar
import fr.harmoniamk.statsmk.repository.AuthenticationRepositoryInterface
import fr.harmoniamk.statsmk.repository.DatabaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.NetworkRepositoryInterface
import fr.harmoniamk.statsmk.repository.NotificationsRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import fr.harmoniamk.statsmk.repository.RemoteConfigRepositoryInterface
import fr.harmoniamk.statsmk.usecase.FetchUseCaseInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@HiltViewModel
class MainViewModel @Inject constructor(
    private val fetchUseCase: FetchUseCaseInterface,
    private val preferencesRepository: PreferencesRepositoryInterface,
    private val authenticationRepository: AuthenticationRepositoryInterface,
    private val databaseRepository: DatabaseRepositoryInterface,
    private val networkRepository: NetworkRepositoryInterface,
    private val remoteConfigRepository: RemoteConfigRepositoryInterface,
    private val notificationsRepository: NotificationsRepositoryInterface
) : ViewModel() {

    private val _sharedWelcomeScreen = MutableSharedFlow<WelcomeScreen>()
    private val _sharedShowPopup = MutableSharedFlow<Pair<WelcomeScreen, List<MKWar>>>()
    private val _sharedShowUpdatePopup = MutableSharedFlow<Unit>()

    val sharedWelcomeScreen = _sharedWelcomeScreen.asSharedFlow()
    val sharedShowPopup = _sharedShowPopup.asSharedFlow()
    val sharedShowUpdatePopup = _sharedShowUpdatePopup.asSharedFlow()

    fun bind() {

        val isConnected = remoteConfigRepository.loadConfig
            .map { networkRepository.networkAvailable }
            .onEach { delay(100) }
            .shareIn(viewModelScope, SharingStarted.Lazily)

        isConnected
            .filter { BuildConfig.VERSION_CODE < remoteConfigRepository.minimumVersion }
            .onEach { _sharedShowUpdatePopup.emit(Unit) }
            .launchIn(viewModelScope)

        isConnected
            .filterNot { it }
            .flatMapLatest { databaseRepository.getWars() }
            .map {
                val screen = when (preferencesRepository.authEmail) {
                    null -> WelcomeScreen.Login
                    else -> WelcomeScreen.Home

                }
                Pair(screen, it)
            }
            .bind(_sharedShowPopup, viewModelScope)

        isConnected
            .filter { it }
            .onEach {
                flowOf(preferencesRepository.authEmail)
                    .filterNotNull()
                    .flatMapLatest {
                        authenticationRepository.reauthenticate(
                            preferencesRepository.authEmail.toString(),
                            preferencesRepository.authPassword.toString()
                        )
                    }
                    .mapNotNull { (it as? AuthUserResponse.Success)?.user?.uid }
                    .flatMapLatest { fetchUseCase.fetch(it) }
                    .onEach { _sharedWelcomeScreen.emit(WelcomeScreen.Home) }
                    .flatMapLatest { notificationsRepository.register(preferencesRepository.mkcTeam?.id ?: "") }
                    .launchIn(viewModelScope)

                flowOf(preferencesRepository.firstLaunch)
                    .filter { it }
                    .onEach {
                        delay(1000)
                        _sharedWelcomeScreen.emit(WelcomeScreen.Signup)
                    }.launchIn(viewModelScope)

                flowOf(preferencesRepository.authEmail)
                    .filterNot { preferencesRepository.firstLaunch }
                    .filter { it == null }
                    .onEach {
                        delay(1000)
                        _sharedWelcomeScreen.emit(WelcomeScreen.Login)
                    }.launchIn(viewModelScope)
            }
            .launchIn(viewModelScope)
    }

}