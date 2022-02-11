package fr.harmoniamk.statsmk.features.addWar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.database.firebase.model.Team
import fr.harmoniamk.statsmk.database.firebase.model.War
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@HiltViewModel
class AddWarViewModel @Inject constructor(private val firebaseRepository: FirebaseRepositoryInterface, private val preferencesRepository: PreferencesRepositoryInterface): ViewModel() {

    private val _sharedTeams = MutableSharedFlow<List<Team>>()
    private val _sharedStarted = MutableSharedFlow<Unit>()
    private val _sharedTeamSelected = MutableSharedFlow<String>()
    private val _sharedAlreadyCreated = MutableSharedFlow<Unit>()

    val sharedTeams = _sharedTeams.asSharedFlow()
    val sharedStarted = _sharedStarted.asSharedFlow()
    val sharedTeamSelected = _sharedTeamSelected.asSharedFlow()
    val sharedAlreadyCreated = _sharedAlreadyCreated.asSharedFlow()

    fun bind(onTeamClick: Flow<Team>, onCreateWar: Flow<Unit>) {
        val date = SimpleDateFormat("dd/MM/yyyy - HH'h'mm", Locale.FRANCE).format(Date())
        var chosenOpponent: Team? = null
        var warName: String? = null

        firebaseRepository.getTeams().map {
            it.filterNot { team -> team.mid == preferencesRepository.currentTeam?.mid }
        }.bind(_sharedTeams, viewModelScope)

        onTeamClick.onEach {
            chosenOpponent = it
            warName = "${preferencesRepository.currentTeam?.shortName} - ${it.shortName}"
            _sharedTeamSelected.emit("Démarrer $warName" )
        }.launchIn(viewModelScope)

        val createWar = onCreateWar
            .flatMapLatest { firebaseRepository.getWars() }
            .map { it.singleOrNull { war -> !war.isOver && war.teamHost == preferencesRepository.currentTeam?.mid} }
            .shareIn(viewModelScope, SharingStarted.Eagerly, replay = 1)

        createWar
            .filter { it == null}
            .mapNotNull { chosenOpponent?.mid }
            .map {
                val war = War(
                    mid = System.currentTimeMillis().toString(),
                    name = warName,
                    teamHost = preferencesRepository.currentTeam?.mid,
                    playerHostId = preferencesRepository.currentUser?.mid,
                    teamOpponent = it,
                    trackPlayed = 0,
                    createdDate = date,
                    updatedDate = date)
                war
            }
            .flatMapLatest { firebaseRepository.writeWar(it) }
            .bind(_sharedStarted, viewModelScope)

        createWar
            .filter { it != null }
            .map {  }
            .bind(_sharedAlreadyCreated, viewModelScope)
    }
}