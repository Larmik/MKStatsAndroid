package fr.harmoniamk.statsmk.fragment.addWar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.model.firebase.Team
import fr.harmoniamk.statsmk.model.firebase.User
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.extension.getCurrent
import fr.harmoniamk.statsmk.model.firebase.NewWar
import fr.harmoniamk.statsmk.model.local.MKWar
import fr.harmoniamk.statsmk.repository.AuthenticationRepository
import fr.harmoniamk.statsmk.repository.AuthenticationRepositoryInterface
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@HiltViewModel
class AddWarViewModel @Inject constructor(private val firebaseRepository: FirebaseRepositoryInterface, private val preferencesRepository: PreferencesRepositoryInterface, private val authenticationRepository: AuthenticationRepositoryInterface): ViewModel() {

    private val _sharedStarted = MutableSharedFlow<Unit>()
    private val _sharedTeamSelected = MutableSharedFlow<String?>()
    private val _sharedAlreadyCreated = MutableSharedFlow<Unit>()

    val sharedStarted = _sharedStarted.asSharedFlow()
    val sharedTeamSelected = _sharedTeamSelected.asSharedFlow()
    val sharedAlreadyCreated = _sharedAlreadyCreated.asSharedFlow()

    fun bind(onTeamClick: Flow<Team>, onCreateWar: Flow<Unit>, onUserSelected: Flow<List<User>>) {

        val date = SimpleDateFormat("dd/MM/yyyy - HH'h'mm", Locale.FRANCE).format(Date())
        val usersSelected = mutableListOf<User>()

        var chosenOpponent: Team? = null
        var war: NewWar?

        onTeamClick.onEach {
            chosenOpponent = it
            _sharedTeamSelected.emit( "${preferencesRepository.currentTeam?.shortName} - ${it.shortName}")
        }.launchIn(viewModelScope)

        val createWar = onCreateWar
            .flatMapLatest { firebaseRepository.getNewWars() }
            .map { it.map { w -> MKWar(w) }.getCurrent(preferencesRepository.currentTeam?.mid) }
            .shareIn(viewModelScope, SharingStarted.Eagerly, replay = 1)

        createWar
            .filter { it == null}
            .mapNotNull { chosenOpponent?.mid }
            .mapNotNull {
                war = NewWar(
                    mid = System.currentTimeMillis().toString(),
                    teamHost = preferencesRepository.currentTeam?.mid,
                    playerHostId = authenticationRepository.user?.uid,
                    teamOpponent = it,
                    createdDate = date
                )
                war
            }
            .onEach {
                usersSelected.forEach { user ->
                    val new = user.apply { this.currentWar = it.mid }
                    firebaseRepository.writeUser(new).first()
                }
                preferencesRepository.currentWar = it
            }
            .flatMapLatest { firebaseRepository.writeNewWar(it) }
            .bind(_sharedStarted, viewModelScope)

        createWar
            .filter { it != null }
            .map {  }
            .bind(_sharedAlreadyCreated, viewModelScope)

        onUserSelected.onEach {
            usersSelected.clear()
            usersSelected.addAll(it)
        }.launchIn(viewModelScope)
    }
}