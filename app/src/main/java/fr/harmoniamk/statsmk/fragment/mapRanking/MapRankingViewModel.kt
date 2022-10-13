package fr.harmoniamk.statsmk.fragment.mapRanking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.application.MainApplication
import fr.harmoniamk.statsmk.enums.Maps
import fr.harmoniamk.statsmk.enums.SortType
import fr.harmoniamk.statsmk.extension.*
import fr.harmoniamk.statsmk.model.firebase.NewWarTrack
import fr.harmoniamk.statsmk.model.local.MKWar
import fr.harmoniamk.statsmk.model.local.MKWarTrack
import fr.harmoniamk.statsmk.model.local.TrackStats
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
class MapRankingViewModel @Inject constructor(private val preferencesRepository: PreferencesRepositoryInterface, private val firebaseRepository: FirebaseRepositoryInterface) : ViewModel() {


    private val _sharedMaps = MutableSharedFlow<List<TrackStats>>()
    private val _sharedGoToStats = MutableSharedFlow<Int>()
    private val _sharedSortTypeSelected = MutableStateFlow(SortType.TOTAL_PLAYED)


    val sharedMaps = _sharedMaps.asSharedFlow()
    val sharedGoToStats = _sharedGoToStats.asSharedFlow()
    val sharedSortTypeSelected = _sharedSortTypeSelected.asStateFlow()
    val temp = mutableListOf<NewWarTrack>()
    val final = mutableListOf<TrackStats>()

    fun bind(onTrackClick: Flow<Int>, onSortClick: Flow<SortType>, onSearch: Flow<String>) {
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
                final.addAll(when (_sharedSortTypeSelected.value) {
                    SortType.TOTAL_PLAYED -> list.sortBySize()
                    SortType.TOTAL_WIN -> list.sortByVictory()
                    SortType.WINRATE -> list.sortByWinRate()
                    SortType.AVERAGE_DIFF -> list.sortByAverage()

                }.map {
                    TrackStats(
                        map = Maps.values()[it.first ?: -1],
                        totalPlayed = it.second.size,
                        winRate = (it.second.filter { MKWarTrack(it).displayedDiff.contains('+') }.size * 100) / it.second.size
                    )
                }.filter { it.totalPlayed >= 2 })
                final
            }
            .bind(_sharedMaps, viewModelScope)

        onTrackClick.bind(_sharedGoToStats, viewModelScope)

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
                final.addAll( when (it) {
                    SortType.TOTAL_PLAYED -> temp.sortBySize()
                    SortType.TOTAL_WIN -> temp.sortByVictory()
                    SortType.WINRATE -> temp.sortByWinRate()
                    SortType.AVERAGE_DIFF -> temp.sortByAverage()

                } .map {
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

}