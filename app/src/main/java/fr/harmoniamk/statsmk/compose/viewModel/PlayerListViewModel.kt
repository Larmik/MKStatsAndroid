package fr.harmoniamk.statsmk.compose.viewModel

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.EntryPointAccessors
import fr.harmoniamk.statsmk.compose.ViewModelFactoryProvider
import fr.harmoniamk.statsmk.extension.isTrue
import fr.harmoniamk.statsmk.fragment.playerSelect.UserSelector
import fr.harmoniamk.statsmk.model.firebase.NewWar
import fr.harmoniamk.statsmk.model.firebase.User
import fr.harmoniamk.statsmk.repository.DatabaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class PlayerListViewModel @AssistedInject constructor(
    @Assisted private val id: String,
    private val preferencesRepository: PreferencesRepositoryInterface,
    private val databaseRepository: DatabaseRepositoryInterface,
    private val firebaseRepository: FirebaseRepositoryInterface
): ViewModel() {

    private val _sharedPlayers = MutableStateFlow<List<UserSelector>?>(null)
    private val _sharedAllies = MutableStateFlow<List<UserSelector>?>(null)
    private val _sharedWarName = MutableStateFlow<String?>(null)
    private val players = mutableListOf<UserSelector>()
    private val allies = mutableListOf<UserSelector>()

    val sharedPlayers = _sharedPlayers.asStateFlow()
    val sharedAllies = _sharedAllies.asStateFlow()
    val sharedWarName = _sharedWarName.asStateFlow()

    private val _sharedStarted = MutableSharedFlow<Unit>()
    private val _sharedAlreadyCreated = MutableSharedFlow<Unit>()

    val sharedStarted = _sharedStarted.asSharedFlow()
    val sharedAlreadyCreated = _sharedAlreadyCreated.asSharedFlow()

    val date = SimpleDateFormat("dd/MM/yyyy - HH'h'mm", Locale.FRANCE).format(Date())
    var official: Boolean = false

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
        fun viewModel(id: String?): PlayerListViewModel {
            val factory: Factory = EntryPointAccessors.fromApplication<ViewModelFactoryProvider>(context = LocalContext.current).playerListViewModel
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
        fun create(id: String?): PlayerListViewModel
    }

    fun selectUser(user: UserSelector) {
        val temp = mutableListOf<UserSelector>()
        _sharedPlayers.value?.forEach {
            when (it.user?.mkcId == user.user?.mkcId) {
                true -> temp.add(user)
                else -> temp.add(it)
            }
        }
        players.addAll(temp)
        _sharedPlayers.value = temp
    }

    fun selectAlly(user: UserSelector) {
        val temp = mutableListOf<UserSelector>()
        _sharedAllies.value?.forEach {
            when (it.user?.mkcId == user.user?.mkcId) {
                true -> temp.add(user)
                else -> temp.add(it)
            }
        }
        allies.addAll(temp)
        _sharedAllies.value = temp
    }

    fun toggleOfficial(official: Boolean) {
        this.official = official
    }

    fun createWar() {
        val war =  NewWar(
            mid = System.currentTimeMillis().toString(),
            teamHost = preferencesRepository.mkcTeam?.id,
            playerHostId = preferencesRepository.mkcPlayer?.id.toString(),
            teamOpponent = id,
            createdDate = date,
            isOfficial = official
        )
        firebaseRepository.getUsers()
            .onEach {
                players.filter { it.isSelected.isTrue }.mapNotNull { it.user }.forEach { user ->
                    val new = user.apply { this.currentWar = war.mid }
                    val fbUser = it.singleOrNull { it.mkcId == user.mkcId }
                    firebaseRepository.writeUser(User(new, fbUser?.mid, fbUser?.discordId)).first()
                    databaseRepository.writeUser(new).first()
                }
                allies.filter { it.isSelected.isTrue }.mapNotNull { it.user }.forEach { user ->
                    val new = user.apply { this.currentWar = war.mid }
                    val fbUser = it.singleOrNull { it.mkcId == user.mkcId }
                    firebaseRepository.writeUser(User(new, fbUser?.mid, fbUser?.discordId)).first()
                    databaseRepository.writeUser(new).first()
                }
                preferencesRepository.currentWar = war
                firebaseRepository.writeCurrentWar(war).first()
                _sharedStarted.emit(Unit)
            }.launchIn(viewModelScope)

    }

    init {
        databaseRepository.getRoster()
            .onEach {
                _sharedPlayers.value =  it.filter { user -> user.isAlly == 0  }
                    .sortedBy { it.name?.toLowerCase(Locale.ROOT) }
                    .map { UserSelector(it, false) }
                _sharedAllies.value =  it.filter { user -> user.isAlly == 1  }
                    .sortedBy { it.name?.toLowerCase(Locale.ROOT) }
                    .map { UserSelector(it, false) }
            }
            .launchIn(viewModelScope)

        databaseRepository.getNewTeam(id)
            .onEach {
                _sharedWarName.value = "${preferencesRepository.mkcTeam?.team_name} - ${it?.team_name}"
            }.launchIn(viewModelScope)
    }

}