package fr.harmoniamk.statsmk.compose.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.extension.isTrue
import fr.harmoniamk.statsmk.model.firebase.Team
import fr.harmoniamk.statsmk.repository.DatabaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class TeamListViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepositoryInterface,
    private val databaseRepository: DatabaseRepositoryInterface
): ViewModel() {

    private val _sharedTeams = MutableStateFlow<List<Team>?>( null)
    private val _sharedAddTeam = MutableSharedFlow<Unit>()

    val sharedTeams = _sharedTeams.asStateFlow()
    val sharedAddTeam = _sharedAddTeam.asSharedFlow()

    private val teams = mutableListOf<Team>()

    fun search(searched: String) {
        _sharedTeams.value = teams.filter {
            it.shortName?.toLowerCase(Locale.ROOT)
                ?.contains(searched.toLowerCase(Locale.ROOT)).isTrue || it.name?.toLowerCase(
                Locale.ROOT)?.contains(searched.toLowerCase(Locale.ROOT)) ?: true
        }.sortedBy { it.name }.filterNot { vm -> vm.mid == preferencesRepository.currentTeam?.mid }
    }

    init {
        databaseRepository.getTeams()
            .onEach {
                teams.clear()
                teams.addAll(it.filterNot { team -> team.mid == preferencesRepository.currentTeam?.mid })
                _sharedTeams.value = teams.sortedBy { it.name }
            }
            .launchIn(viewModelScope)
    }

    fun bind(onAddTeam: Flow<Unit>) {
        onAddTeam.bind(_sharedAddTeam, viewModelScope)
    }

    fun bindAddDialog(onTeamAdded: Flow<Unit>) {
        onTeamAdded
            .flatMapLatest {  databaseRepository.getTeams() }
            .map { list -> list.sortedBy { it.name }.filterNot { vm -> vm.mid == preferencesRepository.currentTeam?.mid } }
            .bind(_sharedTeams, viewModelScope)
    }

}