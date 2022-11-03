package fr.harmoniamk.statsmk.fragment.mapRanking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.application.MainApplication
import fr.harmoniamk.statsmk.enums.Maps
import fr.harmoniamk.statsmk.enums.TrackSortType
import fr.harmoniamk.statsmk.extension.*
import fr.harmoniamk.statsmk.model.firebase.NewWarTrack
import fr.harmoniamk.statsmk.model.local.MKWar
import fr.harmoniamk.statsmk.model.local.MKWarTrack
import fr.harmoniamk.statsmk.model.local.TrackStats
import fr.harmoniamk.statsmk.repository.AuthenticationRepositoryInterface
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import java.util.*
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@HiltViewModel
class MapRankingViewModel @Inject constructor(private val preferencesRepository: PreferencesRepositoryInterface, private val firebaseRepository: FirebaseRepositoryInterface, private val authenticationRepository: AuthenticationRepositoryInterface) : ViewModel() {


    private val _sharedMaps = MutableSharedFlow<List<TrackStats>>()
    private val _sharedGoToStats = MutableSharedFlow<Pair<Int, Boolean>>()
    private val _sharedSortTypeSelected = MutableStateFlow(TrackSortType.TOTAL_PLAYED)
    private val _sharedIndivStatsEnabled = MutableStateFlow(true)


    val sharedMaps = _sharedMaps.asSharedFlow()
    val sharedGoToStats = _sharedGoToStats.asSharedFlow()
    val sharedSortTypeSelected = _sharedSortTypeSelected.asStateFlow()
    val sharedIndivStatsEnabled = _sharedIndivStatsEnabled.asStateFlow()
    val temp = mutableListOf<NewWarTrack>()
    val final = mutableListOf<TrackStats>()

    fun bind(onTrackClick: Flow<Int>, onSortClick: Flow<TrackSortType>, onSearch: Flow<String>, onIndivStatsSelected: Flow<Boolean>) {
        flowOf(preferencesRepository.currentTeam?.mid)
            .filterNotNull()
            .flatMapLatest { firebaseRepository.getNewWars() }
            .filter { it.mapNotNull { war -> war.teamHost}.contains(preferencesRepository.currentTeam?.mid)
                    || it.map {war -> war.teamOpponent}.contains(preferencesRepository.currentTeam?.mid) }
            .mapNotNull { list -> list.map { MKWar(it) }.filter { it.isOver } }
            .map { list ->
                val allTracksPlayed = mutableListOf<NewWarTrack>()
                list.mapNotNull { it.war?.warTracks }.forEach {
                    allTracksPlayed.addAll(it)
                }
                allTracksPlayed
            }
            .map { list ->
                temp.clear()
                temp.addAll(list)
                final.clear()
                final.addAll(sortTracks(_sharedSortTypeSelected.value).map {
                    TrackStats(
                        map = Maps.values()[it.first ?: -1],
                        totalPlayed = it.second.size,
                        winRate = (it.second.filter { MKWarTrack(it).displayedDiff.contains('+') }.size * 100) / it.second.size
                    )
                }.filter { it.totalPlayed >= 2 })
                final
            }
            .bind(_sharedMaps, viewModelScope)

        onTrackClick.onEach { _sharedGoToStats.emit(Pair(it, _sharedIndivStatsEnabled.value)) }.launchIn(viewModelScope)
        onIndivStatsSelected.onEach {
            _sharedIndivStatsEnabled.emit(it)
            final.clear()
            final.addAll(sortTracks(_sharedSortTypeSelected.value).map {
                TrackStats(
                    map = Maps.values()[it.first ?: -1],
                    totalPlayed = it.second.size,
                    winRate = (it.second.filter { MKWarTrack(it).displayedDiff.contains('+') }.size * 100) / it.second.size
                )
            }.filter { it.totalPlayed >= 2 })
            _sharedMaps.emit(final)
        }.launchIn(viewModelScope)

        onSearch
            .map { searched ->
                final
                    .filter {
                    it.map?.name?.toLowerCase(Locale.ROOT)?.contains(searched.toLowerCase(Locale.ROOT)).isTrue
                            || MainApplication.instance?.applicationContext?.getString(it.map?.label ?: R.string.app_name)?.toLowerCase(Locale.ROOT)?.contains(searched.toLowerCase(Locale.ROOT)) ?: true
                }
            }
            .bind(_sharedMaps, viewModelScope)

        onSortClick
            .map {
                _sharedSortTypeSelected.emit(it)
                final.clear()
                final.addAll(sortTracks(it).map {
                    TrackStats(
                        map = Maps.values()[it.first ?: -1],
                        totalPlayed = it.second.size,
                        winRate = (it.second.filter {
                            MKWarTrack(it).displayedDiff.contains(
                                '+'
                            )
                        }.size * 100) / it.second.size
                    )
                }.filter { it.totalPlayed >= 2 })
                        final


            }.bind(_sharedMaps, viewModelScope)
    }

    private fun sortTracks(type: TrackSortType) =
        when (type) {
            TrackSortType.TOTAL_PLAYED -> temp
                .filter { !_sharedIndivStatsEnabled.value || (_sharedIndivStatsEnabled.value && MKWarTrack(it).hasPlayer(preferencesRepository.userId)) }
                .groupBy { it.trackIndex }.toList()
                .sortedByDescending { it.second.size }
            TrackSortType.TOTAL_WIN -> temp
                .filter { !_sharedIndivStatsEnabled.value || (_sharedIndivStatsEnabled.value && MKWarTrack(it).hasPlayer(preferencesRepository.userId)) }
                .groupBy { it.trackIndex }.toList()
                .sortedByDescending { it.second.filter { MKWarTrack(it).displayedDiff.contains('+') }.size }
            TrackSortType.WINRATE -> temp
                .filter { !_sharedIndivStatsEnabled.value || (_sharedIndivStatsEnabled.value && MKWarTrack(it).hasPlayer(preferencesRepository.userId)) }
                .groupBy { it.trackIndex }.toList()
                .sortedByDescending { it.second.filter { MKWarTrack(it).displayedDiff.contains('+') }.size * 100 / it.second.size }
            TrackSortType.AVERAGE_DIFF -> temp
                .filter { !_sharedIndivStatsEnabled.value || (_sharedIndivStatsEnabled.value && MKWarTrack(it).hasPlayer(preferencesRepository.userId)) }
                .groupBy { it.trackIndex }.toList()
                .sortedByDescending { it.second.map { MKWarTrack(it).diffScore }.sum() / it.second.size }







    }


}