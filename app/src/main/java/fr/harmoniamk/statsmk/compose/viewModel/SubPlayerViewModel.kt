package fr.harmoniamk.statsmk.compose.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.extension.isTrue
import fr.harmoniamk.statsmk.fragment.playerSelect.UserSelector
import fr.harmoniamk.statsmk.model.firebase.User
import fr.harmoniamk.statsmk.model.network.MKPlayer
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
    private val databaseRepository: DatabaseRepositoryInterface) : ViewModel() {

    private val _sharedPlayers = MutableStateFlow<List<UserSelector>>(listOf())
    private val _sharedAllies = MutableStateFlow<List<UserSelector>>(listOf())
    private val _sharedTitle = MutableStateFlow(R.string.joueur_sortant)
    private val _sharedBack = MutableSharedFlow<Unit>()
    private val _sharedPlayerSelected = MutableStateFlow<MKPlayer?>(null)

    val sharedPlayers = _sharedPlayers.asStateFlow()
    val sharedAllies = _sharedAllies.asStateFlow()
    val sharedPlayerSelected = _sharedPlayerSelected.asStateFlow()
    val sharedTitle = _sharedTitle.asStateFlow()
    val sharedBack = _sharedBack.asSharedFlow()

    private val playersList = mutableListOf<UserSelector>()
    private val allyList = mutableListOf<UserSelector>()
    private val currentPlayersList = mutableListOf<UserSelector>()
    private val fbUsers = mutableListOf<User>()

    var oldPlayer: MKPlayer? = null
    var newPlayer: MKPlayer? = null

    fun refresh() {
        databaseRepository.getRoster()
            .onEach {
                playersList.clear()
                allyList.clear()
                currentPlayersList.clear()

                playersList.addAll(it.filter { user -> user.currentWar == "-1" }.map { UserSelector(user = it, isSelected = false) })
                allyList.addAll(it.filter { user -> user.currentWar == "-1" &&  user.rosterId == "-1" }.map { UserSelector(user = it, isSelected = false) })
                currentPlayersList.addAll(it.filter { user -> user.currentWar == preferencesRepository.currentWar?.mid }.map { UserSelector(user = it, isSelected = false) } )

                _sharedPlayers.value = currentPlayersList
                _sharedTitle.value = R.string.joueur_sortant
            }
            .flatMapLatest { firebaseRepository.getUsers() }
            .onEach {
                fbUsers.clear()
                fbUsers.addAll(it)
            }
            .launchIn(viewModelScope)
    }

    fun onOldPlayerSelect(user : MKPlayer) {
        oldPlayer = user
        _sharedPlayers.value = playersList
        _sharedAllies.value = allyList
        _sharedTitle.value = R.string.joueur_entrant

        _sharedPlayerSelected.value = user
    }

    fun onNewPlayerSelect(user: MKPlayer) {
        newPlayer = user
        val playerListWithSelected = mutableListOf<UserSelector>()
        val allyListWithSelected = mutableListOf<UserSelector>()
        playersList.forEach {
            when (it.user?.mkcId == user.mkcId) {
                true -> playerListWithSelected.add(it.copy(isSelected = true))
                else -> playerListWithSelected.add(it)
            }
        }
        allyList.forEach {
            when (it.user?.mkcId == user.mkcId) {
                true -> allyListWithSelected.add(it.copy(isSelected = true))
                else -> allyListWithSelected.add(it)
            }
        }
        _sharedPlayers.value = playerListWithSelected
        _sharedAllies.value = allyListWithSelected
    }

    fun onSubClick() {
        oldPlayer?.apply { this.currentWar = "-1" }?.let { old ->
            val fbOld = fbUsers.singleOrNull { it.mkcId == old.mkcId }
            firebaseRepository.writeUser(User(old, fbOld?.mid, fbOld?.discordId))
                .flatMapLatest { databaseRepository.updateUser(old) }
                .mapNotNull { newPlayer?.apply { this.currentWar = preferencesRepository.currentWar?.mid.orEmpty()} }
                .map { new ->
                    val fbNew = fbUsers.singleOrNull { it.mkcId == new.mkcId }
                    firebaseRepository.writeUser(User(new, fbNew?.mid, fbNew?.discordId)).first()
                    databaseRepository.updateUser(new).first()
                }
                .onEach {
                    oldPlayer = null
                    newPlayer = null
                    playersList.clear()
                    allyList.clear()
                    currentPlayersList.clear()
                    fbUsers.clear()
                    _sharedPlayerSelected.value = null
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
            true -> {
                _sharedPlayers.value = playersList
                _sharedAllies.value = allyList
            }
            else -> {
                _sharedPlayers.value = playersList.filter { it.user?.name?.toLowerCase(Locale.ROOT)?.contains(searched.toLowerCase(Locale.ROOT)).isTrue }
                _sharedAllies.value = allyList.filter { it.user?.name?.toLowerCase(Locale.ROOT)?.contains(searched.toLowerCase(Locale.ROOT)).isTrue }
            }
        }
    }

}