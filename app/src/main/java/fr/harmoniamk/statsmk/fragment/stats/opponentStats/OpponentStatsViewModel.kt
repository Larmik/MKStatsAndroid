package fr.harmoniamk.statsmk.fragment.stats.opponentStats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.extension.positionToPoints
import fr.harmoniamk.statsmk.extension.sum
import fr.harmoniamk.statsmk.fragment.stats.opponentRanking.OpponentRankingItemViewModel
import fr.harmoniamk.statsmk.model.firebase.User
import fr.harmoniamk.statsmk.model.local.MKWarPosition
import fr.harmoniamk.statsmk.repository.AuthenticationRepositoryInterface
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class OpponentStatsViewModel @Inject constructor(private val authenticationRepository: AuthenticationRepositoryInterface, private val firebaseRepository: FirebaseRepositoryInterface) : ViewModel() {

    private val _sharedHighestScore = MutableSharedFlow<Pair<Int, String?>?>()
    val sharedHighestScore = _sharedHighestScore.asSharedFlow()
    private val _sharedLowestScore = MutableSharedFlow<Pair<Int, String?>?>()
    val sharedLowestScore = _sharedLowestScore.asSharedFlow()
    private val _sharedDetailsClick = MutableSharedFlow<Unit>()
    val sharedDetailsClick = _sharedDetailsClick.asSharedFlow()

    private val users = mutableListOf<User>()

    fun bind(stats: OpponentRankingItemViewModel?, isIndiv: Boolean, onDetailsClick: Flow<Unit>) {

        firebaseRepository.getUsers()
            .filter { isIndiv }
            .onEach {
                users.clear()
                users.addAll(it)
            }
            .mapNotNull { stats }
            .onEach {
                val finalList = mutableListOf<Pair<Int, String?>>()
                it.stats.warStats.list.forEach { war ->
                    val positions = mutableListOf<Pair<User?, Int>>()
                    war.warTracks?.forEach { warTrack ->
                        warTrack.track?.warPositions?.let { warPositions ->
                            val trackPositions = mutableListOf<MKWarPosition>()
                            warPositions.forEach { position ->
                                trackPositions.add(MKWarPosition(position, users.singleOrNull { it.mid ==  position.playerId }))
                            }
                            trackPositions.groupBy { it.player }.entries.forEach { entry ->
                                positions.add(Pair(entry.key, entry.value.map { pos -> pos.position.position.positionToPoints() }.sum()))
                            }
                        }
                    }
                    val temp = positions
                        .groupBy { it.first }
                        .map { Pair(it.key, it.value.map { it.second }.sum()) }
                        .filter { it.first?.mid == authenticationRepository.user?.uid }
                        .map { Pair(it.second, war.war?.createdDate) }

                    temp.forEach { pair ->
                        finalList.add(pair)
                    }
                }
                _sharedHighestScore.emit(finalList.sortedByDescending { it.first }.firstOrNull())
                _sharedLowestScore.emit(finalList.sortedBy { it.first }.firstOrNull())
            }
            .launchIn(viewModelScope)
        onDetailsClick.bind(_sharedDetailsClick, viewModelScope)




    }
}