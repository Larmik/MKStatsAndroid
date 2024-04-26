package fr.harmoniamk.statsmk.compose.viewModel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class StatsMenuViewModel @Inject constructor(preferencesRepository: PreferencesRepositoryInterface): ViewModel() {
    private val _sharedId = MutableStateFlow(preferencesRepository.mkcPlayer?.id.toString())
    private val _sharedTeam = MutableStateFlow(preferencesRepository.mkcTeam)
    val sharedId = _sharedId.asStateFlow()
    val sharedTeam = _sharedTeam.asStateFlow()
}