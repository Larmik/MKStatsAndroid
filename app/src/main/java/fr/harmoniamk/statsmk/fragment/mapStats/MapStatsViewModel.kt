package fr.harmoniamk.statsmk.fragment.mapStats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.extension.*
import fr.harmoniamk.statsmk.model.local.MKWar
import fr.harmoniamk.statsmk.model.local.MKWarTrack
import fr.harmoniamk.statsmk.model.local.MapDetails
import fr.harmoniamk.statsmk.model.local.MapStats
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
@ExperimentalCoroutinesApi
@FlowPreview
class MapStatsViewModel @Inject constructor(private val preferencesRepository: PreferencesRepositoryInterface, private val firebaseRepository: FirebaseRepositoryInterface) : ViewModel() {

    private val _sharedMapClick = MutableSharedFlow<MapDetails>()
    private val _sharedStats = MutableSharedFlow<MapStats>()

    val sharedMapClick = _sharedMapClick.asSharedFlow()
    val sharedStats = _sharedStats.asSharedFlow()


    fun bind(trackIndex: Int,
             onMapClick: Flow<MapDetails>,
             onVictoryClick: Flow<Unit>,
             onDefeatClick: Flow<Unit>,
             isIndiv: Boolean?
    ) {
        val list = mutableListOf<MapDetails>()
        flowOf(preferencesRepository.currentTeam?.mid)
            .filterNotNull()
            .flatMapLatest { firebaseRepository.getNewWars() }
            .filter { it.mapNotNull { war -> war.teamHost}.contains(preferencesRepository.currentTeam?.mid)
                        || it.map {war -> war.teamOpponent}.contains(preferencesRepository.currentTeam?.mid) }
            .mapNotNull {
                val finalList = mutableListOf<MapDetails>()
                it.forEach { war ->
                   listOf(MKWar(war)).withName(firebaseRepository).first().singleOrNull()?.let { finalWar ->
                       war.warTracks?.filter { track -> track.trackIndex == trackIndex }?.forEach { track ->
                           finalList.add(MapDetails(finalWar, MKWarTrack(track)))
                       }
                   }
                }
                finalList
            }
            .filter { it.isNotEmpty() }
            .onEach {
                list.clear()
                list.addAll(it.filter { !isIndiv.isTrue || (isIndiv.isTrue && it.war.hasPlayer(preferencesRepository.currentUser?.mid)) })
                _sharedStats.emit(MapStats(list, isIndiv.isTrue, preferencesRepository))
            }.launchIn(viewModelScope)

        flowOf(
            onMapClick,
            onVictoryClick.mapNotNull { list.getVictory() },
            onDefeatClick.mapNotNull { list.getDefeat() }
        )
            .flattenMerge()
            .bind(_sharedMapClick, viewModelScope)

    }

}