package fr.harmoniamk.statsmk.compose.viewModel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.compose.ui.MKBottomSheetState
import fr.harmoniamk.statsmk.compose.ui.MKDialogState
import fr.harmoniamk.statsmk.model.firebase.PictureResponse
import fr.harmoniamk.statsmk.model.firebase.ResetPasswordResponse
import fr.harmoniamk.statsmk.model.firebase.UploadPictureResponse
import fr.harmoniamk.statsmk.model.network.MKCFullPlayer
import fr.harmoniamk.statsmk.repository.AuthenticationRepositoryInterface
import fr.harmoniamk.statsmk.repository.DatabaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.NetworkRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import fr.harmoniamk.statsmk.repository.StorageRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authenticationRepository: AuthenticationRepositoryInterface,
    private val preferencesRepository: PreferencesRepositoryInterface,
    private val databaseRepository: DatabaseRepositoryInterface) : ViewModel() {

    private val _sharedDisconnect = MutableSharedFlow<Unit>()
     private val _sharedBottomSheetValue = MutableStateFlow<MKBottomSheetState?>(null)
    private val _sharedDialogValue = MutableStateFlow<MKDialogState?>(null)
    private val _sharedEmail = MutableStateFlow<String?>(null)
    private val _sharedPlayer = MutableStateFlow<MKCFullPlayer?>(null)

    val sharedDisconnect =_sharedDisconnect.asSharedFlow()
    val sharedBottomSheetValue = _sharedBottomSheetValue.asSharedFlow()
    val sharedDialogValue = _sharedDialogValue.asSharedFlow()
    val sharedEmail = _sharedEmail.asStateFlow()
    val sharedPlayer = _sharedPlayer.asStateFlow()

    init { refresh() }

    fun refresh() {
        flowOf(preferencesRepository.mkcPlayer)
            .filterNotNull()
            .onEach {
                _sharedPlayer.value = it
               _sharedEmail.value = authenticationRepository.user?.email
            }.launchIn(viewModelScope)
    }

    fun dismissBottomSheet() {
        _sharedBottomSheetValue.value = null
        refresh()
    }

    fun onEditEmail() {
        _sharedBottomSheetValue.value = MKBottomSheetState.EditUser()
    }

    fun onEditPassword() {
        authenticationRepository.resetPassword(authenticationRepository.user?.email.toString())
            .onEach {
                _sharedDialogValue.value =
                MKDialogState.ChangePassword(
                   text =  when (it) {
                        is ResetPasswordResponse.Success -> R.string.reset_pwd_success
                        is ResetPasswordResponse.Error -> R.string.reset_pwd_error
                    },
                    onDismiss = {
                        _sharedDialogValue.value = null
                    }
                )

            }.launchIn(viewModelScope)
    }

    fun onLogout() {
        _sharedDialogValue.value = MKDialogState.Logout(
            onLogout = {
                databaseRepository.clear()
                    .onEach {
                        authenticationRepository.signOut()
                        preferencesRepository.mkcTeam = null
                        preferencesRepository.authEmail = null
                        preferencesRepository.authPassword = null
                        _sharedDisconnect.emit(Unit)
                    }.launchIn(viewModelScope)
            },
            onDismiss = {
                _sharedDialogValue.value = null
            }
        )
    }

}