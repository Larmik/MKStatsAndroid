package fr.harmoniamk.statsmk.fragment.dispos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.fragment.playerSelect.UserSelector
import fr.harmoniamk.statsmk.model.firebase.PlayerDispo
import fr.harmoniamk.statsmk.model.firebase.WarDispo
import fr.harmoniamk.statsmk.repository.DatabaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
@FlowPreview
@ExperimentalCoroutinesApi
class OtherPlayersViewModel @Inject constructor(private val firebaseRepository: FirebaseRepositoryInterface, private val databaseRepository: DatabaseRepositoryInterface, private val preferencesRepository: PreferencesRepositoryInterface) : ViewModel() {

    private val _sharedPlayers = MutableSharedFlow<List<UserSelector>>()
    private val _sharedDismiss = MutableSharedFlow<Unit>()
    private val _sharedLoading = MutableSharedFlow<Unit>()

    val sharedPlayers = _sharedPlayers.asSharedFlow()
    val sharedDismiss = _sharedDismiss.asSharedFlow()
    val sharedLoading = _sharedLoading.asSharedFlow()

    private var playerSelected: String? = null
    var dispo: Int? = null

    fun bind(dispo: WarDispo, onPlayerSelected: Flow<UserSelector>, onButtonClick: Flow<Int>) {
        val alreadyDispoUsers = mutableListOf<String>()
        dispo.dispoPlayers?.forEach { dispos ->
            alreadyDispoUsers.addAll(dispos.players.orEmpty())
        }

        databaseRepository.getUsers()
            .map { it
                .filter { user -> user.team == preferencesRepository.currentTeam?.mid && !alreadyDispoUsers.contains(user.mid)}
                .map { UserSelector(it, false) }
            }.bind(_sharedPlayers, viewModelScope)

        onPlayerSelected
            .onEach { playerSelected = it.user?.mid }
            .launchIn(viewModelScope)

        onButtonClick
            .onEach { this.dispo = it }
            .mapNotNull { playerSelected }
            .onEach { _sharedLoading.emit(Unit) }
            .map {
                val playersDispo = mutableListOf<PlayerDispo>()
                dispo.dispoPlayers?.forEach { playerDispo ->
                    when (playerDispo.dispo == this.dispo) {
                        true -> {
                            val players = mutableListOf<String?>()
                            players.addAll(playerDispo.players.orEmpty())
                            players.add(playerSelected)
                            playersDispo.add(playerDispo.apply { this.players = players.distinct().filterNotNull() })
                        }
                        else -> playersDispo.add(playerDispo)
                    }
                }
                dispo.apply { this.dispoPlayers = playersDispo }
            }.flatMapLatest { firebaseRepository.writeDispo(it) }
            .bind(_sharedDismiss, viewModelScope)
    }

}