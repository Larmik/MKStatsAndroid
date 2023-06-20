package fr.harmoniamk.statsmk.compose.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.BuildConfig
import fr.harmoniamk.statsmk.enums.UserRole
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.extension.formatToDate
import fr.harmoniamk.statsmk.extension.get
import fr.harmoniamk.statsmk.extension.safeSubList
import fr.harmoniamk.statsmk.extension.withLineUpAndOpponent
import fr.harmoniamk.statsmk.extension.withName
import fr.harmoniamk.statsmk.model.firebase.NewWar
import fr.harmoniamk.statsmk.model.firebase.Team
import fr.harmoniamk.statsmk.model.firebase.WarDispo
import fr.harmoniamk.statsmk.model.local.MKWar
import fr.harmoniamk.statsmk.repository.AuthenticationRepositoryInterface
import fr.harmoniamk.statsmk.repository.DatabaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.NetworkRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import fr.harmoniamk.statsmk.repository.RemoteConfigRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
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
    private val remoteConfigRepository: RemoteConfigRepositoryInterface
) : ViewModel() {

    private val _sharedCurrentWar = MutableStateFlow<MKWar?>(null)
    private val _sharedLastWars = MutableStateFlow<List<MKWar>?>(null)
    private val _sharedTeam = MutableStateFlow<Team?>(null)
    private val _sharedDispoVisible = MutableStateFlow<List<WarDispo>?>(null)
    private val _sharedCreateManualWarEnabled = MutableStateFlow(false)



    private val _sharedCurrentWarClick = MutableSharedFlow<Boolean>()
    private val _sharedGoToWar = MutableSharedFlow<MKWar>()



    private val _sharedHasTeam = MutableSharedFlow<Boolean>()
    private val _sharedCreateWar = MutableSharedFlow<Unit>()
    private val _sharedCreateTeamDialog = MutableSharedFlow<Boolean>()
    private val  _sharedShowUpdatePopup = MutableSharedFlow<Unit>()
    private val _sharedNextScheduledWar = MutableSharedFlow<WarDispo>()
    private val _sharedStarted = MutableSharedFlow<Unit>()
    private val _sharedLoading = MutableSharedFlow<Boolean>()

    val sharedCurrentWar = _sharedCurrentWar.asStateFlow()
    val sharedLastWars = _sharedLastWars.asStateFlow()
    val sharedTeam = _sharedTeam.asStateFlow()
    val sharedDispos = _sharedDispoVisible.asStateFlow()
    val sharedCreateManualWarEnabled = _sharedCreateManualWarEnabled.asStateFlow()



    val sharedHasTeam = _sharedHasTeam.asSharedFlow()
    val sharedCreateWar = _sharedCreateWar.asSharedFlow()
    val sharedCreateTeamDialog = _sharedCreateTeamDialog.asSharedFlow()
    val sharedCurrentWarClick = _sharedCurrentWarClick.asSharedFlow()
    val sharedGoToWar = _sharedGoToWar.asSharedFlow()
    val sharedShowUpdatePopup = _sharedShowUpdatePopup.asSharedFlow()
    val sharedNextScheduledWar = _sharedNextScheduledWar.asSharedFlow()
    val sharedStarted = _sharedStarted.asSharedFlow()
    val sharedLoading = _sharedLoading.asSharedFlow()

    private var currentWar: MKWar? = null
    private var scheduledWar: WarDispo? = null
    private val dispoList = mutableListOf<WarDispo>()

    init {
        authenticationRepository.userRole
            .onEach {
                _sharedTeam.value = preferencesRepository.currentTeam
                _sharedHasTeam.emit(preferencesRepository.currentTeam?.mid.takeIf { it != "-1" } != null)
            }.launchIn(viewModelScope)

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
                            _sharedCreateManualWarEnabled.value = false
                        }
                    }
                }
                _sharedDispoVisible.value = it
                _sharedLastWars.value = lastWars
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
                _sharedCreateManualWarEnabled.value = it == null
                _sharedCurrentWar.value = currentWar
            }.launchIn(viewModelScope)
    }

    fun bind(onCreateWar: Flow<Unit>, onCurrentWarClick: Flow<Unit>, onWarClick: Flow<MKWar>, onCreateTeam: Flow<Unit>, onCreateScheduledWar: Flow<Unit>) {
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

        onCreateScheduledWar
            .onEach { _sharedLoading.emit(true) }
            .mapNotNull { scheduledWar }
            .mapNotNull {
                val war = NewWar(
                    mid = System.currentTimeMillis().toString(),
                    teamHost = preferencesRepository.currentTeam?.mid,
                    playerHostId = authenticationRepository.user?.uid,
                    teamOpponent = it.opponentId,
                    createdDate = SimpleDateFormat("dd/MM/yyyy - HH'h'mm", Locale.FRANCE).format(
                        Date()
                    ),
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
                authenticationRepository.userRole
                    .onEach {
                        _sharedTeam.value = preferencesRepository.currentTeam
                        // _sharedCreateManualWarEnabled.value = (it >= UserRole.ADMIN.ordinal && networkRepository.networkAvailable && scheduledWar == null && currentWar == null)
                        _sharedHasTeam.emit(preferencesRepository.currentTeam?.mid.takeIf { it != "-1" } != null)
                    }.launchIn(viewModelScope)
            }.launchIn(viewModelScope)
    }


}