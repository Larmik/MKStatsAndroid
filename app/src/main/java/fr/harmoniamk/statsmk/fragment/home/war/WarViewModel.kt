package fr.harmoniamk.statsmk.fragment.home.war

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.BuildConfig
import fr.harmoniamk.statsmk.enums.UserRole
import fr.harmoniamk.statsmk.extension.*
import fr.harmoniamk.statsmk.model.local.MKWar
import fr.harmoniamk.statsmk.repository.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@HiltViewModel
class WarViewModel @Inject constructor(
    private val firebaseRepository: FirebaseRepositoryInterface,
    private val preferencesRepository: PreferencesRepositoryInterface,
    private val authenticationRepository: AuthenticationRepositoryInterface,
    private val databaseRepository: DatabaseRepositoryInterface,
    private val networkRepository: NetworkRepositoryInterface,
    private val remoteConfigRepository: RemoteConfigRepositoryInterface) : ViewModel() {

    private val _sharedHasTeam = MutableSharedFlow<Boolean>()
    private val _sharedTeamName = MutableSharedFlow<String>()
    private val _sharedCreateWar = MutableSharedFlow<Unit>()
    private val _sharedCreateTeamDialog = MutableSharedFlow<Boolean>()
    private val _sharedCurrentWar = MutableSharedFlow<MKWar?>()
    private val _sharedCurrentWarClick = MutableSharedFlow<Boolean>()
    private val _sharedLastWars = MutableSharedFlow<List<MKWar>>()
    private val _sharedGoToWar = MutableSharedFlow<MKWar>()
    private val _sharedButtonVisible = MutableSharedFlow<Boolean>()
    private val _sharedDispoVisible = MutableSharedFlow<Boolean>()
    private val _sharedLoaded = MutableSharedFlow<Unit>()
    private val  _sharedShowUpdatePopup = MutableSharedFlow<Unit>()

    val sharedHasTeam = _sharedHasTeam.asSharedFlow()
    val sharedTeamName = _sharedTeamName.asSharedFlow()
    val sharedCreateWar = _sharedCreateWar.asSharedFlow()
    val sharedCreateTeamDialog = _sharedCreateTeamDialog.asSharedFlow()
    val sharedCurrentWar = _sharedCurrentWar.asSharedFlow()
    val sharedCurrentWarClick = _sharedCurrentWarClick.asSharedFlow()
    val sharedLastWars = _sharedLastWars.asSharedFlow()
    val sharedGoToWar = _sharedGoToWar.asSharedFlow()
    val sharedButtonVisible = _sharedButtonVisible.asSharedFlow()
    val sharedDispoVisible = _sharedDispoVisible.asSharedFlow()
    val sharedLoaded = _sharedLoaded.asSharedFlow()
    val sharedShowUpdatePopup = _sharedShowUpdatePopup.asSharedFlow()

    private var currentWar: MKWar? = null

    fun bind(onCreateWar: Flow<Unit>, onCurrentWarClick: Flow<Unit>, onWarClick: Flow<MKWar>, onCreateTeam: Flow<Unit>) {
        refresh()

        val shouldUpdate = flowOf(remoteConfigRepository.minimumVersion)
            .onEach { delay(100) }
            .map { BuildConfig.VERSION_CODE < it }
            .shareIn(viewModelScope, SharingStarted.Eagerly)

        onCreateTeam.onEach { _sharedCreateTeamDialog.emit(true) }.launchIn(viewModelScope)
        onWarClick.bind(_sharedGoToWar, viewModelScope)
        onCreateWar.bind(_sharedCreateWar, viewModelScope)
        onCurrentWarClick
            .mapNotNull { currentWar }
            .map { networkRepository.networkAvailable }
            .bind(_sharedCurrentWarClick, viewModelScope)

        shouldUpdate
            .filter { it }
            .onEach { _sharedShowUpdatePopup.emit(Unit) }
            .launchIn(viewModelScope)

        shouldUpdate
            .filterNot { it }
            .flatMapLatest {  firebaseRepository.listenToNewWars() }
            .map { it.map { MKWar(it) } }
            .flatMapLatest { it.withName(databaseRepository) }
            .onEach {
                currentWar = it.takeIf { networkRepository.networkAvailable }?.getCurrent(preferencesRepository.currentTeam?.mid)
                _sharedCurrentWar.emit(currentWar)
                _sharedLastWars.emit(it.filter { war -> war.isOver && war.war?.teamHost == preferencesRepository.currentTeam?.mid }.sortedByDescending{ it.war?.createdDate?.formatToDate() }.safeSubList(0, 5))
                _sharedLoaded.emit(Unit)
            }
            .flatMapLatest { databaseRepository.writeWars(it) }
            .flatMapLatest { firebaseRepository.getUsers() }
            .flatMapLatest { databaseRepository.writeUsers(it) }
            .launchIn(viewModelScope)

        firebaseRepository.getDispos()
            .onEach {
                    _sharedDispoVisible.emit(it.isNotEmpty())

            }.launchIn(viewModelScope)
    }

    fun bindAddTeamDialog(onTeamAdded: Flow<Unit>) {
        onTeamAdded
            .onEach {
                _sharedCreateTeamDialog.emit(false)
                refresh()
            }.launchIn(viewModelScope)
    }

    private fun refresh() {
        authenticationRepository.userRole
            .onEach {
                preferencesRepository.currentTeam?.name?.let { _sharedTeamName.emit(it) }
                _sharedButtonVisible.emit(it >= UserRole.ADMIN.ordinal && networkRepository.networkAvailable)
                _sharedHasTeam.emit(preferencesRepository.currentTeam?.mid != null && preferencesRepository.currentTeam?.mid != "-1")
            }.launchIn(viewModelScope)
    }

}