package fr.harmoniamk.statsmk.fragment.home.war

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.enums.UserRole
import fr.harmoniamk.statsmk.extension.*
import fr.harmoniamk.statsmk.model.local.MKWar
import fr.harmoniamk.statsmk.repository.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@HiltViewModel
class WarViewModel @Inject constructor(private val firebaseRepository: FirebaseRepositoryInterface, private val preferencesRepository: PreferencesRepositoryInterface, private val authenticationRepository: AuthenticationRepositoryInterface, private val databaseRepository: DatabaseRepositoryInterface, private val networkRepository: NetworkRepositoryInterface) : ViewModel() {

    private val _sharedHasTeam = MutableSharedFlow<Boolean>()
    private val _sharedTeamName = MutableSharedFlow<String>()
    private val _sharedCreateWar = MutableSharedFlow<Unit>()
    private val _sharedCreateTeamDialog = MutableSharedFlow<Boolean>()
    private val _sharedCurrentWar = MutableSharedFlow<MKWar?>()
    private val _sharedCurrentWarClick = MutableSharedFlow<Pair<MKWar, Boolean>>()
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

    private var firstCall = true

    fun bind(onCreateWar: Flow<Unit>, onCurrentWarClick: Flow<Unit>, onWarClick: Flow<MKWar>, onCreateTeam: Flow<Unit>) {
        refresh()
        onCreateTeam.onEach { _sharedCreateTeamDialog.emit(true) }.launchIn(viewModelScope)
        onWarClick.bind(_sharedGoToWar, viewModelScope)
        onCreateWar.bind(_sharedCreateWar, viewModelScope)
        onCurrentWarClick
            .flatMapLatest { databaseRepository.getWars() }
            .mapNotNull { it.getCurrent(preferencesRepository.currentTeam?.mid) }
            .map { Pair(it, networkRepository.networkAvailable) }
            .bind(_sharedCurrentWarClick, viewModelScope)

        firebaseRepository.listenToNewWars()
            .map { it.map { MKWar(it) } }
            .flatMapLatest { it.withName(databaseRepository) }
            .flatMapLatest { databaseRepository.writeWars(it) }
            .flatMapLatest { databaseRepository.getWars() }
            .onEach {
                it.takeIf { networkRepository.networkAvailable && !firstCall }?.let { list ->
                    when (val current = list.getCurrent(preferencesRepository.currentTeam?.mid)) {
                        null -> refresh()
                        else -> _sharedCurrentWar.emit(current)
                    }
                }
                firstCall = false
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
                _sharedButtonVisible.emit(it >= UserRole.ADMIN.ordinal && networkRepository.networkAvailable)
                _sharedHasTeam.emit(preferencesRepository.currentTeam?.mid != null && preferencesRepository.currentTeam?.mid != "-1")
            }
            .flatMapLatest { databaseRepository.getWars() }
            .onEach {
                _sharedCurrentWar.emit(it.takeIf { networkRepository.networkAvailable }?.getCurrent(preferencesRepository.currentTeam?.mid))
                _sharedLastWars.emit(it.filter { war -> war.isOver && war.war?.teamHost == preferencesRepository.currentTeam?.mid }.sortedByDescending{ it.war?.createdDate?.formatToDate() }.safeSubList(0, 5))
            }.launchIn(viewModelScope)






    }

}