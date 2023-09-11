package fr.harmoniamk.statsmk.compose.viewModel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.repository.AuthenticationRepositoryInterface
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class StatsMenuViewModel @Inject constructor(private val authenticationRepository: AuthenticationRepositoryInterface): ViewModel() {

    private val _sharedId = MutableStateFlow(authenticationRepository.user?.uid)
    val sharedId = _sharedId.asStateFlow()

}