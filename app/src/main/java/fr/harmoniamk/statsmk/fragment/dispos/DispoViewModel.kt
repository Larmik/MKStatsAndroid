package fr.harmoniamk.statsmk.fragment.dispos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.model.firebase.Dispo
import fr.harmoniamk.statsmk.model.firebase.PlayerDispo
import fr.harmoniamk.statsmk.model.firebase.WarDispo
import fr.harmoniamk.statsmk.repository.AuthenticationRepositoryInterface
import fr.harmoniamk.statsmk.repository.DatabaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class DispoViewModel @Inject constructor(private val firebaseRepository: FirebaseRepositoryInterface, private val databaseRepository: DatabaseRepositoryInterface, private val authenticationRepository: AuthenticationRepositoryInterface) : ViewModel() {

    private val _sharedDispos = MutableSharedFlow<List<WarDispo>>()
    val sharedDispo = _sharedDispos.asSharedFlow()
    private val dispos = mutableListOf<WarDispo>()

    fun bind(onDispoSelected: Flow<Pair<WarDispo, Dispo>>) {
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
    }

}