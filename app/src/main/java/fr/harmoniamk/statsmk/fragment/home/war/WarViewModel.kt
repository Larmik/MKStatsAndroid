package fr.harmoniamk.statsmk.fragment.home.war

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.BuildConfig
import fr.harmoniamk.statsmk.enums.UserRole
import fr.harmoniamk.statsmk.extension.*
import fr.harmoniamk.statsmk.model.firebase.NewWar
import fr.harmoniamk.statsmk.model.firebase.WarDispo
import fr.harmoniamk.statsmk.model.local.MKWar
import fr.harmoniamk.statsmk.repository.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import java.text.SimpleDateFormat
import java.util.*
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
    private val  _sharedShowUpdatePopup = MutableSharedFlow<Unit>()
    private val _sharedNextScheduledWar = MutableSharedFlow<WarDispo>()
    private val _sharedStarted = MutableSharedFlow<Unit>()
    private val _sharedLoading = MutableSharedFlow<Boolean>()

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
    val sharedShowUpdatePopup = _sharedShowUpdatePopup.asSharedFlow()
    val sharedNextScheduledWar = _sharedNextScheduledWar.asSharedFlow()
    val sharedStarted = _sharedStarted.asSharedFlow()
    val sharedLoading = _sharedLoading.asSharedFlow()

    private var currentWar: MKWar? = null
    private var scheduledWar: WarDispo? = null
    private val dispoList = mutableListOf<WarDispo>()

    fun bind(onCreateWar: Flow<Unit>, onCurrentWarClick: Flow<Unit>, onWarClick: Flow<MKWar>, onCreateTeam: Flow<Unit>, onCreateScheduledWar: Flow<Unit>) {
        refresh()
        flowOf(remoteConfigRepository.minimumVersion)
            .onEach { delay(100) }
            .filter { BuildConfig.VERSION_CODE < it }
            .onEach { _sharedShowUpdatePopup.emit(Unit) }
            .launchIn(viewModelScope)

        onCreateTeam
            .onEach { _sharedCreateTeamDialog.emit(true) }
            .launchIn(viewModelScope)

        onWarClick.bind(_sharedGoToWar, viewModelScope)
        onCreateWar.bind(_sharedCreateWar, viewModelScope)
        onCurrentWarClick
            .mapNotNull { currentWar }
            .map { networkRepository.networkAvailable }
            .bind(_sharedCurrentWarClick, viewModelScope)

      firebaseRepository.getDispos()
            .onEach {
                dispoList.clear()
                dispoList.addAll(it)
                val hour = Date().get(Calendar.HOUR_OF_DAY)
                val lastWars = databaseRepository.getWars().firstOrNull()
                    ?.filter { war -> war.war?.teamHost == preferencesRepository.currentTeam?.mid }
                    ?.sortedByDescending { it.war?.createdDate?.formatToDate() }
                    ?.safeSubList(0, 5).orEmpty()
                dispoList.forEach {
                    if (it.lineUp != null && it.opponentId != null) {
                        it.withLineUpAndOpponent(databaseRepository).firstOrNull()?.let {
                            scheduledWar = it
                            _sharedNextScheduledWar.emit(it)
                            _sharedButtonVisible.emit(false)
                        }
                    }
                }
                _sharedDispoVisible.emit(it.isNotEmpty())
                _sharedLastWars.emit(lastWars)
            }.launchIn(viewModelScope)

        firebaseRepository.listenToCurrentWar()
            .onEach {
                if (it == null) {
                    firebaseRepository.getNewWars(preferencesRepository.currentTeam?.mid ?: "")
                        .flatMapLatest { it.map { MKWar(it) }.withName(databaseRepository) }
                        .onEach { databaseRepository.writeWars(it) }
                        .firstOrNull()
                        ?.filter { war -> war.war?.teamHost == preferencesRepository.currentTeam?.mid }
                        ?.sortedByDescending { it.war?.createdDate?.formatToDate() }
                        ?.safeSubList(0, 5)?.let {
                            _sharedLastWars.emit(it)
                        }
                }
                currentWar = it.takeIf { networkRepository.networkAvailable }
                _sharedCurrentWar.emit(currentWar)
            }.launchIn(viewModelScope)

        onCreateScheduledWar
            .onEach { _sharedLoading.emit(true) }
            .mapNotNull { scheduledWar }
            .mapNotNull {
                val war = NewWar(
                    mid = System.currentTimeMillis().toString(),
                    teamHost = preferencesRepository.currentTeam?.mid,
                    playerHostId = authenticationRepository.user?.uid,
                    teamOpponent = it.opponentId,
                    createdDate = SimpleDateFormat("dd/MM/yyyy - HH'h'mm", Locale.FRANCE).format(Date()),
                    isOfficial = false
                )
                it.lineUp?.forEach { userId ->
                    databaseRepository.getUser(userId.userId).firstOrNull()?.let { user ->
                        val new = user.apply { this.currentWar = war.mid }
                        firebaseRepository.writeUser(new).first()
                    }
                }
                preferencesRepository.currentWar = war
                war
            }
            .flatMapLatest { firebaseRepository.writeCurrentWar(it) }
            .bind(_sharedStarted, viewModelScope)
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
                _sharedButtonVisible.emit(it >= UserRole.ADMIN.ordinal && networkRepository.networkAvailable && scheduledWar == null)
                _sharedHasTeam.emit(preferencesRepository.currentTeam?.mid != null && preferencesRepository.currentTeam?.mid != "-1")
            }.launchIn(viewModelScope)
    }

}