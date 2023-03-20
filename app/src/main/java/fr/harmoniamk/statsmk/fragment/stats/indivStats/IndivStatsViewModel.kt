package fr.harmoniamk.statsmk.fragment.stats.indivStats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.enums.Maps
import fr.harmoniamk.statsmk.extension.*
import fr.harmoniamk.statsmk.model.local.*
import fr.harmoniamk.statsmk.repository.AuthenticationRepositoryInterface
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@HiltViewModel
class IndivStatsViewModel @Inject constructor(
    private val firebaseRepository: FirebaseRepositoryInterface,
    private val authenticationRepository: AuthenticationRepositoryInterface
) : ViewModel() {

    private val _sharedTrackClick = MutableSharedFlow<Int>()
    private val _sharedWarClick = MutableSharedFlow<MKWar>()
    private val _sharedStats = MutableSharedFlow<Stats>()

    val sharedTrackClick = _sharedTrackClick.asSharedFlow()
    val sharedWarClick = _sharedWarClick.asSharedFlow()
    val sharedStats = _sharedStats.asSharedFlow()

    private var bestMap: TrackStats? = null
    private var worstMap: TrackStats? = null
    private var mostPlayedMap: TrackStats? = null
    private var highestVicory: MKWar? = null
    private var loudestDefeat: MKWar? = null
    private var highestScore: MKWar? = null
    private var lowestScore: MKWar? = null

    fun bind(
        list: List<MKWar>?,
        onBestClick: Flow<Unit>,
        onWorstClick: Flow<Unit>,
        onMostPlayedClick: Flow<Unit>,
        onVictoryClick: Flow<Unit>,
        onDefeatClick: Flow<Unit>,
        onHighestScore: Flow<Unit>,
        onLowestScore: Flow<Unit>,
        ) {

             flowOf(list)
                .filterNotNull()
                 .flatMapLatest { it.withName(firebaseRepository) }
                 .flatMapLatest { it.withFullStats(firebaseRepository, authenticationRepository.user?.uid) }
                 .onEach { stats ->
                    bestMap = stats.averageForMaps.filter { it.totalPlayed >= 2 }.maxByOrNull { it.playerScore ?: 0 }
                    worstMap = stats.averageForMaps.filter { it.totalPlayed >= 2 }.minByOrNull { it.playerScore ?: 0 }
                    mostPlayedMap = stats.averageForMaps.maxByOrNull { it.totalPlayed }
                    highestVicory = stats.warStats.highestVictory
                    loudestDefeat = stats.warStats.loudestDefeat
                     highestScore = stats.highestScore?.war
                     lowestScore = stats.lowestScore?.war
                    _sharedStats.emit(stats)
                }.launchIn(viewModelScope)

        flowOf(
            onBestClick.mapNotNull { bestMap },
            onWorstClick.mapNotNull { worstMap },
            onMostPlayedClick.mapNotNull { mostPlayedMap },
        ).flattenMerge()
            .map { Maps.values().indexOf(it.map) }
            .bind(_sharedTrackClick, viewModelScope)

        flowOf(onVictoryClick.mapNotNull { highestVicory }, onDefeatClick.mapNotNull { loudestDefeat }, onHighestScore.mapNotNull { highestScore }, onLowestScore.mapNotNull { lowestScore })
            .flattenMerge()
            .bind(_sharedWarClick, viewModelScope)
    }

}