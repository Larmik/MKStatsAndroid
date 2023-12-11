package fr.harmoniamk.statsmk.compose.viewModel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.model.local.CurrentPlayerModel
import fr.harmoniamk.statsmk.compose.ui.MKBottomSheetState
import fr.harmoniamk.statsmk.compose.ui.MKDialogState
import fr.harmoniamk.statsmk.enums.UserRole
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.extension.isTrue
import fr.harmoniamk.statsmk.extension.positionToPoints
import fr.harmoniamk.statsmk.extension.sum
import fr.harmoniamk.statsmk.extension.withName
import fr.harmoniamk.statsmk.extension.withTeamName
import fr.harmoniamk.statsmk.model.firebase.Shock
import fr.harmoniamk.statsmk.model.firebase.User
import fr.harmoniamk.statsmk.model.local.MKWar
import fr.harmoniamk.statsmk.model.local.MKWarPosition
import fr.harmoniamk.statsmk.model.local.MKWarTrack
import fr.harmoniamk.statsmk.repository.AuthenticationRepositoryInterface
import fr.harmoniamk.statsmk.repository.DatabaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flattenMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class CurrentWarViewModel @Inject constructor(
    private val firebaseRepository: FirebaseRepositoryInterface,
    private val preferencesRepository: PreferencesRepositoryInterface,
    private val authenticationRepository: AuthenticationRepositoryInterface,
    private val databaseRepository: DatabaseRepositoryInterface
) : ViewModel() {

    private val _sharedCurrentWar = MutableStateFlow<MKWar?>(null)
    private val _sharedButtonVisible = MutableStateFlow(false)
    private val _sharedTracks = MutableStateFlow<List<MKWarTrack>?>(null)
    private val _sharedWarPlayers = MutableStateFlow<List<CurrentPlayerModel>?>(null)
    private val _sharedBottomSheetValue = MutableStateFlow<MKBottomSheetState?>(null)
    private val _sharedDialogValue = MutableStateFlow<MKDialogState?>(null)
    private val _sharedGoToWarResume = MutableSharedFlow<String>()
    private val _sharedBackToWars = MutableSharedFlow<Unit>()
    private val _sharedTrackClick = MutableSharedFlow<Int>()

    val sharedCurrentWar = _sharedCurrentWar.asStateFlow()
    val sharedButtonVisible = _sharedButtonVisible.asStateFlow()
    val sharedTracks = _sharedTracks.asStateFlow()
    val sharedWarPlayers = _sharedWarPlayers.asStateFlow()
    val sharedBottomSheetValue = _sharedBottomSheetValue.asStateFlow()
    val sharedDialogValue = _sharedDialogValue.asStateFlow()
    val sharedGoToWarResume = _sharedGoToWarResume.asSharedFlow()
    val sharedBackToWars = _sharedBackToWars.asSharedFlow()
    val sharedTrackClick = _sharedTrackClick.asSharedFlow()

    init {
        flowOf(firebaseRepository.getCurrentWar(preferencesRepository.mkcTeam?.id.orEmpty()), firebaseRepository.listenToCurrentWar())
            .flattenMerge()
            .flatMapLatest { it?.war.withName(databaseRepository) }
            .filterNotNull()
            .onEach {
                val penalties = it.war?.penalties?.withTeamName(databaseRepository)?.firstOrNull()
                val warWithPenas =
                    it.war?.copy(penalties = penalties).withName(databaseRepository).firstOrNull()
                val isAdmin =
                    (authenticationRepository.userRole.firstOrNull() ?: 0) >= UserRole.ADMIN.ordinal
                val players = firebaseRepository.getUsers().first()
                    .filter { it.currentWar == preferencesRepository.currentWar?.mid }
                    .sortedBy { it.name?.toLowerCase(Locale.ROOT) }

                preferencesRepository.currentWar = warWithPenas?.war
                _sharedCurrentWar.emit(warWithPenas)
                _sharedTracks.emit(warWithPenas?.war?.warTracks.orEmpty().map { MKWarTrack(it) })
                _sharedWarPlayers.takeIf { warWithPenas?.war?.warTracks == null }
                    ?.emit(players.map { CurrentPlayerModel(it, 0) })
                _sharedButtonVisible.emit(isAdmin.isTrue)
            }
            .mapNotNull { it.war?.warTracks?.map { MKWarTrack(it) } }
            .onEach { _sharedTracks.emit(it) }
            .flatMapLatest { initPlayersList(it) }
            .bind(_sharedWarPlayers, viewModelScope)
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
                firebaseRepository.getUsers()
                    .map { list -> list.filter { user -> user.currentWar == preferencesRepository.currentWar?.mid } }
                    .onEach { list ->
                        list.forEach {
                            val newUser = it.apply { this.currentWar = "-1" }
                            firebaseRepository.writeUser(newUser).first()
                        }
                    }
                    .flatMapLatest { firebaseRepository.deleteCurrentWar() }
                    .bind(_sharedBackToWars, viewModelScope)
            },
            onDismiss = { _sharedDialogValue.value = null }
        )
    }

    fun onValidateWar() {
        _sharedDialogValue.value = MKDialogState.ValidateWar(
            onValidateWar = {
                viewModelScope.launch {
                    _sharedDialogValue.value = MKDialogState.Loading(R.string.creating_war)
                    preferencesRepository.currentWar?.let { war ->
                        firebaseRepository.writeNewWar(war).first()
                        firebaseRepository.deleteCurrentWar().first()
                        val mkWar = listOf(MKWar(war)).withName(databaseRepository).first()
                        mkWar.singleOrNull()?.let { databaseRepository.writeWar(it).first() }
                        databaseRepository.getUsers().first().filter { it.currentWar == war.mid }
                            .forEach {
                                val new = it.apply { this.currentWar = "-1" }
                                firebaseRepository.writeUser(new).first()
                            }
                        war.withName(databaseRepository)
                            .mapNotNull { it?.war?.mid }
                            .onEach { _sharedDialogValue.value = null }
                            .bind(_sharedGoToWarResume, viewModelScope)
                    }
                }
            },
            onDismiss = { _sharedDialogValue.value = null }
        )
    }

    fun dismissBottomSheet() {
        _sharedBottomSheetValue.value = null
    }

    private fun initPlayersList(trackList: List<MKWarTrack>): Flow<List<CurrentPlayerModel>> =
        firebaseRepository.getUsers()
            .map { players ->
                val finalList = mutableListOf<CurrentPlayerModel>()
                val positions = mutableListOf<Pair<User?, Int>>()
                val shocks = mutableStateListOf<Shock>()
                trackList.forEach {
                    it.track?.warPositions?.let { warPositions ->
                        val trackPositions = mutableListOf<MKWarPosition>()
                        warPositions.forEach { position ->
                            trackPositions.add(
                                MKWarPosition(
                                    position = position,
                                    player = players.singleOrNull { it.mkcId == position.playerId })
                            )
                        }
                        trackPositions.groupBy { it.player }.entries.forEach { entry ->
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
                    val isOld = pair.first?.currentWar == "-1"
                    val isNew = trackList.size > trackList.filter { track -> track.hasPlayer(pair.first?.mkcId) }.size && pair.first?.currentWar == preferencesRepository.currentWar?.mid
                    finalList.add(CurrentPlayerModel(pair.first, pair.second, isOld, isNew, shockCount))
                }
                players.filter {
                    it.currentWar == preferencesRepository.currentWar?.mid && !finalList.map { it.player?.mkcId }.contains(it.mkcId)
                }.forEach { finalList.add(CurrentPlayerModel(it, 0, isNew = true)) }
                finalList
            }

}