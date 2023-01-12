package fr.harmoniamk.statsmk.fragment.addPenalty

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.model.firebase.NewWar
import fr.harmoniamk.statsmk.model.firebase.Penalty
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject


@ExperimentalCoroutinesApi
@HiltViewModel
class AddPenaltyViewModel @Inject constructor(private val firebaseRepository: FirebaseRepositoryInterface) : ViewModel() {

    private val _sharedTeam1Label = MutableSharedFlow<String>()
    private val _sharedTeam2Label = MutableSharedFlow<String>()
    private val _sharedTeam1Selected = MutableSharedFlow<Boolean>()
    private val _sharedDismiss = MutableSharedFlow<Unit>()

    val sharedTeam1Label = _sharedTeam1Label.asSharedFlow()
    val sharedTeam2Label = _sharedTeam2Label.asSharedFlow()
    val sharedTeam1Selected = _sharedTeam1Selected.asSharedFlow()
    val sharedDismiss = _sharedDismiss.asSharedFlow()

    private var amount: Int? = null

    fun bind(war: NewWar, onTeamSelected: Flow<String>, onAmountAdded: Flow<String?>, onPenaltyClick: Flow<Unit>) {
        var teamSelected: String? = war.teamHost
        firebaseRepository.getTeam(war.teamHost)
            .mapNotNull { it?.name }
            .onEach {
                _sharedTeam1Selected.emit(true)
                _sharedTeam1Label.emit(it)
            }.launchIn(viewModelScope)
        firebaseRepository.getTeam(war.teamOpponent)
            .mapNotNull { it?.name }
            .bind(_sharedTeam2Label, viewModelScope)

        onTeamSelected
            .onEach {
                teamSelected = it
                _sharedTeam1Selected.emit(it == war.teamHost)
            }.launchIn(viewModelScope)
        onAmountAdded.filterNotNull().onEach { amount = it.toIntOrNull() }.launchIn(viewModelScope)
        onPenaltyClick
            .mapNotNull { teamSelected }
            .mapNotNull { amount?.let { amount ->  Penalty(it, amount) } }
            .flatMapLatest {
                val penalties = war.penalties?.toMutableList() ?: mutableListOf()
                penalties.add(it)
                val newWar = war.apply { this.penalties = penalties }
                firebaseRepository.writeNewWar(newWar)
            }
            .onEach { _sharedDismiss.emit(Unit) }
            .launchIn(viewModelScope)


    }
}