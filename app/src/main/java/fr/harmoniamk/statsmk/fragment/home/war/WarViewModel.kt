package fr.harmoniamk.statsmk.fragment.home.war

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.enums.UserRole
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.extension.getCurrent
import fr.harmoniamk.statsmk.extension.getLasts
import fr.harmoniamk.statsmk.extension.withName
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
            .flatMapLatest { it.withName(databaseRepository) }
            .shareIn(viewModelScope, SharingStarted.Eagerly, replay = 1)

        warsFlow
            .map { war = it.getCurrent(preferencesRepository.currentTeam?.mid); war}
            .flatMapLatest { listOf(it).withName(databaseRepository) }
            .onEach { _sharedCurrentWar.emit(it.singleOrNull()) }
            .launchIn(viewModelScope)

        warsFlow
            .map { it.getLasts(preferencesRepository.currentTeam?.mid) }
            .bind(_sharedLastWars, viewModelScope)

        warsFlow
            .mapNotNull { preferencesRepository.currentTeam?.name }
            .bind(_sharedTeamName, viewModelScope)

        warsFlow
            .flatMapLatest { databaseRepository.getUser(authenticationRepository.user?.uid) }

            .map{
                it?.team != "-1"
            }
            .bind(_sharedHasTeam, viewModelScope)
    }

}