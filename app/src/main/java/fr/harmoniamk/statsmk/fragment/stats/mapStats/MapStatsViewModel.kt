package fr.harmoniamk.statsmk.fragment.stats.mapStats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.extension.*
import fr.harmoniamk.statsmk.model.local.MKWar
import fr.harmoniamk.statsmk.model.local.MKWarTrack
import fr.harmoniamk.statsmk.model.local.MapDetails
import fr.harmoniamk.statsmk.model.local.MapStats
import fr.harmoniamk.statsmk.repository.AuthenticationRepositoryInterface
import fr.harmoniamk.statsmk.repository.DatabaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
@ExperimentalCoroutinesApi
@FlowPreview
class MapStatsViewModel @Inject constructor(private val preferencesRepository: PreferencesRepositoryInterface, private val firebaseRepository: FirebaseRepositoryInterface, private val databaseRepository: DatabaseRepositoryInterface) : ViewModel() {

    private val _sharedMapClick = MutableSharedFlow<MapDetails>()
    private val _sharedStats = MutableSharedFlow<MapStats>()
    private val _sharedDetailsClick = MutableSharedFlow<Unit>()

    val sharedMapClick = _sharedMapClick.asSharedFlow()
    val sharedStats = _sharedStats.asSharedFlow()
    val sharedDetailsClick = _sharedDetailsClick.asSharedFlow()



    fun bind(trackIndex: Int,
             warList: List<MKWar>,
             onVictoryClick: Flow<Unit>,
             onDefeatClick: Flow<Unit>,
             isIndiv: Boolean?,
             userId: String?,
             teamId: String?,
             isWeek: Boolean?,
             isMonth: Boolean?,
             onDetailsClick: Flow<Unit>
    ) {
        val mapDetailsList = mutableListOf<MapDetails>()
        val onlyIndiv = isIndiv.isTrue || preferencesRepository.currentTeam?.mid == null

         flowOf(warList)
             .filter {
                 (!onlyIndiv && it.mapNotNull { war -> war.war?.teamHost}.contains(preferencesRepository.currentTeam?.mid)
                         || it.map {war -> war.war?.teamOpponent}.contains(preferencesRepository.currentTeam?.mid))
                         || onlyIndiv
             }
             .mapNotNull { list -> list
                 .filter { it.isOver }
                 .filter {  !onlyIndiv || (onlyIndiv && it.hasPlayer(userId)) }
                 .filter {  !isWeek.isTrue || (isWeek.isTrue && it.isThisWeek) }
                 .filter {  !isMonth.isTrue || (isMonth.isTrue && it.isThisMonth) }
              }
             .map {
                val finalList = mutableListOf<MapDetails>()
                 it.withName(databaseRepository).firstOrNull()?.let { list ->
                     list.forEach { mkWar ->
                         mkWar.warTracks?.filter { track -> track.index == trackIndex }?.forEach { track ->
                             val position = track.track?.warPositions?.singleOrNull { it.playerId == userId }?.position?.takeIf { userId != null }
                             finalList.add(MapDetails(mkWar, MKWarTrack(track.track), position))
                         }
                     }
                 }
                finalList
            }
            .filter { it.isNotEmpty() }
            .onEach {
                mapDetailsList.clear()
                mapDetailsList.addAll(it
                    .filter { !onlyIndiv || (onlyIndiv && it.war.war?.warTracks?.any { MKWarTrack(it).hasPlayer(userId) }.isTrue) }
                    .filter { teamId == null || it.war.hasTeam(teamId) }

                )
                _sharedStats.emit(MapStats(mapDetailsList, onlyIndiv && userId != null, userId))
            }.launchIn(viewModelScope)

        onDetailsClick.bind(_sharedDetailsClick, viewModelScope)

        flowOf(
            onVictoryClick.mapNotNull { mapDetailsList.getVictory() },
            onDefeatClick.mapNotNull { mapDetailsList.getDefeat() }
        )
            .flattenMerge()
            .bind(_sharedMapClick, viewModelScope)

    }

}