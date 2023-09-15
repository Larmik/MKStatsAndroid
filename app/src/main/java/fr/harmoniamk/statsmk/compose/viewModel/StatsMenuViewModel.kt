package fr.harmoniamk.statsmk.compose.viewModel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.repository.AuthenticationRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class StatsMenuViewModel @Inject constructor(authenticationRepository: AuthenticationRepositoryInterface, preferencesRepository: PreferencesRepositoryInterface): ViewModel() {
    private val _sharedId = MutableStateFlow(authenticationRepository.user?.uid)
    private val _sharedTeam = MutableStateFlow(preferencesRepository.currentTeam)
    val sharedId = _sharedId.asStateFlow()
    val sharedTeam = _sharedTeam.asStateFlow()
}