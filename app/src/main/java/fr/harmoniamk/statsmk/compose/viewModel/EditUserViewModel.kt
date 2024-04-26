package fr.harmoniamk.statsmk.compose.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.repository.AuthenticationRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@HiltViewModel
class EditUserViewModel @Inject constructor(
    private val authenticationRepository: AuthenticationRepositoryInterface,
    private val preferencesRepository: PreferencesRepositoryInterface,
): ViewModel() {


    fun onValidate(value: String) {
            authenticationRepository.updateEmail(value)
                .onEach {
                    preferencesRepository.authEmail = value
                }.launchIn(viewModelScope)
        }

}