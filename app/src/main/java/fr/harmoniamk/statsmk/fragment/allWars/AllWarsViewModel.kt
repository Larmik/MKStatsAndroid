package fr.harmoniamk.statsmk.fragment.allWars

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.extension.formatToDate
import fr.harmoniamk.statsmk.extension.isTrue
import fr.harmoniamk.statsmk.extension.withName
import fr.harmoniamk.statsmk.model.firebase.Team
import fr.harmoniamk.statsmk.model.local.MKWar
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import java.util.*
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@HiltViewModel
class AllWarsViewModel @Inject constructor(private val firebaseRepository: FirebaseRepositoryInterface, private val preferencesRepository: PreferencesRepositoryInterface) : ViewModel() {

    private val _sharedWars = MutableSharedFlow<List<MKWar>>()
    private val _sharedWarClick = MutableSharedFlow<MKWar>()

    val sharedWars = _sharedWars.asSharedFlow()
    val sharedWarClick = _sharedWarClick.asSharedFlow()

    private val wars = mutableListOf<MKWar>()
    private val teams = mutableListOf<Team>()

    fun bind(onItemClick: Flow<MKWar>, onSearch: Flow<String>) {
        firebaseRepository.getNewWars()
            .mapNotNull { list -> list.filter { war -> war.teamHost == preferencesRepository.currentTeam?.mid }.sortedByDescending { it.createdDate?.formatToDate() }.map { MKWar(it) } }
            .flatMapLatest { it.withName(firebaseRepository) }
            .onEach {
                wars.clear()
                wars.addAll(it)
                _sharedWars.emit(wars)
            }
            .flatMapLatest { firebaseRepository.getTeams() }
            .onEach {
                teams.clear()
                teams.addAll(it)
            }
            .launchIn(viewModelScope)
        onItemClick.bind(_sharedWarClick, viewModelScope)

        onSearch
            .map { searched ->
                val filteredTeams = teams.filter { it.name?.toLowerCase()?.contains(searched.toLowerCase()).isTrue }
                val filteredWars = mutableListOf<MKWar>()
                filteredTeams.forEach { team ->
                    filteredWars.addAll(wars.filter { it.war?.teamOpponent?.equals(team.mid).isTrue })
                }
                filteredWars
            }
            .bind(_sharedWars, viewModelScope)
    }

}