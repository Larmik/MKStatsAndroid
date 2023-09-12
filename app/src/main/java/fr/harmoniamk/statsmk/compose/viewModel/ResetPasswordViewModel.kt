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
    private val _sharedDialogValue = MutableStateFlow<MKDialogState?>(null)

    val sharedDismiss = _sharedDismiss.asSharedFlow()
    val sharedDialogValue = _sharedDialogValue.asStateFlow()

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