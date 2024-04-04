package fr.harmoniamk.statsmk.compose.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.model.network.MKCTeam
import fr.harmoniamk.statsmk.repository.DatabaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class TeamListViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepositoryInterface,
    databaseRepository: DatabaseRepositoryInterface,
): ViewModel() {

    private val _sharedTeams = MutableStateFlow<List<MKCTeam>?>( null)
    val sharedTeams = _sharedTeams.asStateFlow()
    private val teams = mutableListOf<MKCTeam>()

    fun search(searched: String) {
        _sharedTeams.value = teams.filter {
            it.team_tag.lowercase()
                .contains(searched.lowercase()) || it.team_name.lowercase().contains(searched.lowercase())
        }.sortedBy { it.team_name }.filterNot { vm -> vm.team_id == preferencesRepository.mkcTeam?.id }
    }

    init {
        databaseRepository.getNewTeams()
            .onEach {
                teams.clear()
                teams.addAll(it
                    .filter { it.player_count >= 6 }
                    .filter { team -> team.team_id != preferencesRepository.mkcTeam?.id  })
                _sharedTeams.value = teams.sortedBy { it.team_name }
            }
            .launchIn(viewModelScope)
    }

}