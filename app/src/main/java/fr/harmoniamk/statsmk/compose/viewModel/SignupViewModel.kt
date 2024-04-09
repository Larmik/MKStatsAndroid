package fr.harmoniamk.statsmk.compose.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.compose.ui.MKDialogState
import fr.harmoniamk.statsmk.model.firebase.AuthUserResponse
import fr.harmoniamk.statsmk.model.firebase.User
import fr.harmoniamk.statsmk.model.network.NetworkResponse
import fr.harmoniamk.statsmk.repository.AuthenticationRepositoryInterface
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import fr.harmoniamk.statsmk.usecase.FetchUseCaseInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
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
    private val fetchUseCase: FetchUseCaseInterface
) : ViewModel() {

    private val _sharedNext = MutableSharedFlow<Unit>()
    private val _sharedToast = MutableSharedFlow<String>()
    private val _sharedDialogValue = MutableStateFlow<MKDialogState?>(null)

    val sharedNext = _sharedNext.asSharedFlow()
    val sharedDialogValue = _sharedDialogValue.asStateFlow()
    val sharedToast = _sharedToast.asSharedFlow()

    fun onSignup(email: String, password: String, mkcId: String) {
        _sharedDialogValue.value = MKDialogState.Loading(R.string.fetch_player)
        val fetchPlayer = fetchUseCase.fetchPlayer(mkcId)
            .shareIn(viewModelScope, SharingStarted.Lazily)

        val fetchTeam = fetchPlayer
            .mapNotNull { (it as? NetworkResponse.Success)?.response }
            .onEach { _sharedDialogValue.value = MKDialogState.Loading(R.string.fetch_team) }
            .flatMapLatest { fetchUseCase.fetchTeam() }

        val createUser = fetchTeam
            .mapNotNull { (it as? NetworkResponse.Success)?.response }
            .onEach { _sharedDialogValue.value = MKDialogState.Loading(R.string.creating_user) }
            .flatMapLatest { authenticationRepository.createUser(email, password) }
            .shareIn(viewModelScope, SharingStarted.Lazily)

        createUser
            .mapNotNull { (it as? AuthUserResponse.Success)?.user }
            .onEach {
                preferencesRepository.authEmail = email
                preferencesRepository.authPassword = password
                preferencesRepository.firstLaunch = false
            }
            .map {
                val player = preferencesRepository.mkcPlayer
                val role = when (preferencesRepository.mkcTeam?.rosterList?.singleOrNull { it.mkcId.split(".").getOrNull(0) == player?.id.toString() }?.isLeader == "1.0") {
                    true -> 2
                    else -> 0
                }
                preferencesRepository.role = role
                User(
                    mid = authenticationRepository.user?.uid.orEmpty(),
                    name = player?.display_name,
                    currentWar = "-1",
                    discordId = "-1",
                    picture = player?.profile_picture,
                    role = role,
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

        createUser
            .debounce(500)
            .mapNotNull { (it as? AuthUserResponse.Error)?.message }
            .onEach { _sharedDialogValue.value = MKDialogState.Error(it) {
                _sharedDialogValue.value = null
            } }
            .launchIn(viewModelScope)

        fetchPlayer
            .mapNotNull { (it as? NetworkResponse.Error) }
            .onEach { _sharedDialogValue.value = MKDialogState.Error("Le joueur n'a pas été trouvé. Vous devez être inscrit sur MKCentral pour utiliser l'application.") {
                _sharedDialogValue.value = null
            } }.launchIn(viewModelScope)

        fetchTeam
            .mapNotNull { (it as? NetworkResponse.Error) }
            .onEach { _sharedDialogValue.value = MKDialogState.Error("Le joueur ne fait partie d'aucune équipe. Vous devez faire partie d'une team sur MKCentral pour utiliser l'application.") {
                _sharedDialogValue.value = null
            } }.launchIn(viewModelScope)
    }

}