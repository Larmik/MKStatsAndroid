package fr.harmoniamk.statsmk.fragment.home.war

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.enums.UserRole
import fr.harmoniamk.statsmk.extension.*
import fr.harmoniamk.statsmk.model.firebase.NewWar
import fr.harmoniamk.statsmk.model.local.MKTeam
import fr.harmoniamk.statsmk.model.local.MKWar
import fr.harmoniamk.statsmk.repository.AuthenticationRepositoryInterface
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@HiltViewModel
class WarViewModel @Inject constructor(private val firebaseRepository: FirebaseRepositoryInterface, private val preferencesRepository: PreferencesRepositoryInterface, private val authenticationRepository: AuthenticationRepositoryInterface) : ViewModel() {

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

    private var war: MKWar? = null


    fun bind(onCreateWar: Flow<Unit>, onCurrentWarClick: Flow<Unit>, onWarClick: Flow<MKWar>, onCreateTeam: Flow<Unit>) {
        refresh()
        onCreateTeam.onEach { _sharedCreateTeamDialog.emit(true) }.launchIn(viewModelScope)
        onWarClick.bind(_sharedGoToWar, viewModelScope)
        onCreateWar.bind(_sharedCreateWar, viewModelScope)
        onCurrentWarClick.mapNotNull { war }.bind(_sharedCurrentWarClick, viewModelScope)
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
            .mapNotNull { it >= UserRole.ADMIN.ordinal }
            .bind(_sharedButtonVisible, viewModelScope)

        val warsFlow = flowOf(firebaseRepository.getNewWars(), firebaseRepository.listenToNewWars())
            .flattenMerge()
            .map { it.map { MKWar(it) } }
            .shareIn(viewModelScope, SharingStarted.Eagerly, replay = 1)

        warsFlow
            .map { war = it.getCurrent(preferencesRepository.currentTeam?.mid); war}
            .flatMapLatest { listOf(it).withName(firebaseRepository) }
            .onEach { _sharedCurrentWar.emit(it.singleOrNull()) }
            .launchIn(viewModelScope)

        warsFlow
            .map { it.getLasts(preferencesRepository.currentTeam?.mid) }
            .map {
                val temp = mutableListOf<MKWar>()
                it.forEach {
                    val hostName = firebaseRepository.getTeam(it.war?.teamHost).firstOrNull()?.shortName
                    val opponentName = firebaseRepository.getTeam(it.war?.teamOpponent).firstOrNull()?.shortName
                    temp.add(it.apply { this.name = "$hostName - $opponentName" })
                }
                temp
            }
            .bind(_sharedLastWars, viewModelScope)

        warsFlow
            .mapNotNull { preferencesRepository.currentTeam?.name }
            .bind(_sharedTeamName, viewModelScope)

        firebaseRepository.getUsers()
            .map{
                it.singleOrNull {
                        user -> user.mid == authenticationRepository.user?.uid
                }?.team != "-1"
            }
            .bind(_sharedHasTeam, viewModelScope)
    }

}