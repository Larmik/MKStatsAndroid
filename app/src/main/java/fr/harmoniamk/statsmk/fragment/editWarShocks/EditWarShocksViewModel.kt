package fr.harmoniamk.statsmk.fragment.editWarShocks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.model.firebase.NewWar
import fr.harmoniamk.statsmk.model.firebase.NewWarTrack
import fr.harmoniamk.statsmk.model.firebase.Shock
import fr.harmoniamk.statsmk.model.firebase.User
import fr.harmoniamk.statsmk.model.local.MKWarPosition
import fr.harmoniamk.statsmk.repository.DatabaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class EditWarShocksViewModel  @Inject constructor(private val firebaseRepository: FirebaseRepositoryInterface, private val databaseRepository: DatabaseRepositoryInterface): ViewModel() {

    private val _sharedPlayers = MutableSharedFlow<List<MKWarPosition>?>()
    private val _sharedShocks = MutableSharedFlow<List<Pair<String?, Shock>>>()
    private val _sharedDismiss = MutableSharedFlow<Unit>()
    val sharedShocks = _sharedShocks.asSharedFlow()
    val sharedPlayers = _sharedPlayers.asSharedFlow()
    val sharedDismiss = _sharedDismiss.asSharedFlow()

    private val shockList = mutableListOf<Pair<String?, Shock>>()
    private val playerList = mutableListOf<MKWarPosition>()
    private val users = mutableListOf<User>()



    fun bind(war: NewWar, track: NewWarTrack?, onValid: Flow<Unit>, onShockAdded: Flow<String>, onShockRemoved: Flow<String>) {
        val shocks = mutableMapOf<String?, Int>()
        databaseRepository.getUsers()
            .onEach { list ->
                users.addAll(list)
                track?.warPositions?.forEach {
                    list.singleOrNull { user -> user.mid == it.playerId }?.let { user ->
                        playerList.add(MKWarPosition(it, user))
                    }

                }
                track?.shocks?.forEach {
                    val user = list.singleOrNull { user -> user.mid == it.playerId }
                    shockList.add(Pair(user?.name, it))
                    shocks[user?.mid] = it.count
                }
                _sharedShocks.emit(shockList)
                _sharedPlayers.emit(playerList)
            }.launchIn(viewModelScope)

        onShockRemoved
            .onEach { id -> shocks[id]?.takeIf { it > 0 }?.let { shocks[id] = it-1 } }
            .map { id ->
                shockList.clear()
                shocks.forEach { shock ->
                    shock.takeIf { map -> map.value > 0 }?.let {
                        val name = databaseRepository.getUser(it.key).firstOrNull()?.name
                        shockList.add(Pair(name, Shock(it.key, it.value)))
                    }
                }
                shockList
            }
           .bind(_sharedShocks, viewModelScope)

        onShockAdded
            .onEach { id -> shocks[id]?.let { shocks[id] = it+1 } }
            .map { id ->
                shockList.clear()
                if (shocks[id] == null)
                    shocks[id] = 1
                shocks.forEach { shock ->
                    shock.takeIf { map -> map.value > 0 }?.let {
                        val name = databaseRepository.getUser(it.key).firstOrNull()?.name
                        shockList.add(Pair(name, Shock(it.key, it.value)))
                    }
                }
                shockList
            }
         .bind(_sharedShocks, viewModelScope)

        onValid
            .mapNotNull { war }
            .map {
                val newWarTrack = NewWarTrack(track?.mid, track?.trackIndex, track?.warPositions, shockList.map { it.second })
                val newTrackList = mutableListOf<NewWarTrack>()
                war.warTracks?.forEach { track ->
                    if (track.mid == newWarTrack.mid)
                        newTrackList.add(newWarTrack)
                    else
                        newTrackList.add(track)
                }
                NewWar(war.mid, war.playerHostId, war.teamHost, war.teamOpponent, war.createdDate, newTrackList, war.penalties, war.isOfficial)
            }.flatMapLatest { firebaseRepository.writeNewWar(it) }
            .bind(_sharedDismiss, viewModelScope)



    }
}

