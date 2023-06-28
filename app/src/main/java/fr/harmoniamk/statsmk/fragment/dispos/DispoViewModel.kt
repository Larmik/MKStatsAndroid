package fr.harmoniamk.statsmk.fragment.dispos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.database.entities.TopicEntity
import fr.harmoniamk.statsmk.enums.UserRole
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.extension.withLineUpAndOpponent
import fr.harmoniamk.statsmk.model.firebase.Dispo
import fr.harmoniamk.statsmk.model.firebase.PlayerDispo
import fr.harmoniamk.statsmk.model.firebase.WarDispo
import fr.harmoniamk.statsmk.repository.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@HiltViewModel
class DispoViewModel @Inject constructor(private val firebaseRepository: FirebaseRepositoryInterface, private val databaseRepository: DatabaseRepositoryInterface, private val authenticationRepository: AuthenticationRepositoryInterface, preferencesRepository: PreferencesRepositoryInterface, private val notificationsRepository: NotificationsRepositoryInterface) : ViewModel() {

    private val _sharedDispos = MutableSharedFlow<List<Pair<WarDispo, Boolean>>>()
    private val _sharedPopupShowing = MutableSharedFlow<Boolean>()
    private val _sharedShowOtherPlayers = MutableSharedFlow<WarDispo>()
    private val _sharedGoToScheduleWar = MutableSharedFlow<WarDispo>()

    val sharedDispo = _sharedDispos.asSharedFlow()
    val sharedGoToScheduleWar = _sharedGoToScheduleWar.asSharedFlow()
    val sharedShowOtherPlayers = _sharedShowOtherPlayers.asSharedFlow()
    val sharedPopupShowing = _sharedPopupShowing.asSharedFlow()

    private val dispos = mutableListOf<WarDispo>()
    val teamId = preferencesRepository.currentTeam?.mid ?: ""

    private var dispoDetails: String? = null

    fun bind(onDispoSelected: Flow<Pair<WarDispo, Dispo>>, onClickWarSchedule: Flow<WarDispo>, onClickOtherPlayer: Flow<WarDispo>, onPopup: Flow<Unit>) {
        firebaseRepository.getDispos()
            .map {
                val finalDispos = mutableListOf<WarDispo>()
                it.forEach { dispo ->
                    val playerDispoList = mutableListOf<PlayerDispo>()
                    dispo.dispoPlayers?.forEach {
                        val listName = mutableListOf<String?>()
                        it.players?.forEach {
                            listName.add(databaseRepository.getUser(it.takeIf{it != "-1"}).firstOrNull()?.name)
                        }
                        playerDispoList.add(it.apply { this.playerNames = listName.filterNotNull() })
                    }
                    dispo.withLineUpAndOpponent(databaseRepository).firstOrNull()?.let {
                        finalDispos.add(it.apply {
                            this.dispoPlayers = playerDispoList
                        })
                    }
                }
                finalDispos
            }
            .onEach {
                dispos.clear()
                dispos.addAll(it)
            }
            .map {
                val role = authenticationRepository.userRole.firstOrNull() ?: 0
                it.map { Pair(it, role >= UserRole.ADMIN.ordinal) }
            }
            .bind(_sharedDispos, viewModelScope)

        onPopup
            .flatMapLatest {  authenticationRepository.userRole }
            .map { it >= UserRole.LEADER.ordinal }
            .bind(_sharedPopupShowing, viewModelScope)

        onDispoSelected
            .onEach { pair ->
                when (pair.second) {
                    Dispo.CANT -> {
                        notificationsRepository.switchNotification(teamId + "_dispo_reminder_" + pair.first.dispoHour.toString(), false).firstOrNull()?.let {
                            switchTopic(teamId + "_dispo_reminder_" + pair.first.dispoHour.toString(), false).first()
                        }
                        notificationsRepository.switchNotification(teamId + "_lu_infos_" + pair.first.dispoHour.toString(), false).firstOrNull()?.let {
                            switchTopic(teamId + "_lu_infos_" + pair.first.dispoHour.toString(), false).first()
                        }
                    }
                    Dispo.NOT_SURE -> {
                        notificationsRepository.switchNotification(teamId + "_lu_infos_" + pair.first.dispoHour.toString(), true).firstOrNull()?.let {
                            switchTopic(teamId + "_lu_infos_" + pair.first.dispoHour.toString(), true).first()
                        }
                        notificationsRepository.switchNotification(teamId + "_dispo_reminder_" + pair.first.dispoHour.toString(), true).firstOrNull()?.let {
                            switchTopic(teamId + "_dispo_reminder_" + pair.first.dispoHour.toString(), true).first()
                        }
                    }
                    else -> {
                        notificationsRepository.switchNotification(teamId + "_dispo_reminder_" + pair.first.dispoHour.toString(), false).firstOrNull()?.let {
                            switchTopic(teamId + "_dispo_reminder_" + pair.first.dispoHour.toString(), false).first()
                        }
                        notificationsRepository.switchNotification(teamId + "_lu_infos_" + pair.first.dispoHour.toString(), true).firstOrNull()?.let {
                            switchTopic(teamId + "_lu_infos_" + pair.first.dispoHour.toString(), true).first()
                        }
                    }
                }
            }
            .map { pair ->
                val warDispo = pair.first
                val playerDispos = mutableListOf<PlayerDispo>()
                warDispo.dispoPlayers?.forEach { playerDispo ->
                    val finalPlayers = mutableListOf<String?>()
                    finalPlayers.addAll(playerDispo.players?.filter { it != authenticationRepository.user?.uid || warDispo.dispoHour != pair.first.dispoHour}.orEmpty())
                    if (warDispo.dispoHour == pair.first.dispoHour) {
                        if (playerDispo.dispo == pair.second.ordinal)
                            finalPlayers.add(authenticationRepository.user?.uid)
                    }
                    val listName = mutableListOf<String?>()
                    finalPlayers.forEach {
                        listName.add(databaseRepository.getUser(it.takeIf{it != "-1"}).firstOrNull()?.name)
                    }
                    playerDispos.add(playerDispo.apply {
                        this.players = finalPlayers.filterNotNull()
                        this.playerNames = listName.filterNotNull()
                    })
                }
                warDispo.apply { this.dispoPlayers = playerDispos }
            }
            .flatMapLatest { firebaseRepository.writeDispo(it) }
            .launchIn(viewModelScope)

        onClickWarSchedule
            .bind(_sharedGoToScheduleWar, viewModelScope)

        onClickOtherPlayer
            .bind(_sharedShowOtherPlayers, viewModelScope)

        onPopup.onEach { _sharedPopupShowing.emit(true) }.launchIn(viewModelScope)
    }

    fun bindPopup(onDetailsValidated: Flow<Unit>, onDismiss: Flow<Unit>,  onDetailsAdded: Flow<String>) {
        onDetailsAdded
            .onEach { dispoDetails = it }
            .launchIn(viewModelScope)

        onDismiss
            .onEach { _sharedPopupShowing.emit(false) }
            .launchIn(viewModelScope)

        onDetailsValidated
            .mapNotNull {
                dispos.lastOrNull()?.apply { this.details = dispoDetails }

            }.flatMapLatest { firebaseRepository.writeDispo(it) }
            .onEach { _sharedPopupShowing.emit(false) }
            .launchIn(viewModelScope)
    }

    private fun switchTopic(topic: String, subscribed: Boolean) = flow {
        when (subscribed) {
            true -> databaseRepository.writeTopic(TopicEntity(topic = topic)).first()
            else -> databaseRepository.deleteTopic(topic).first()
        }
        emit(Unit)
    }

}