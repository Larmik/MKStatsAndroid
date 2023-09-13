package fr.harmoniamk.statsmk.compose.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.extension.isTrue
import fr.harmoniamk.statsmk.model.local.MKWarTrack
import fr.harmoniamk.statsmk.model.local.MapDetails
import fr.harmoniamk.statsmk.repository.AuthenticationRepositoryInterface
import fr.harmoniamk.statsmk.repository.DatabaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class WarTrackListViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepositoryInterface,
    private val databaseRepository: DatabaseRepositoryInterface,
    private val authenticationRepository: AuthenticationRepositoryInterface
) : ViewModel() {


    var onlyIndiv = preferencesRepository.currentTeam?.mid == null

    private val _sharedMapStats = MutableStateFlow<List<MapDetails>?>(null)

    val sharedMapStats = _sharedMapStats.asStateFlow()

    fun init(trackIndex: Int, teamId: String?, userId: String?) {

        databaseRepository.getWars()
            .map {
                onlyIndiv = userId != null || preferencesRepository.currentTeam?.mid == null
                when {
                    userId != null && teamId != null -> it.filter { war ->
                        war.hasPlayer(
                            authenticationRepository.user?.uid
                        ) && war.hasTeam(teamId)
                    }

                    onlyIndiv -> it.filter { war ->
                        war.hasPlayer(
                            userId ?: authenticationRepository.user?.uid
                        )
                    }

                    else -> it.filter { war ->
                        war.hasTeam(
                            teamId ?: preferencesRepository.currentTeam?.mid
                        )
                    }
                }
            }

            .mapNotNull { list ->
                list
                    .filter {
                        (onlyIndiv && it.hasPlayer(userId)) || !onlyIndiv && it.hasTeam(
                            preferencesRepository.currentTeam?.mid
                        )
                    }
            }
            .map {
                val finalList = mutableListOf<MapDetails>()
                it.forEach { mkWar ->
                    mkWar.warTracks?.filter { track -> track.index == trackIndex }
                        ?.forEach { track ->
                            val position =
                                track.track?.warPositions?.singleOrNull { it.playerId == userId }?.position?.takeIf { userId != null }
                            finalList.add(MapDetails(mkWar, MKWarTrack(track.track), position))
                        }
                }
                finalList
            }
            .filter { it.isNotEmpty() }
            .onEach {
                val mapDetailsList = mutableListOf<MapDetails>()
                mapDetailsList.addAll(it
                    .filter {
                        !onlyIndiv || (onlyIndiv && it.war.war?.warTracks?.any {
                            MKWarTrack(
                                it
                            ).hasPlayer(userId)
                        }.isTrue)
                    }
                    .filter { teamId == null || it.war.hasTeam(teamId) }

                )
                _sharedMapStats.value = mapDetailsList
            }.launchIn(viewModelScope)
    }
}