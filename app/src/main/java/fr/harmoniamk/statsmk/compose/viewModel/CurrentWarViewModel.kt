package fr.harmoniamk.statsmk.compose.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.compose.ui.MKBottomSheetState
import fr.harmoniamk.statsmk.compose.ui.MKDialogState
import fr.harmoniamk.statsmk.enums.UserRole
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.extension.isTrue
import fr.harmoniamk.statsmk.extension.positionToPoints
import fr.harmoniamk.statsmk.extension.sum
import fr.harmoniamk.statsmk.extension.withName
import fr.harmoniamk.statsmk.extension.withTeamName
import fr.harmoniamk.statsmk.fragment.currentWar.CurrentPlayerModel
import fr.harmoniamk.statsmk.model.firebase.NewWar
import fr.harmoniamk.statsmk.model.firebase.Penalty
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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flattenMerge
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
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
    private val _sharedButtonVisible = MutableStateFlow<Boolean>(false)
    private val _sharedTracks = MutableStateFlow<List<MKWarTrack>?>(null)
    private val _sharedWarPlayers = MutableStateFlow<List<CurrentPlayerModel>?>(null)
    private val _sharedPenalties = MutableStateFlow<List<Penalty>?>(null)
    private val _sharedShockCount = MutableStateFlow<String?>(null)
    private val _sharedBottomSheetValue = MutableStateFlow<MKBottomSheetState?>(null)
    private val _sharedDialogValue = MutableStateFlow<MKDialogState?>(null)



    val sharedCurrentWar = _sharedCurrentWar.asStateFlow()
    val sharedButtonVisible = _sharedButtonVisible.asStateFlow()
    val sharedTracks = _sharedTracks.asStateFlow()
    val sharedWarPlayers = _sharedWarPlayers.asStateFlow()
    val sharedPenalties = _sharedPenalties.asStateFlow()
    val sharedShockCount = _sharedShockCount.asStateFlow()
    val sharedBottomSheetValue = _sharedBottomSheetValue.asStateFlow()
    val sharedDialogValue = _sharedDialogValue.asStateFlow()



    private val  _sharedQuit = MutableSharedFlow<Unit>()
    private val  _sharedBackToWars = MutableSharedFlow<Unit>()
    private val _sharedSelectTrack = MutableSharedFlow<Unit>()
    private val _sharedTrackClick = MutableSharedFlow<Int>()
    private val _sharedPopupShowing = MutableSharedFlow<Boolean>()
    private val _sharedAddPenalty = MutableSharedFlow<NewWar>()
    private val _sharedSubPlayer = MutableSharedFlow<Unit>()
    private val _sharedLoading = MutableSharedFlow<Boolean>()

    val sharedBackToWars = _sharedBackToWars.asSharedFlow()
    val sharedTrackClick = _sharedTrackClick.asSharedFlow()

    init {
        val currentWar = firebaseRepository.listenToCurrentWar()
            .shareIn(viewModelScope, SharingStarted.Lazily)

        val warFlow = currentWar
            .filterNotNull()
            .flatMapLatest { listOf(it).withName(databaseRepository) }
            .mapNotNull { it.singleOrNull() }


        warFlow.onEach {
            val penalties = it.war?.penalties?.withTeamName(databaseRepository)?.firstOrNull()
            val warWithPenas = it.war?.copy(penalties = penalties).withName(databaseRepository).firstOrNull()
            val isAdmin = (authenticationRepository.userRole.firstOrNull() ?: 0) >= UserRole.ADMIN.ordinal
            preferencesRepository.currentWar = warWithPenas?.war
            _sharedCurrentWar.emit(warWithPenas)
            _sharedButtonVisible.emit(isAdmin.isTrue)
            _sharedTracks.emit(warWithPenas?.war?.warTracks.orEmpty().map { MKWarTrack(it) })
            val players = databaseRepository.getUsers().first().filter { it.currentWar == preferencesRepository.currentWar?.mid }
                .sortedBy { it.name?.toLowerCase(Locale.ROOT) }
            _sharedWarPlayers.takeIf { warWithPenas?.war?.warTracks == null }?.emit(players.map { CurrentPlayerModel(it, 0) })

        }
            .mapNotNull { it.war?.penalties }
            .flatMapLatest { it.withTeamName(databaseRepository) }
            .bind(_sharedPenalties, viewModelScope)

       warFlow
            .mapNotNull { it.war?.warTracks?.map { MKWarTrack(it) } }
            .map {
                var shockCount = 0
                val positions = mutableListOf<Pair<User?, Int>>()
                val players = databaseRepository.getUsers().firstOrNull()
                _sharedTracks.emit(it)
                it.forEach {
                    it.track?.warPositions?.let { warPositions ->
                        val trackPositions = mutableListOf<MKWarPosition>()
                        warPositions.forEach { position ->
                            trackPositions.add(MKWarPosition(position, players?.singleOrNull { it.mid ==  position.playerId }))
                        }
                        trackPositions.groupBy { it.player }.entries.forEach { entry ->
                            positions.add(Pair(entry.key, entry.value.map { pos -> pos.position.position.positionToPoints() }.sum()))
                        }

                    }
                    it.track?.shocks?.map { it.count }?.forEach {
                        shockCount += it
                    }
                }
                val temp = positions.groupBy { it.first }.map { Pair(it.key, it.value.map { it.second }.sum()) }.sortedByDescending { it.second }
                val finalList = mutableListOf<CurrentPlayerModel>()
                temp.forEach { pair ->
                    val isOld = pair.first?.currentWar == "-1"
                    val isNew = it.size > it.filter { track -> track.hasPlayer(pair.first?.mid) }.size && pair.first?.currentWar == preferencesRepository.currentWar?.mid
                    finalList.add(CurrentPlayerModel(pair.first, pair.second, isOld, isNew))
                }
                databaseRepository.getUsers().firstOrNull()
                    ?.filter { it.currentWar == preferencesRepository.currentWar?.mid && !finalList.map { it.player?.mid }.contains(it.mid)}
                    ?.forEach { finalList.add(CurrentPlayerModel(it, 0, isNew = true)) }
                _sharedWarPlayers.emit(finalList)
                shockCount.takeIf { it > 0 }?.let {
                    _sharedShockCount.emit("x$it")
                }
            }
            .launchIn(viewModelScope)


    }

    fun bind(onBack: Flow<Unit>, onNextTrack: Flow<Unit>, onTrackClick: Flow<Int>, onPopup: Flow<Unit>, onPenalty: Flow<Unit>, onSub: Flow<Unit>, onSubDismiss: Flow<Unit>) {



        flowOf(flowOf(Unit), onSubDismiss)
            .flattenMerge()
           // .flatMapLatest { trackPlayersFlow }
            .launchIn(viewModelScope)

        onBack.bind(_sharedQuit, viewModelScope)
        onNextTrack.bind(_sharedSelectTrack, viewModelScope)
        onTrackClick.bind(_sharedTrackClick, viewModelScope)
        onPopup
            .onEach { _sharedPopupShowing.emit(true) }
            .launchIn(viewModelScope)


        onPenalty
            .mapNotNull { preferencesRepository.currentWar }
            .bind(_sharedAddPenalty, viewModelScope)
        onSub.bind(_sharedSubPlayer, viewModelScope)


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
                databaseRepository.getUsers()
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
            onDismiss = {
                _sharedDialogValue.value = null
            }
        )
    }

    fun dismissBottomSheet() {
        _sharedBottomSheetValue.value = null
    }
}