package fr.harmoniamk.statsmk.compose.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.compose.ui.MKBottomSheetState
import fr.harmoniamk.statsmk.compose.ui.MKDialogState
import fr.harmoniamk.statsmk.extension.withName
import fr.harmoniamk.statsmk.model.firebase.AuthUserResponse
import fr.harmoniamk.statsmk.model.local.MKWar
import fr.harmoniamk.statsmk.model.network.MKCTeam
import fr.harmoniamk.statsmk.repository.AuthenticationRepositoryInterface
import fr.harmoniamk.statsmk.repository.DatabaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.MKCentralRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val firebaseRepository: FirebaseRepositoryInterface,
    private val preferencesRepository: PreferencesRepositoryInterface,
    private val authenticationRepository: AuthenticationRepositoryInterface,
    private val mkCentralRepository: MKCentralRepositoryInterface,
    private val databaseRepository: DatabaseRepositoryInterface
) : ViewModel() {

    private val _sharedNext = MutableStateFlow<Unit?>(null)
    private val _sharedDialogValue = MutableStateFlow<MKDialogState?>(null)
    private val _sharedBottomSheetValue = MutableStateFlow<MKBottomSheetState?>(null)

    val sharedNext = _sharedNext.asSharedFlow()
    val sharedDialogValue = _sharedDialogValue.asStateFlow()
    val sharedBottomSheetValue = _sharedBottomSheetValue.asStateFlow()

    fun onConnect(email: String, password: String) {
        _sharedDialogValue.value = MKDialogState.Loading(R.string.connexion_en_cours)
        val connectUser =  authenticationRepository.signIn(email, password)
            .shareIn(viewModelScope, SharingStarted.Lazily)

        connectUser
            .mapNotNull { (it as? AuthUserResponse.Success)?.user?.uid }
            .flatMapLatest { databaseRepository.getUser(it) }
            .onEach {
                preferencesRepository.authEmail = email
                preferencesRepository.authPassword = password
                preferencesRepository.firstLaunch = false
                _sharedDialogValue.value = MKDialogState.Loading(R.string.fetch_data)
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
                preferencesRepository.mkcTeam?.id?.let {
                    firebaseRepository.getNewWars(it)
                        .map { it.map { MKWar(it) } }
                        .flatMapLatest { it.withName(databaseRepository) }
                        .flatMapLatest {  databaseRepository.writeWars(it) }
                        .firstOrNull()
                }
            }
            .onEach { _sharedNext.value = Unit }
            .launchIn(viewModelScope)

        connectUser
            .mapNotNull { (it as? AuthUserResponse.Error)?.message }
            .onEach { _sharedDialogValue.value = MKDialogState.ChangePassword(it) {
                _sharedDialogValue.value = null
            } }
            .launchIn(viewModelScope)
    }

    fun onForgotPassword() {
        _sharedBottomSheetValue.value = MKBottomSheetState.ResetPassword()
    }

    fun dismissBottomSheet() {
        _sharedBottomSheetValue.value = null
    }

}