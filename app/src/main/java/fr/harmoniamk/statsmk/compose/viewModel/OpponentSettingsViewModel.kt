package fr.harmoniamk.statsmk.compose.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.extension.isTrue
import fr.harmoniamk.statsmk.model.network.MKCTeam
import fr.harmoniamk.statsmk.repository.DatabaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.MKCentralRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@HiltViewModel
class OpponentSettingsViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepositoryInterface,
    mkCentralRepository: MKCentralRepositoryInterface,
    private val databaseRepository: DatabaseRepositoryInterface,
) : ViewModel() {

    private val _sharedTeams = MutableStateFlow<List<MKCTeam>>(listOf())
    val sharedTeams = _sharedTeams.asStateFlow()
    private val teams = mutableListOf<MKCTeam>()

    init {
        mkCentralRepository.teams
            .map {
                teams.clear()
                teams.addAll(it.filterNot { team -> team.team_id.toString() == preferencesRepository.currentTeam?.mid }); teams
            }.bind(_sharedTeams, viewModelScope)
    }

    fun onSearch(search: String) {
        _sharedTeams.value = when (search.isNotEmpty()) {
            true -> teams.filter { it.team_name.lowercase().contains(search).isTrue || it.team_tag.lowercase().contains(search).isTrue }
            else -> teams
        }
    }

}