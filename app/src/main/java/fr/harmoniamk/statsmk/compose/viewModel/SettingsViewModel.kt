package fr.harmoniamk.statsmk.compose.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.compose.ui.MKDialogState
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import fr.harmoniamk.statsmk.usecase.FetchUseCaseInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.text.SimpleDateFormat
import java.util.Date
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepositoryInterface,
    private val fetchUseCase: FetchUseCaseInterface,
): ViewModel() {
    private val _sharedLastUpdate = MutableStateFlow(preferencesRepository.lastUpdate)
    private val _sharedDialogValue = MutableStateFlow<MKDialogState?>(null)

    val sharedLastUpdate = _sharedLastUpdate.asStateFlow()
    val sharedDialogValue = _sharedDialogValue.asStateFlow()

    fun onUpdate() {
        _sharedDialogValue.value = MKDialogState.Loading(R.string.fetch_player)
        fetchUseCase.fetchPlayer()
            .onEach { _sharedDialogValue.value = MKDialogState.Loading(R.string.fetch_players) }
            .flatMapLatest { fetchUseCase.fetchPlayers(forceUpdate = true) }
            .onEach {  _sharedDialogValue.value = MKDialogState.Loading(R.string.fetch_teams) }
            .flatMapLatest { fetchUseCase.fetchTeams() }
            .onEach {  _sharedDialogValue.value = MKDialogState.Loading(R.string.fetch_wars) }
            .flatMapLatest { fetchUseCase.fetchWars() }
            .onEach {
                preferencesRepository.lastUpdate = SimpleDateFormat("dd/MM/yyyy HH:mm").format(Date())
                _sharedDialogValue.value = null
                _sharedLastUpdate.value = preferencesRepository.lastUpdate
            }.launchIn(viewModelScope)
    }

}