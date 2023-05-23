package fr.harmoniamk.statsmk.fragment.dispos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.database.entities.TopicEntity
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.extension.isTrue
import fr.harmoniamk.statsmk.model.firebase.Dispo
import fr.harmoniamk.statsmk.model.firebase.PlayerDispo
import fr.harmoniamk.statsmk.model.firebase.WarDispo
import fr.harmoniamk.statsmk.model.local.ScheduledWar
import fr.harmoniamk.statsmk.repository.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@HiltViewModel
class DispoViewModel @Inject constructor(private val firebaseRepository: FirebaseRepositoryInterface, private val databaseRepository: DatabaseRepositoryInterface, private val authenticationRepository: AuthenticationRepositoryInterface, private val preferencesRepository: PreferencesRepositoryInterface, private val notificationsRepository: NotificationsRepositoryInterface) : ViewModel() {

    private val _sharedDispos = MutableSharedFlow<List<WarDispo>>()
    val sharedDispo = _sharedDispos.asSharedFlow()
    private val dispos = mutableListOf<WarDispo>()
    val teamId = preferencesRepository.currentTeam?.mid ?: ""

    fun bind(onDispoSelected: Flow<Pair<WarDispo, Dispo>>, onClickWarSchedule: Flow<Unit>, onWarScheduled: Flow<ScheduledWar>) {
        firebaseRepository.getDispos()
            .map {
                val finalDispos = mutableListOf<WarDispo>()
                it.forEach { dispo ->
                    val playerDispoList = mutableListOf<PlayerDispo>()
                    dispo.dispoPlayers.forEach {
                        val listName = mutableListOf<String?>()
                        it.players?.forEach {
                            listName.add(databaseRepository.getUser(it.takeIf{it != "-1"}).firstOrNull()?.name)
                        }
                        playerDispoList.add(it.apply { this.playerNames = listName.filterNotNull() })
                    }
                    finalDispos.add(dispo.apply { this.dispoPlayers = playerDispoList })
                }
                finalDispos
            }
            .onEach {
                dispos.clear()
                dispos.addAll(it)
            }.bind(_sharedDispos, viewModelScope)

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
                val finalDispos = mutableListOf<WarDispo>()
                dispos.forEach { warDispo ->
                   val finalDispo = mutableListOf<PlayerDispo>()
                    warDispo.dispoPlayers.forEach { playerDispo ->
                        val finalPlayers = mutableListOf<String?>()
                        finalPlayers.addAll(playerDispo.players?.filter { it != authenticationRepository.user?.uid || warDispo.dispoHour != pair.first.dispoHour}.orEmpty())
                        if (warDispo.dispoHour == pair.first.dispoHour) {
                            if (playerDispo.dispo == pair.second.ordinal)
                                finalPlayers.add(authenticationRepository.user?.uid)
                        }
                        finalDispo.add(playerDispo.apply {
                            this.players = finalPlayers.filterNotNull()
                            this.playerNames = null
                        })
                    }
                    finalDispos.add(warDispo.apply { this.dispoPlayers = finalDispo })
                }
                finalDispos
            }
            .flatMapLatest { firebaseRepository.writeDispo(it) }
            .launchIn(viewModelScope)

        onWarScheduled
            .onEach {
                if (it.lineUp.contains(authenticationRepository.user?.uid)) {
                    switchTopic(teamId + "_lu_infos_" + it.hour.toString(), false)
                    switchTopic(teamId + "_war_reminder_" + it.hour.toString(), true)
                }
            }.launchIn(viewModelScope)
    }

    private fun switchTopic(topic: String, subscribed: Boolean) = flow {
        when (subscribed) {
            true -> databaseRepository.writeTopic(TopicEntity(topic = topic)).first()
            else -> databaseRepository.deleteTopic(topic).first()
        }
        emit(Unit)
    }

}