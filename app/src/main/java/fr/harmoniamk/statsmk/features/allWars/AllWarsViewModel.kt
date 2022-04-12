package fr.harmoniamk.statsmk.features.allWars

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.extension.formatToDate
import fr.harmoniamk.statsmk.model.MKWar
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@HiltViewModel
class AllWarsViewModel @Inject constructor(private val firebaseRepository: FirebaseRepositoryInterface, private val preferencesRepository: PreferencesRepositoryInterface) : ViewModel() {

    private val _sharedWars = MutableSharedFlow<List<MKWar>>()
    private val _sharedTeamName = MutableSharedFlow<String?>()
    private val _sharedWarClick = MutableSharedFlow<MKWar>()

    val sharedWars = _sharedWars.asSharedFlow()
    val sharedTeamName = _sharedTeamName.asSharedFlow()
    val sharedWarClick = _sharedWarClick.asSharedFlow()

    fun bind(onItemClick: Flow<MKWar>) {
        firebaseRepository.getWars()
            .onEach { _sharedTeamName.emit(preferencesRepository.currentTeam?.name) }
            .mapNotNull { list -> list.filter { war -> war.teamHost == preferencesRepository.currentTeam?.mid }.sortedByDescending { it.createdDate?.formatToDate() }.map { MKWar(it) } }
            .bind(_sharedWars, viewModelScope)
        onItemClick.bind(_sharedWarClick, viewModelScope)
    }

}