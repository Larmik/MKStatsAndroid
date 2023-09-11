package fr.harmoniamk.statsmk.compose.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.compose.ui.MKDialogState
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.model.firebase.ResetPasswordResponse
import fr.harmoniamk.statsmk.repository.AuthenticationRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class ResetPasswordViewModel @Inject constructor(private val authenticationRepository: AuthenticationRepositoryInterface) : ViewModel() {

    private val _sharedDismiss = MutableSharedFlow<Unit>()
    private val _sharedToast = MutableSharedFlow<String>()
    private val _sharedDialogValue = MutableStateFlow<MKDialogState?>(null)

    val sharedDismiss = _sharedDismiss.asSharedFlow()
    val sharedToast = _sharedToast.asSharedFlow()
    val sharedDialogValue = _sharedDialogValue.asStateFlow()

    fun bind(onEmail: Flow<String>, onReset: Flow<Unit>, onBack: Flow<Unit>) {
        var email: String? = null
        onEmail.onEach {
            email = it
        }.launchIn(viewModelScope)

        onReset
            .mapNotNull { email }
            .flatMapLatest { authenticationRepository.resetPassword(it) }
            .onEach {
                _sharedToast.emit(it.message)
                if (it is ResetPasswordResponse.Success) _sharedDismiss.emit(Unit)
            }.launchIn(viewModelScope)

        onBack.bind(_sharedDismiss, viewModelScope)
    }

    fun onReset(email: String) {
        authenticationRepository.resetPassword(email)
            .onEach {
                _sharedDialogValue.value = MKDialogState.ChangePassword(text = it.message, onDismiss = {
                    viewModelScope.launch {
                        _sharedDialogValue.value = null
                        _sharedDismiss.emit(Unit)
                    }
                })
            }.launchIn(viewModelScope)
    }
}