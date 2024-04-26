package fr.harmoniamk.statsmk.compose.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.compose.ui.MKDialogState
import fr.harmoniamk.statsmk.repository.AuthenticationRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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