package fr.harmoniamk.statsmk.fragment.settings.managePlayers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.model.firebase.User
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.repository.DatabaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import java.util.*
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@HiltViewModel
class AddPlayersViewModel @Inject constructor(private val firebaseRepository: FirebaseRepositoryInterface, private val preferencesRepository: PreferencesRepositoryInterface, private val databaseRepository: DatabaseRepositoryInterface) : ViewModel() {

    private val _sharedUserAdded = MutableSharedFlow<Unit>()
    private val _sharedToast = MutableSharedFlow<String>()
    private val _sharedButtonEnabled = MutableSharedFlow<Boolean>()
    val sharedButtonEnabled = _sharedButtonEnabled.asSharedFlow()
    val sharedToast = _sharedToast.asSharedFlow()
    val sharedUserAdded = _sharedUserAdded.asSharedFlow()

    fun bind(onName: Flow<String>, onPlayerAdded: Flow<Unit>, onTeamChecked: Flow<Boolean>) {
        var name: String? = null
        var teamChecked = false
        onName.onEach {
            name = it
            _sharedButtonEnabled.emit(!name.isNullOrEmpty())
        }.launchIn(viewModelScope)
        val playerAdded = onPlayerAdded
            .flatMapLatest { databaseRepository.getUsers() }
            .shareIn(viewModelScope, SharingStarted.Lazily)

        onTeamChecked
            .onEach { teamChecked = it }
            .launchIn(viewModelScope)

        playerAdded
            .filterNot { it.map { player -> player.name?.toLowerCase(Locale.getDefault()) }.contains(name?.toLowerCase(Locale.getDefault())) }
            .mapNotNull { name }
            .map {
                User(
                    mid = System.currentTimeMillis().toString(),
                    name = it,
                    team = if (teamChecked) preferencesRepository.currentTeam?.mid else "-1",
                    picture = "https://firebasestorage.googleapis.com/v0/b/stats-mk.appspot.com/o/mk_stats_logo.png?alt=media&token=930c6fdb-9e42-4b23-a9de-3c069d2f982b"
                )
            }
            .flatMapLatest { firebaseRepository.writeUser(it) }
            .bind(_sharedUserAdded, viewModelScope)

        playerAdded
            .filter { it.map { player -> player.name?.toLowerCase(Locale.getDefault()) }.contains(name?.toLowerCase(Locale.getDefault())) }
            .onEach { _sharedToast.emit("Ce joueur existe déjà") }
            .launchIn(viewModelScope)
    }

}