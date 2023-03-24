package fr.harmoniamk.statsmk.fragment.home.war

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.enums.UserRole
import fr.harmoniamk.statsmk.extension.*
import fr.harmoniamk.statsmk.model.local.MKWar
import fr.harmoniamk.statsmk.repository.AuthenticationRepositoryInterface
import fr.harmoniamk.statsmk.repository.DatabaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@HiltViewModel
class WarViewModel @Inject constructor(private val firebaseRepository: FirebaseRepositoryInterface, private val preferencesRepository: PreferencesRepositoryInterface, private val authenticationRepository: AuthenticationRepositoryInterface, private val databaseRepository: DatabaseRepositoryInterface) : ViewModel() {

    private val _sharedHasTeam = MutableSharedFlow<Boolean>()
    private val _sharedTeamName = MutableSharedFlow<String>()
    private val _sharedCreateWar = MutableSharedFlow<Unit>()
    private val _sharedCreateTeamDialog = MutableSharedFlow<Boolean>()
    private val _sharedCurrentWar = MutableSharedFlow<MKWar?>()
    private val _sharedCurrentWarClick = MutableSharedFlow<MKWar>()
    private val _sharedLastWars = MutableSharedFlow<List<MKWar>>()
    private val _sharedGoToWar = MutableSharedFlow<MKWar>()
    private val _sharedButtonVisible = MutableSharedFlow<Boolean>()

    val sharedHasTeam = _sharedHasTeam.asSharedFlow()
    val sharedTeamName = _sharedTeamName.asSharedFlow()
    val sharedCreateWar = _sharedCreateWar.asSharedFlow()
    val sharedCreateTeamDialog = _sharedCreateTeamDialog.asSharedFlow()
    val sharedCurrentWar = _sharedCurrentWar.asSharedFlow()
    val sharedCurrentWarClick = _sharedCurrentWarClick.asSharedFlow()
    val sharedLastWars = _sharedLastWars.asSharedFlow()
    val sharedGoToWar = _sharedGoToWar.asSharedFlow()
    val sharedButtonVisible = _sharedButtonVisible.asSharedFlow()

    fun bind(onCreateWar: Flow<Unit>, onCurrentWarClick: Flow<Unit>, onWarClick: Flow<MKWar>, onCreateTeam: Flow<Unit>) {
        refresh()
        onCreateTeam.onEach { _sharedCreateTeamDialog.emit(true) }.launchIn(viewModelScope)
        onWarClick.bind(_sharedGoToWar, viewModelScope)
        onCreateWar.bind(_sharedCreateWar, viewModelScope)
        onCurrentWarClick.mapNotNull { databaseRepository.warList.getCurrent(preferencesRepository.currentTeam?.mid) }.bind(_sharedCurrentWarClick, viewModelScope)
        firebaseRepository.listenToNewWars()
            .map { it.map { MKWar(it) } }
            .flatMapLatest { it.withName(databaseRepository) }
            .onEach {
                databaseRepository.warList.clear()
                databaseRepository.warList.addAll(it)
                refresh()
            }.launchIn(viewModelScope)
    }

    fun bindAddTeamDialog(onTeamAdded: Flow<Unit>) {
        onTeamAdded
            .onEach {
                _sharedCreateTeamDialog.emit(false)
                refresh()
            }.launchIn(viewModelScope)
    }

    fun refresh() {
        authenticationRepository.userRole
            .onEach {
                preferencesRepository.currentTeam?.name?.let { _sharedTeamName.emit(it) }
                _sharedButtonVisible.emit(it >= UserRole.ADMIN.ordinal)
                _sharedHasTeam.emit(preferencesRepository.currentTeam?.mid != null && preferencesRepository.currentTeam?.mid != "-1")
                _sharedCurrentWar.emit(databaseRepository.warList.getCurrent(preferencesRepository.currentTeam?.mid))
                _sharedLastWars.emit(databaseRepository.warList.filter { war -> war.isOver && war.war?.teamHost == preferencesRepository.currentTeam?.mid }.sortedByDescending{ it.war?.createdDate?.formatToDate() }.safeSubList(0, 5))

            }.launchIn(viewModelScope)






    }

}