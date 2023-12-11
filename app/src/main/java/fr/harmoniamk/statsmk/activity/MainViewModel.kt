package fr.harmoniamk.statsmk.activity

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.BuildConfig
import fr.harmoniamk.statsmk.enums.WelcomeScreen
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.extension.withName
import fr.harmoniamk.statsmk.model.firebase.AuthUserResponse
import fr.harmoniamk.statsmk.model.local.MKWar
import fr.harmoniamk.statsmk.model.network.MKCTeam
import fr.harmoniamk.statsmk.repository.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@HiltViewModel
class MainViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepositoryInterface,
    private val authenticationRepository: AuthenticationRepositoryInterface,
    private val firebaseRepository: FirebaseRepositoryInterface,
    private val databaseRepository: DatabaseRepositoryInterface,
    private val networkRepository: NetworkRepositoryInterface,
    private val remoteConfigRepository: RemoteConfigRepositoryInterface,
    private val notificationsRepository: NotificationsRepositoryInterface,
    private val mkCentralRepository: MKCentralRepositoryInterface
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
            .flatMapLatest { databaseRepository.clearUsers() }
            .flatMapLatest { firebaseRepository.getUsers() }
            .flatMapLatest { databaseRepository.writeUsers(it) }
            .flatMapLatest { databaseRepository.clearTeams() }
            .flatMapLatest { firebaseRepository.getTeams() }
            .flatMapLatest { databaseRepository.writeTeams(it) }
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
                    .flatMapLatest { databaseRepository.getUser(it) }
                    .onEach {
                        when (val team = it?.team?.takeIf { it != "-1" }) {
                            null -> preferencesRepository.currentTeam = null
                            else -> preferencesRepository.currentTeam = databaseRepository.getTeam(team).firstOrNull()
                        }
                    }
                    .flatMapLatest { mkCentralRepository.getPlayer(it?.mkcId.orEmpty()) }
                    .onEach { preferencesRepository.mkcPlayer = it }
                    .flatMapLatest { mkCentralRepository.getTeam(it.current_teams.firstOrNull()?.team_id.toString()) }
                    .onEach { preferencesRepository.mkcTeam = it }
                    .flatMapLatest { databaseRepository.writeRoster(it.rosterList.orEmpty()) }
                    .flatMapLatest { mkCentralRepository.teams }
                    .flatMapLatest { databaseRepository.writeNewTeams(it) }
                    .flatMapLatest { databaseRepository.getTeams() }
                    .flatMapLatest { databaseRepository.writeNewTeams(it.filter { team -> team.mid.toLong() > 999999 }.map { MKCTeam(it) }) }
                    .onEach {
                        preferencesRepository.currentTeam?.mid?.let {
                            firebaseRepository.getNewWars(it).zip(databaseRepository.getWars()) { remoteDb, localDb ->
                                val finalLocalDb = localDb.filter { it.war?.teamHost == preferencesRepository.currentTeam?.mid }
                                Log.d("MKDebugOnly", "local db size: ${finalLocalDb.size}")
                                Log.d("MKDebugOnly", "remote db size: ${remoteDb.size}")
                                when (finalLocalDb.size == remoteDb.size) {
                                    true -> localDb
                                    else -> remoteDb
                                        .map { MKWar(it) }
                                        .withName(databaseRepository)
                                        .onEach { databaseRepository.writeWars(it).first() }
                                        .first()
                                }
                            }.firstOrNull()
                        }
                        _sharedWelcomeScreen.emit(WelcomeScreen.Home)
                    }
                    .flatMapLatest { notificationsRepository.register(preferencesRepository.currentTeam?.mid ?: "") }
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