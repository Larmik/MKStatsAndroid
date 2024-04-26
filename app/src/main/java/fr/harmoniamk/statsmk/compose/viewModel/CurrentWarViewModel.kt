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
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.compose.ViewModelFactoryProvider
import fr.harmoniamk.statsmk.compose.ui.MKBottomSheetState
import fr.harmoniamk.statsmk.compose.ui.MKDialogState
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.extension.positionToPoints
import fr.harmoniamk.statsmk.extension.sum
import fr.harmoniamk.statsmk.extension.withName
import fr.harmoniamk.statsmk.extension.withTeamName
import fr.harmoniamk.statsmk.model.firebase.Shock
import fr.harmoniamk.statsmk.model.firebase.User
import fr.harmoniamk.statsmk.model.local.CurrentPlayerModel
import fr.harmoniamk.statsmk.model.local.MKWar
import fr.harmoniamk.statsmk.model.local.MKWarPosition
import fr.harmoniamk.statsmk.model.local.MKWarTrack
import fr.harmoniamk.statsmk.model.network.MKPlayer
import fr.harmoniamk.statsmk.repository.DatabaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class CurrentWarViewModel @AssistedInject constructor(
    @Assisted("teamId") val teamId: String,
    private val firebaseRepository: FirebaseRepositoryInterface,
    private val preferencesRepository: PreferencesRepositoryInterface,
    private val databaseRepository: DatabaseRepositoryInterface
) : ViewModel() {

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun provideFactory(
            assistedFactory: Factory,
            teamId: String
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return assistedFactory.create(teamId) as T
            }
        }

        @Composable
        fun viewModel(teamId: String): CurrentWarViewModel {
            val factory: Factory = EntryPointAccessors.fromApplication<ViewModelFactoryProvider>(context = LocalContext.current).currentWarViewModel
            return androidx.lifecycle.viewmodel.compose.viewModel(
                factory = provideFactory(
                    assistedFactory = factory,
                    teamId = teamId
                )
            )
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(@Assisted("teamId") teamId: String): CurrentWarViewModel
    }

    private val _sharedCurrentWar = MutableStateFlow<MKWar?>(null)
    private val _sharedButtonVisible = MutableStateFlow(false)
    private val _sharedTracks = MutableStateFlow<List<MKWarTrack>?>(null)
    private val _sharedWarPlayers = MutableStateFlow<List<CurrentPlayerModel>?>(null)
    private val _sharedBottomSheetValue = MutableStateFlow<MKBottomSheetState?>(null)
    private val _sharedDialogValue = MutableStateFlow<MKDialogState?>(null)
    private val _sharedGoToWarResume = MutableSharedFlow<String>()
    private val _sharedBackToWars = MutableSharedFlow<Unit>()

    val sharedCurrentWar = _sharedCurrentWar.asStateFlow()
    val sharedButtonVisible = _sharedButtonVisible.asStateFlow()
    val sharedTracks = _sharedTracks.asStateFlow()
    val sharedWarPlayers = _sharedWarPlayers.asStateFlow()
    val sharedBottomSheetValue = _sharedBottomSheetValue.asStateFlow()
    val sharedDialogValue = _sharedDialogValue.asStateFlow()
    val sharedGoToWarResume = _sharedGoToWarResume.asSharedFlow()
    val sharedBackToWars = _sharedBackToWars.asSharedFlow()

    val users = mutableListOf<User>()
    val currentPlayers = mutableListOf<MKPlayer>()

    init {
        firebaseRepository.listenToCurrentWar(teamId)
            .flatMapLatest { it?.war.withName(databaseRepository) }
            .filterNotNull()
            .onEach {
                preferencesRepository.currentWar = it.war
                val penalties = it.war?.penalties?.withTeamName(databaseRepository)?.firstOrNull()
                val warWithPenas = it.war?.copy(penalties = penalties).withName(databaseRepository).firstOrNull()
                val warTracksList = it.war?.warTracks.orEmpty().map { MKWarTrack(it) }
                users.clear()
                currentPlayers.clear()
                users.addAll(firebaseRepository.getUsers().firstOrNull()?.filter { user -> user.currentWar == it.war?.mid }?.sortedBy { it.name?.lowercase() }.orEmpty())
                currentPlayers.addAll(databaseRepository.getRoster().firstOrNull().orEmpty())
                _sharedCurrentWar.emit(warWithPenas)
                _sharedTracks.emit(warWithPenas?.war?.warTracks.orEmpty().map { MKWarTrack(it) })
                _sharedWarPlayers.takeIf { warWithPenas?.war?.warTracks == null }?.emit(currentPlayers.filter { player -> users.singleOrNull { it.mkcId == player.mkcId }?.currentWar == it.war?.mid }.map { CurrentPlayerModel(it, 0, tracksPlayed = 0) })
                _sharedButtonVisible.emit(warWithPenas?.war?.playerHostId.equals(preferencesRepository.mkcPlayer?.id.toString()))
                _sharedTracks.emit(warTracksList)
                _sharedWarPlayers.takeIf { warTracksList.isNotEmpty() }?.emit(initPlayersList(warTracksList, it.war?.mid.orEmpty()))
            }.launchIn(viewModelScope)
    }

    fun onSubPlayer() {
        _sharedBottomSheetValue.value = MKBottomSheetState.SubPlayer()
    }

    fun onPenalty() {
        _sharedBottomSheetValue.value = MKBottomSheetState.Penalty()
    }

    fun onCancelClick() {
        _sharedDialogValue.value = MKDialogState.CancelWar(
            onWarCancelled = {
                _sharedDialogValue.value = MKDialogState.Loading(R.string.delete_war_in_progress)
                databaseRepository.getRoster()
                    .onEach {
                    it.filter { it.currentWar != "-1" }.forEach { user ->
                        val newUser = user.copy(currentWar = "-1")
                        val fbUser = users.singleOrNull { it.mkcId == user.mkcId }
                        firebaseRepository.writeUser(User(newUser, fbUser?.mid, fbUser?.discordId)).firstOrNull()
                        databaseRepository.updateUser(newUser).firstOrNull()
                    }
                    firebaseRepository.deleteCurrentWar(teamId).firstOrNull()
                    _sharedBackToWars.emit(Unit)
                }.launchIn(viewModelScope)
            },
            onDismiss = { _sharedDialogValue.value = null }
        )
    }

    fun onValidateWar() {
        preferencesRepository.currentWar?.let { war ->
            _sharedDialogValue.value = MKDialogState.ValidateWar(
                onValidateWar = {
                    viewModelScope.launch {
                        _sharedDialogValue.value = MKDialogState.Loading(R.string.creating_war)
                        firebaseRepository.writeNewWar(war).first()
                        firebaseRepository.deleteCurrentWar(teamId).first()
                        val mkWar = listOf(MKWar(war)).withName(databaseRepository).first()
                        mkWar.singleOrNull()?.let { databaseRepository.writeWar(it).first() }
                        currentPlayers
                            .filter { player -> users.singleOrNull { it.mkcId == player.mkcId }?.currentWar == war.mid }
                            .forEach {user ->
                                val new = user.apply { this.currentWar = "-1" }
                                val fbUser = firebaseRepository.getUsers().firstOrNull()?.singleOrNull { it.mkcId == user.mkcId }
                                firebaseRepository.writeUser(User(new, fbUser?.mid, fbUser?.discordId)).first()
                                databaseRepository.updateUser(new).first()
                            }
                        war.withName(databaseRepository)
                            .mapNotNull { it?.war?.mid }
                            .onEach { _sharedDialogValue.value = null }
                            .bind(_sharedGoToWarResume, viewModelScope)
                    }
                },
                onDismiss = { _sharedDialogValue.value = null }
            )
        }
    }

    fun dismissBottomSheet() {
        _sharedBottomSheetValue.value = null
    }

    private fun initPlayersList(trackList: List<MKWarTrack>, warId: String): List<CurrentPlayerModel> {
        val finalList = mutableListOf<CurrentPlayerModel>()
        val positions = mutableListOf<Pair<MKPlayer?, Int>>()
        val shocks = mutableStateListOf<Shock>()
        trackList.forEach {
            it.track?.warPositions?.let { warPositions ->
                val trackPositions = mutableListOf<MKWarPosition>()
                warPositions.forEach { position ->
                    viewModelScope.launch {
                        trackPositions.add(
                            MKWarPosition(
                                position = position,
                                mkcPlayer = currentPlayers.singleOrNull { it.mkcId == position.playerId }
                            )
                        )
                    }
                }
                trackPositions.groupBy { it.mkcPlayer }.entries.forEach { entry ->
                    positions.add(
                        Pair(
                            entry.key,
                            entry.value.map { pos -> pos.position.position.positionToPoints() }
                                .sum()
                        )
                    )
                }
                shocks.addAll(it.track.shocks?.takeIf { it.isNotEmpty() }.orEmpty())
            }
        }
        val temp = positions.groupBy { it.first }
            .map { Pair(it.key, it.value.map { it.second }.sum()) }
            .sortedByDescending { it.second }
        temp.forEach { pair ->
            val shockCount = shocks.filter { it.playerId == pair.first?.mkcId }.map { it.count }.sum()

            finalList.add(CurrentPlayerModel(
                player = pair.first,
                score = pair.second,
                tracksPlayed = trackList.filter { track -> track.hasPlayer(pair.first?.mkcId) }.size,
                shockCount = shockCount
            ))
        }
        currentPlayers.filter {
            it.currentWar == warId && !finalList.map { it.player?.mkcId }.contains(it.mkcId)
        }.forEach { finalList.add(CurrentPlayerModel(player = it, score = 0, tracksPlayed = 0)) }
        return finalList
    }

}