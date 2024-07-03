package fr.harmoniamk.statsmk.compose.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.compose.ui.MKBottomSheetState
import fr.harmoniamk.statsmk.model.local.TeamType
import fr.harmoniamk.statsmk.model.network.MKCFullTeam
import fr.harmoniamk.statsmk.model.network.MKPlayer
import fr.harmoniamk.statsmk.repository.DatabaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@HiltViewModel
class TeamSettingsViewModel @Inject constructor(
    databaseRepository: DatabaseRepositoryInterface,
    private val preferencesRepository: PreferencesRepositoryInterface
) : ViewModel() {

    private val _sharedPlayers = MutableStateFlow<Map<String, List<MKPlayer>>>(mapOf())
    private val _sharedTeam = MutableStateFlow<MKCFullTeam?>(null)
    private val _sharedPictureLoaded = MutableStateFlow<String?>(null)
    private val _sharedBottomSheetValue = MutableStateFlow<MKBottomSheetState?>(null)

    val sharedPlayers = _sharedPlayers.asStateFlow()
    val sharedTeam = _sharedTeam.asStateFlow()
    val sharedPictureLoaded = _sharedPictureLoaded.asStateFlow()
    val sharedBottomSheetValue = _sharedBottomSheetValue.asSharedFlow()

    init {
        databaseRepository.getPlayers()
            .filterNotNull()
            .onEach { list ->
                preferencesRepository.mkcTeam?.let {
                    val rosters = mutableMapOf<String, List<MKPlayer>>()
                    _sharedTeam.emit(it)
                    _sharedPictureLoaded.emit(it.logoUrl)
                    list.groupBy { it.rosterId }.forEach { rosterId, players ->
                        when (rosterId) {
                            "-1" -> rosters["Allies"] = players.sortedBy { it.name.lowercase() }
                            else ->  databaseRepository.getRosters()
                                .mapNotNull { it.singleOrNull { team -> team.teamId == rosterId } }
                                .onEach {
                                    when (preferencesRepository.teamType) {
                                        is TeamType.SingleRoster ->  rosters["Equipe"] = players.sortedBy { it.name.lowercase() }
                                        else -> rosters[it.name] = players.sortedBy { it.name.lowercase() }
                                    }
                                    _sharedPlayers.value = rosters.toSortedMap(compareByDescending { it })
                                }.launchIn(viewModelScope)
                        }
                    }
                }
            }.launchIn(viewModelScope)
    }

    fun dismissBottomSheet() {
        _sharedBottomSheetValue.value = null
    }

}