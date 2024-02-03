package fr.harmoniamk.statsmk.compose.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.compose.ui.MKBottomSheetState
import fr.harmoniamk.statsmk.compose.ui.MKDialogState
import fr.harmoniamk.statsmk.model.firebase.AuthUserResponse
import fr.harmoniamk.statsmk.repository.AuthenticationRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import fr.harmoniamk.statsmk.usecase.FetchUseCaseInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import java.text.SimpleDateFormat
import java.util.Date
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val fetchUseCase: FetchUseCaseInterface,
    private val preferencesRepository: PreferencesRepositoryInterface,
    private val authenticationRepository: AuthenticationRepositoryInterface,
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
            .mapNotNull { (it as? AuthUserResponse.Success)?.user }
            .onEach {
                preferencesRepository.authEmail = email
                preferencesRepository.authPassword = password
                preferencesRepository.firstLaunch = false
                _sharedDialogValue.value = MKDialogState.Loading(R.string.fetch_player)
            }
            .flatMapLatest { fetchUseCase.fetchPlayer() }
            .onEach {  _sharedDialogValue.value = MKDialogState.Loading(R.string.fetch_players) }
            .flatMapLatest { fetchUseCase.fetchPlayers(forceUpdate = false) }
            .onEach {  _sharedDialogValue.value = MKDialogState.Loading(R.string.fetch_teams) }
            .flatMapLatest { fetchUseCase.fetchTeams() }
            .onEach {  _sharedDialogValue.value = MKDialogState.Loading(R.string.fetch_wars) }
            .flatMapLatest { fetchUseCase.fetchWars() }
            .onEach {
                preferencesRepository.lastUpdate = SimpleDateFormat("dd/MM/yyyy HH:mm").format(
                    Date()
                )
                _sharedNext.value = Unit
            }
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