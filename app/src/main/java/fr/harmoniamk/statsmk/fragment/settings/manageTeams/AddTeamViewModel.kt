package fr.harmoniamk.statsmk.fragment.settings.manageTeams

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.enums.UserRole
import fr.harmoniamk.statsmk.extension.isTrue
import fr.harmoniamk.statsmk.model.firebase.Team
import fr.harmoniamk.statsmk.repository.AuthenticationRepositoryInterface
import fr.harmoniamk.statsmk.repository.DatabaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@HiltViewModel
class AddTeamViewModel @Inject constructor(private val firebaseRepository: FirebaseRepositoryInterface, private val preferencesRepository: PreferencesRepositoryInterface, private val authenticationRepository: AuthenticationRepositoryInterface, private val databaseRepository: DatabaseRepositoryInterface) : ViewModel() {
    private val _sharedTeamAdded = MutableSharedFlow<Unit>()
    val sharedTeamAdded = _sharedTeamAdded.asSharedFlow()

    fun onCreateClick(name: String, shortName: String, teamWithLeader: Boolean) {
        val id = System.currentTimeMillis().toString()
        val addClick =  databaseRepository.getTeams().shareIn(viewModelScope, SharingStarted.Lazily)

        addClick
            .filterNot {
                it.map { team -> team.name?.lowercase() }.contains(name.lowercase())
                        || it.map { team -> team.shortName?.lowercase() }.contains(shortName.lowercase())
            }
            .filterNot { teamWithLeader }
            .map {
                val team = Team(
                    mid = id,
                    name = name,
                    shortName = shortName,
                    picture = "https://firebasestorage.googleapis.com/v0/b/stats-mk.appspot.com/o/mk_stats_logo.png?alt=media&token=930c6fdb-9e42-4b23-a9de-3c069d2f982b"
                )
                if (teamWithLeader) preferencesRepository.currentTeam = team
                team
            }
            .flatMapLatest { firebaseRepository.writeTeam(it) }
            .onEach { _sharedTeamAdded.emit(Unit) }
            .launchIn(viewModelScope)

        addClick
            .filter { teamWithLeader }
            .mapNotNull {
                it.singleOrNull { team ->
                    team.name?.lowercase()
                        ?.equals(name.lowercase()).isTrue
                            || team.shortName?.lowercase()
                        ?.equals(shortName.lowercase()).isTrue
                }?.takeIf { !it.hasLeader.isTrue }?.apply { this.hasLeader = true }
            }
            .onEach { preferencesRepository.currentTeam = it }
            .flatMapLatest { firebaseRepository.writeTeam(it) }
            .flatMapLatest { databaseRepository.getUser(authenticationRepository.user?.uid) }
            .filterNotNull()
            .flatMapLatest { firebaseRepository.writeUser(it.apply {
                this.role = UserRole.LEADER.ordinal
                this.team = preferencesRepository.currentTeam?.mid
            }) }
            .onEach { _sharedTeamAdded.emit(Unit) }
            .launchIn(viewModelScope)

        addClick
            .filterNot {
                it.map { team -> team.name?.lowercase() }.contains(name.lowercase())
                        || it.map { team -> team.shortName?.lowercase() }.contains(shortName.lowercase())
            }
            .filter { teamWithLeader }
            .map {
                val team = Team(
                    mid = id,
                    name = name,
                    shortName = shortName,
                    hasLeader = true,
                    picture = "https://firebasestorage.googleapis.com/v0/b/stats-mk.appspot.com/o/mk_stats_logo.png?alt=media&token=930c6fdb-9e42-4b23-a9de-3c069d2f982b"
                )
                preferencesRepository.currentTeam = team
                team
            }
            .flatMapLatest { firebaseRepository.writeTeam(it) }
            .flatMapLatest { databaseRepository.getUser(authenticationRepository.user?.uid) }
            .filterNotNull()
            .flatMapLatest { firebaseRepository.writeUser(it.apply {
                this.role = UserRole.LEADER.ordinal
                this.team = id
            }) }
            .onEach { _sharedTeamAdded.emit(Unit) }
            .launchIn(viewModelScope)

    }

}