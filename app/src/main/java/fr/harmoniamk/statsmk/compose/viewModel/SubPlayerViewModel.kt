package fr.harmoniamk.statsmk.compose.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.extension.isTrue
import fr.harmoniamk.statsmk.fragment.playerSelect.UserSelector
import fr.harmoniamk.statsmk.model.firebase.User
import fr.harmoniamk.statsmk.repository.DatabaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@HiltViewModel
class SubPlayerViewModel @Inject constructor(
    private val firebaseRepository: FirebaseRepositoryInterface,
    private val preferencesRepository: PreferencesRepositoryInterface,
    databaseRepository: DatabaseRepositoryInterface) : ViewModel() {

    private val _sharedPlayers = MutableStateFlow<List<UserSelector>>(listOf())
    private val _sharedTitle = MutableStateFlow(R.string.joueur_sortant)
    private val _sharedBack = MutableSharedFlow<Unit>()
    private val _sharedPlayerSelected = MutableStateFlow<User?>(null)

    val sharedPlayers = _sharedPlayers.asStateFlow()
    val sharedPlayerSelected = _sharedPlayerSelected.asStateFlow()
    val sharedTitle = _sharedTitle.asStateFlow()
    val sharedBack = _sharedBack.asSharedFlow()

    private val playersList = mutableListOf<UserSelector>()
    private val currentPlayersList = mutableListOf<UserSelector>()

    var oldPlayer: User? = null
    var newPlayer: User? = null

    init {

        databaseRepository.getUsers()
            .onEach {
                playersList.clear()
                currentPlayersList.clear()
                playersList.addAll(it.filter { user -> user.currentWar == "-1" }.map { UserSelector(user = it, isSelected = false) })
                currentPlayersList.addAll(it.filter { user -> user.currentWar == preferencesRepository.currentWar?.mid }.map { UserSelector(user = it, isSelected = false) } )
                _sharedPlayers.emit(currentPlayersList)
            }.launchIn(viewModelScope)
    }

    fun onOldPlayerSelect(user : User) {
        oldPlayer = user
        _sharedPlayers.value = playersList
        _sharedTitle.value = R.string.joueur_entrant
        _sharedPlayerSelected.value = user
    }

    fun onNewPlayerSelect(user: User) {
        newPlayer = user
        val playerListWithSelected = mutableListOf<UserSelector>()
        playersList.forEach {
            when (it.user?.mid == user.mid) {
                true -> playerListWithSelected.add(it.copy(isSelected = true))
                else -> playerListWithSelected.add(it)
            }
        }
        _sharedPlayers.value = playerListWithSelected
    }

    fun onSubClick() {
        oldPlayer?.copy(currentWar = "-1")?.let {
            firebaseRepository.writeUser(it)
                .mapNotNull { newPlayer?.copy(currentWar = preferencesRepository.currentWar?.mid) }
                .flatMapLatest { firebaseRepository.writeUser(it) }
                .onEach {
                    oldPlayer = null
                    newPlayer = null
                    playersList.clear()
                    currentPlayersList.clear()
                    _sharedPlayerSelected.value = null
                    _sharedPlayers.value = currentPlayersList
                    _sharedTitle.value = R.string.joueur_sortant
                }
                .bind(_sharedBack, viewModelScope)
        }

    }

    fun onBack() {
        when (oldPlayer) {
            null ->  viewModelScope.launch { _sharedBack.emit(Unit) }
            else -> {
                oldPlayer = null
                _sharedPlayerSelected.value = null
                _sharedPlayers.value = currentPlayersList
                _sharedTitle.value = R.string.joueur_sortant
            }
        }
    }

    fun onSearch(searched: String) {
        when (searched.isEmpty()) {
            true -> _sharedPlayers.value = playersList
            else -> _sharedPlayers.value = playersList.filter { it.user?.name?.toLowerCase(Locale.ROOT)?.contains(searched.toLowerCase(Locale.ROOT)).isTrue }
        }
    }

}