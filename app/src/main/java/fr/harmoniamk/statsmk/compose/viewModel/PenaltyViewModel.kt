package fr.harmoniamk.statsmk.compose.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.extension.withName
import fr.harmoniamk.statsmk.model.firebase.Penalty
import fr.harmoniamk.statsmk.model.network.MKCTeam
import fr.harmoniamk.statsmk.repository.DatabaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import javax.inject.Inject


@OptIn(FlowPreview::class)
@ExperimentalCoroutinesApi
@HiltViewModel
class PenaltyViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepositoryInterface,
    private val firebaseRepository: FirebaseRepositoryInterface,
    private val databaseRepository: DatabaseRepositoryInterface) : ViewModel() {

    private val _sharedTeam1 = MutableStateFlow<MKCTeam?>(null)
    private val _sharedTeam2 = MutableStateFlow<MKCTeam?>(null)
    private val _sharedDismiss = MutableSharedFlow<Unit>()

    val sharedTeam1 = _sharedTeam1.asStateFlow()
    val sharedTeam2 = _sharedTeam2.asStateFlow()
    val sharedDismiss = _sharedDismiss.asSharedFlow()

    private var teamSelected: String? = null
    private var amount: Int? = null

    init {
        preferencesRepository.currentWar
            ?.withName(databaseRepository)
            ?.mapNotNull { it?.war }
            ?.onEach {
                teamSelected = it.teamHost
                _sharedTeam1.value = databaseRepository.getNewTeam(it.teamHost).firstOrNull()
                _sharedTeam2.value = databaseRepository.getNewTeam(it.teamOpponent).firstOrNull()
            }
            ?.launchIn(viewModelScope)
    }

    fun onSelectTeam(team: String?) {
        teamSelected = team
    }

    fun onAmount(amount: String) {
        this.amount = amount.toIntOrNull()
    }

    fun onPenaltyAdded() {
        teamSelected?.let { team ->
            amount?.let { amount ->
                val penalties = preferencesRepository.currentWar?.penalties?.toMutableList() ?: mutableListOf()
                penalties.add(Penalty(team, amount))
                preferencesRepository.currentWar?.apply { this.penalties = penalties }?.withName(databaseRepository)
                    ?.mapNotNull { it?.war }
                    ?.flatMapLatest {  firebaseRepository.writeCurrentWar(it) }
                    ?.onEach { _sharedDismiss.emit(Unit) }
                    ?.launchIn(viewModelScope)
                }
            }
        }

}