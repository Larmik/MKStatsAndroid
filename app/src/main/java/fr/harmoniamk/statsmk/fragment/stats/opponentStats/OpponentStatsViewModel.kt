package fr.harmoniamk.statsmk.fragment.stats.opponentStats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.enums.Maps
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.extension.positionToPoints
import fr.harmoniamk.statsmk.extension.sum
import fr.harmoniamk.statsmk.fragment.stats.opponentRanking.OpponentRankingItemViewModel
import fr.harmoniamk.statsmk.fragment.stats.playerRanking.PlayerRankingItemViewModel
import fr.harmoniamk.statsmk.model.firebase.User
import fr.harmoniamk.statsmk.model.local.MKWar
import fr.harmoniamk.statsmk.model.local.MKWarPosition
import fr.harmoniamk.statsmk.repository.AuthenticationRepositoryInterface
import fr.harmoniamk.statsmk.repository.DatabaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@FlowPreview
@HiltViewModel
class OpponentStatsViewModel @Inject constructor(private val databaseRepository: DatabaseRepositoryInterface) : ViewModel() {

    private val _sharedHighestScore = MutableSharedFlow<Pair<Int, String?>?>()
    val sharedHighestScore = _sharedHighestScore.asSharedFlow()
    private val _sharedLowestScore = MutableSharedFlow<Pair<Int, String?>?>()
    val sharedLowestScore = _sharedLowestScore.asSharedFlow()
    private val _sharedDetailsClick = MutableSharedFlow<Unit>()
    val sharedDetailsClick = _sharedDetailsClick.asSharedFlow()


    private val _sharedTrackClick = MutableSharedFlow<Pair<String?, Int>>()
    private val _sharedWarClick = MutableSharedFlow<MKWar>()


    val sharedTrackClick = _sharedTrackClick.asSharedFlow()
    val sharedWarClick = _sharedWarClick.asSharedFlow()

    private val users = mutableListOf<User>()
    private var item: OpponentRankingItemViewModel? = null

    fun bind(stats: OpponentRankingItemViewModel?, userId: String?, isIndiv: Boolean, onDetailsClick: Flow<Unit>,
             onBestClick: Flow<Unit>,
             onWorstClick: Flow<Unit>,
             onMostPlayedClick: Flow<Unit>,
             onVictoryClick: Flow<Unit>,
             onDefeatClick: Flow<Unit>,
             onHighestScore: Flow<Unit>,
             onLowestScore: Flow<Unit>) {

        databaseRepository.getUsers()
            .onEach {
                users.clear()
                users.addAll(it)
            }
            .mapNotNull { stats }
            .onEach {
                item = it
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
                        .filter { it.first?.mid == userId }
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
        flowOf(
            onBestClick.mapNotNull { if (isIndiv && userId != null) item?.stats?.bestPlayerMap else item?.stats?.bestMap },
            onWorstClick.mapNotNull { if (isIndiv && userId != null) item?.stats?.worstPlayerMap else item?.stats?.worstMap },
            onMostPlayedClick.mapNotNull { item?.stats?.mostPlayedMap },
        ).flattenMerge()
            .map { Maps.values().indexOf(it.map) }
            .map { Pair(userId, it) }
            .bind(_sharedTrackClick, viewModelScope)

        flowOf(onVictoryClick.mapNotNull { item?.stats?.warStats?.highestVictory }, onDefeatClick.mapNotNull { item?.stats?.warStats?.loudestDefeat }, onHighestScore.mapNotNull { item?.stats?.highestScore?.war }, onLowestScore.mapNotNull { item?.stats?.lowestScore?.war })
            .flattenMerge()
            .bind(_sharedWarClick, viewModelScope)




    }
}