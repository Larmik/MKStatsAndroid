package fr.harmoniamk.statsmk.compose.viewModel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.EntryPointAccessors
import fr.harmoniamk.statsmk.compose.ViewModelFactoryProvider
import fr.harmoniamk.statsmk.extension.positionToPoints
import fr.harmoniamk.statsmk.extension.sum
import fr.harmoniamk.statsmk.model.firebase.Shock
import fr.harmoniamk.statsmk.model.local.CurrentPlayerModel
import fr.harmoniamk.statsmk.model.local.MKWar
import fr.harmoniamk.statsmk.model.local.MKWarPosition
import fr.harmoniamk.statsmk.model.local.MKWarTrack
import fr.harmoniamk.statsmk.model.network.MKPlayer
import fr.harmoniamk.statsmk.repository.DatabaseRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach


@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class WarDetailsViewModel  @AssistedInject constructor(
    @Assisted private val id: String,
    private val databaseRepository: DatabaseRepositoryInterface
) : ViewModel() {

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun provideFactory(
            assistedFactory: Factory,
            id: String?
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return assistedFactory.create(id) as T
            }
        }

        @Composable
        fun viewModel(id: String?): WarDetailsViewModel {
            val factory: Factory = EntryPointAccessors.fromApplication<ViewModelFactoryProvider>(context = LocalContext.current).warDetailsViewModel
            return androidx.lifecycle.viewmodel.compose.viewModel(
                factory = provideFactory(
                    assistedFactory = factory,
                    id = id
                )
            )
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(id: String?): WarDetailsViewModel
    }
    private val _sharedWar = MutableStateFlow<MKWar?>(null)
    private val _sharedWarPlayers = MutableStateFlow<List<CurrentPlayerModel>?>(null)
    private val _sharedTracks = MutableStateFlow<List<MKWarTrack>?>(null)

    val sharedWar = _sharedWar.asStateFlow()
    val sharedTracks = _sharedTracks.asStateFlow()
    val sharedWarPlayers = _sharedWarPlayers.asStateFlow()

    init {
        databaseRepository.getWar(id)
            .onEach { war ->
                _sharedWar.value = war
            }
            .mapNotNull { it?.warTracks }
            .onEach {
                val positions = mutableListOf<Pair<MKPlayer?, Int>>()
                val players = databaseRepository.getRoster().firstOrNull()
                val shocks = mutableStateListOf<Shock>()
                _sharedTracks.emit(it)
                it.forEach {
                    val trackPositions = mutableListOf<MKWarPosition>()
                    it.track?.warPositions?.let { warPositions ->
                        warPositions.forEach { position ->
                            trackPositions.add(MKWarPosition(position = position, mkcPlayer = players?.singleOrNull { it.mkcId ==  position.playerId }))
                        }
                        trackPositions.groupBy { it.mkcPlayer }.entries.forEach { entry ->
                            positions.add(Pair(entry.key, entry.value.map { pos -> pos.position.position.positionToPoints() }.sum()))
                        }
                        shocks.addAll(it.track.shocks?.takeIf { it.isNotEmpty() }.orEmpty())
                    }
                }
                val temp = positions.groupBy { it.first }.map { Pair(it.key, it.value.map { it.second }.sum()) }.sortedByDescending { it.second }
                val finalList = mutableListOf<CurrentPlayerModel>()
                temp.forEach { pair ->
                    val shockCount = shocks.filter { it.playerId == pair.first?.mkcId }.map { it.count }.sum()
                    finalList.add(CurrentPlayerModel(
                        player = pair.first,
                        score = pair.second,
                        tracksPlayed = it.filter { track -> track.hasPlayer(pair.first?.mkcId) }.size, shockCount = shockCount)
                    )
                }
                _sharedWarPlayers.emit(finalList)
            }
            .onEach { list ->
                var count = 0
                list.forEach { track ->
                    track.track?.shocks?.map { it.count }?.forEach {
                        count += it
                    }
                }
            }
            .launchIn(viewModelScope)
    }

}