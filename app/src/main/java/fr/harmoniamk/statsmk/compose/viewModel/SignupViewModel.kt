package fr.harmoniamk.statsmk.compose.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.compose.ui.MKDialogState
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.model.firebase.AuthUserResponse
import fr.harmoniamk.statsmk.model.firebase.User
import fr.harmoniamk.statsmk.model.network.NetworkResponse
import fr.harmoniamk.statsmk.repository.AuthenticationRepositoryInterface
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.MKCentralRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import fr.harmoniamk.statsmk.usecase.FetchUseCaseInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flattenMerge
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import java.text.SimpleDateFormat
import java.util.Date
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class SignupViewModel @Inject constructor(
    private val firebaseRepository: FirebaseRepositoryInterface,
    private val preferencesRepository: PreferencesRepositoryInterface,
    private val authenticationRepository: AuthenticationRepositoryInterface,
    private val mkCentralRepository: MKCentralRepositoryInterface,
    private val fetchUseCase: FetchUseCaseInterface
) : ViewModel() {

    private val _sharedNext = MutableSharedFlow<Unit>()
    private val _sharedToast = MutableSharedFlow<String>()
    private val _sharedDialogValue = MutableStateFlow<MKDialogState?>(null)

    val sharedNext = _sharedNext.asSharedFlow()
    val sharedDialogValue = _sharedDialogValue.asStateFlow()
    val sharedToast = _sharedToast.asSharedFlow()

    fun onSignup(email: String, password: String, mkcId: String) {
        _sharedDialogValue.value = MKDialogState.Loading(R.string.creating_user)
        val imageUrl =
            "https://firebasestorage.googleapis.com/v0/b/stats-mk.appspot.com/o/mk_stats_logo.png?alt=media&token=930c6fdb-9e42-4b23-a9de-3c069d2f982b"
        val createUser = authenticationRepository.createUser(email, password)
            .shareIn(viewModelScope, SharingStarted.Lazily)

        val fetchPlayer = createUser
            .mapNotNull { (it as? AuthUserResponse.Success)?.user?.uid }
            .mapNotNull { authenticationRepository.user }
            .onEach {
                preferencesRepository.authEmail = email
                preferencesRepository.authPassword = password
                preferencesRepository.firstLaunch = false
                _sharedDialogValue.value = MKDialogState.Loading(R.string.fetch_player)
            }
            .flatMapLatest { mkCentralRepository.getPlayer(mkcId) }

        fetchPlayer
            .mapNotNull { (it as? NetworkResponse.Success)?.response }
            .onEach { preferencesRepository.mkcPlayer = it }
            .flatMapLatest { mkCentralRepository.getTeam(it.current_teams.firstOrNull()?.team_id.toString()) }
            .mapNotNull { (it as? NetworkResponse.Success)?.response }
            .onEach { preferencesRepository.mkcTeam = it }
            .map {
                val player = preferencesRepository.mkcPlayer
                User(
                    mid = authenticationRepository.user?.uid.orEmpty(),
                    name = player?.display_name,
                    currentWar = "-1",
                    discordId = "-1",
                    picture = player?.profile_picture?.takeIf { it.isNotEmpty() } ?: imageUrl,
                    role = 0,
                    mkcId = player?.id.toString(),
                    rosterId = player?.current_teams?.getOrNull(0)?.team_id.toString()
                )
            }
            .flatMapLatest { firebaseRepository.writeUser(it) }
            .onEach { _sharedDialogValue.value = MKDialogState.Loading(R.string.fetch_players) }
            .flatMapLatest { fetchUseCase.fetchPlayers(forceUpdate = false) }
            .onEach {  _sharedDialogValue.value = MKDialogState.Loading(R.string.fetch_allies) }
            .flatMapLatest { fetchUseCase.fetchAllies(forceUpdate = false) }
            .onEach { _sharedDialogValue.value = MKDialogState.Loading(R.string.fetch_teams) }
            .flatMapLatest { fetchUseCase.fetchTeams() }
            .onEach { _sharedDialogValue.value = MKDialogState.Loading(R.string.fetch_wars) }
            .flatMapLatest { fetchUseCase.fetchWars() }
            .onEach {
                preferencesRepository.lastUpdate = SimpleDateFormat("dd/MM/yyyy HH:mm").format(
                    Date()
                )
                _sharedDialogValue.value = null
                _sharedNext.emit(Unit)
            }
            .launchIn(viewModelScope)

        flowOf(
            createUser.mapNotNull { (it as? AuthUserResponse.Error)?.message },
            fetchPlayer.mapNotNull { (it as? NetworkResponse.Error)?.error }
        )
            .flattenMerge()
            .onEach { _sharedDialogValue.value = null }
            .bind(_sharedToast, viewModelScope)
    }

}