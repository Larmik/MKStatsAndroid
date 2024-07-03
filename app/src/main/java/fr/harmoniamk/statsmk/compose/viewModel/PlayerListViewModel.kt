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
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.compose.ViewModelFactoryProvider
import fr.harmoniamk.statsmk.compose.ui.MKDialogState
import fr.harmoniamk.statsmk.extension.isTrue
import fr.harmoniamk.statsmk.fragment.playerSelect.UserSelector
import fr.harmoniamk.statsmk.model.firebase.NewWar
import fr.harmoniamk.statsmk.model.firebase.User
import fr.harmoniamk.statsmk.model.local.TeamType
import fr.harmoniamk.statsmk.repository.DatabaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.zip
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class PlayerListViewModel @AssistedInject constructor(
    @Assisted("teamHostId") private val teamHostId: String,
    @Assisted("teamOpponentId") private val teamOpponentId: String,
    private val preferencesRepository: PreferencesRepositoryInterface,
    private val databaseRepository: DatabaseRepositoryInterface,
    private val firebaseRepository: FirebaseRepositoryInterface,
): ViewModel() {

    private val _sharedPlayers = MutableStateFlow<Map<String, List<UserSelector>>>(mapOf())
    private val _sharedDialogValue = MutableStateFlow<MKDialogState?>(null)
    private val _sharedWarName = MutableStateFlow<String?>(null)
    private val _sharedStarted = MutableSharedFlow<String>()

    val sharedPlayers = _sharedPlayers.asStateFlow()
    val sharedWarName = _sharedWarName.asStateFlow()
    val sharedDialogValue = _sharedDialogValue.asStateFlow()
    val sharedStarted = _sharedStarted.asSharedFlow()

    private val date = SimpleDateFormat("dd/MM/yyyy - HH'h'mm", Locale.FRANCE).format(Date())
    private var official: Boolean = false
    private val roster = mutableMapOf<String, List<UserSelector>>()



    companion object {
        @Suppress("UNCHECKED_CAST")
        fun provideFactory(
            assistedFactory: Factory,
            teamHostId: String?,
            teamOpponentId: String?
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return assistedFactory.create(teamHostId, teamOpponentId) as T
            }
        }

        @Composable
        fun viewModel(teamHostId: String?, teamOpponentId: String?): PlayerListViewModel {
            val factory: Factory = EntryPointAccessors.fromApplication<ViewModelFactoryProvider>(context = LocalContext.current).playerListViewModel
            return androidx.lifecycle.viewmodel.compose.viewModel(
                factory = provideFactory(
                    assistedFactory = factory,
                    teamHostId = teamHostId,
                    teamOpponentId = teamOpponentId
                )
            )
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(@Assisted("teamHostId") teamHostId: String?, @Assisted("teamOpponentId") teamOpponentId: String?): PlayerListViewModel
    }

    fun selectUser(user: UserSelector) {
        _sharedPlayers.value.forEach {
            val temp = mutableListOf<UserSelector>()
            it.value.forEach {
                when (it.user?.mid == user.user?.mid) {
                    true -> temp.add(user)
                    else -> temp.add(it)
                }
            }
            roster[it.key] = temp
        }
        _sharedPlayers.value = roster.toSortedMap(compareByDescending { it })
    }

    fun toggleOfficial(official: Boolean) {
        this.official = official
    }

    fun createWar() {
        _sharedDialogValue.value = MKDialogState.Loading(R.string.creating_war)
        val war =  NewWar(
            mid = System.currentTimeMillis().toString(),
            teamHost = teamHostId,
            playerHostId = preferencesRepository.mkcPlayer?.id.toString(),
            teamOpponent = teamOpponentId,
            createdDate = date,
            isOfficial = official
        )
        firebaseRepository.getUsers()
            .onEach { userList ->
                roster.values.forEach {
                    val playersSelected = it.filter { it.isSelected.isTrue }.mapNotNull { it.user }
                    playersSelected.forEach { user ->
                        val new = user.apply { this.currentWar = war.mid }
                        val fbUser = userList.singleOrNull { it.mkcId == user.mkcId }
                        firebaseRepository.writeUser(User(new, fbUser)).first()
                        databaseRepository.updateUser(new).first()
                    }
                }
                preferencesRepository.currentWar = war
                firebaseRepository.writeCurrentWar(war).first()
                delay(1000)
                _sharedDialogValue.value = null
                _sharedStarted.emit(war.teamHost.orEmpty())
            }.launchIn(viewModelScope)
    }

    init {
        databaseRepository.getPlayers()
            .filterNotNull()
            .onEach { list ->
                list.groupBy { it.rosterId }.forEach { rosterId, players ->
                    when (rosterId) {
                        "-1" -> roster["Allies"] = players.sortedBy { it.name.lowercase() }.map { UserSelector(it, false) }
                        else -> databaseRepository.getRosters()
                            .mapNotNull { it.singleOrNull { team -> team.teamId == rosterId } }
                            .onEach {
                                when (preferencesRepository.teamType) {
                                    is TeamType.SingleRoster ->  roster["Equipe"] = players.sortedBy { it.name.lowercase() }.map { UserSelector(it, false) }
                                    else -> roster[it.name] = players.sortedBy { it.name.lowercase() }.map { UserSelector(it, false) }
                                }
                                _sharedPlayers.value = roster.toSortedMap(compareByDescending { it })
                            }.launchIn(viewModelScope)
                    }
                }
            }
            .launchIn(viewModelScope)

        databaseRepository.getNewTeam(teamHostId)
            .zip( databaseRepository.getNewTeam(teamOpponentId)) { host, opponent ->
                _sharedWarName.value = "${host?.team_tag} - ${opponent?.team_tag}"
            }.launchIn(viewModelScope)
    }

}