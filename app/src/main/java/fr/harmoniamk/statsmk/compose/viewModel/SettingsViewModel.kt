package fr.harmoniamk.statsmk.compose.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.compose.ui.MKBottomSheetState
import fr.harmoniamk.statsmk.compose.ui.MKDialogState
import fr.harmoniamk.statsmk.model.local.TeamType
import fr.harmoniamk.statsmk.repository.AuthenticationRepositoryInterface
import fr.harmoniamk.statsmk.repository.DatabaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import fr.harmoniamk.statsmk.usecase.FetchUseCaseInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepositoryInterface,
    private val databaseRepository: DatabaseRepositoryInterface,
    private val fetchUseCase: FetchUseCaseInterface,
    authenticationRepository: AuthenticationRepositoryInterface
): ViewModel() {

    private val _sharedLastUpdate = MutableStateFlow(preferencesRepository.lastUpdate)
    private val _sharedDialogValue = MutableStateFlow<MKDialogState?>(null)
    private val _sharedToast = MutableSharedFlow<Int>()
    private val _sharedBottomSheetValue = MutableStateFlow<MKBottomSheetState?>(null)

    val sharedLastUpdate = _sharedLastUpdate.asStateFlow()
    val sharedDialogValue = _sharedDialogValue.asStateFlow()
    val sharedToast = _sharedToast.asSharedFlow()
    val sharedBottomSheetValue = _sharedBottomSheetValue.asStateFlow()
    val isGod = authenticationRepository.userRole == 3

    val type: TeamType
        get()  {
            val team = preferencesRepository.mkcTeam
            return  when {
                team?.primary_team_id != null -> TeamType.MultiRoster(teamId = team.primary_team_id.toString().takeIf { it != team.id }, secondaryTeamsId = null)
                !team?.secondary_teams.isNullOrEmpty() -> TeamType.MultiRoster(teamId = team?.id, secondaryTeamsId = team?.secondary_teams?.map { it.id })
                else -> TeamType.SingleRoster(teamId = team?.id.toString())
            }
        }

    fun fetchTags() = fetchUseCase.fetchTags().onEach { _sharedToast.emit(R.string.tags_mis_jour) }.launchIn(viewModelScope)
    fun purgeUsers() = fetchUseCase.purgePlayers().onEach { _sharedToast.emit(R.string.base_d_utilisateurs_purg_e) }.launchIn(viewModelScope)

    fun onUpdate() {
        _sharedDialogValue.value = MKDialogState.Loading(R.string.mise_jour_des_donn_es)
        databaseRepository.clear()
            .onEach { _sharedDialogValue.value = MKDialogState.Loading(R.string.fetch_player) }
            .flatMapLatest { fetchUseCase.fetchPlayer() }
            .onEach { _sharedDialogValue.value = MKDialogState.Loading(R.string.fetch_players) }
            .flatMapLatest { fetchUseCase.fetchPlayers(forceUpdate = true) }
            .onEach {  _sharedDialogValue.value = MKDialogState.Loading(R.string.fetch_allies) }
            .flatMapLatest { fetchUseCase.fetchAllies(forceUpdate = true) }
            .onEach {  _sharedDialogValue.value = MKDialogState.Loading(R.string.fetch_teams) }
            .flatMapLatest { fetchUseCase.fetchTeams() }
            .onEach {  _sharedDialogValue.value = MKDialogState.Loading(R.string.fetch_wars) }
            .flatMapLatest { fetchUseCase.fetchWars() }
            .onEach {
                preferencesRepository.lastUpdate = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
                _sharedDialogValue.value = null
                _sharedLastUpdate.value = preferencesRepository.lastUpdate
            }.launchIn(viewModelScope)
    }

    fun showStatsDisplay()
    {
        when (type) {
            is TeamType.SingleRoster -> _sharedDialogValue.value = MKDialogState.Error(R.string.single_roster_label) {
                _sharedDialogValue.value = null
            }
            else ->  _sharedBottomSheetValue.value = MKBottomSheetState.StatsDisplayMode(preferencesRepository.rosterOnly)
        }

    }

    fun showTheme() {
        when (preferencesRepository.coffees >= 15 || isGod) {
            true ->  _sharedBottomSheetValue.value = MKBottomSheetState.Theme(
                preferencesRepository.mainColor,
                preferencesRepository.secondaryColor,
                preferencesRepository.mainTextColor,
                preferencesRepository.secondaryTextColor,
            )
            else ->  _sharedDialogValue.value = MKDialogState.Error(R.string.theme_locked){
                _sharedDialogValue.value = null
            }
        }
    }

    fun dismissBottomSheet() {
        _sharedBottomSheetValue.value = null
    }

    fun setStatsDisplayMode(rosterOnly: Boolean) {
        preferencesRepository.rosterOnly = rosterOnly
        _sharedBottomSheetValue.value = null
    }

    fun setColorsTheme(mainColor: String, secondaryColor: String, mainTextColor: String, secondaryTextColor: String) {
        preferencesRepository.mainColor = mainColor
        preferencesRepository.secondaryColor = secondaryColor
        preferencesRepository.mainTextColor = mainTextColor
        preferencesRepository.secondaryTextColor = secondaryTextColor
        _sharedBottomSheetValue.value = null
        _sharedDialogValue.value = MKDialogState.Error(R.string.theme_changed) {
            _sharedDialogValue.value = null
        }
    }
}