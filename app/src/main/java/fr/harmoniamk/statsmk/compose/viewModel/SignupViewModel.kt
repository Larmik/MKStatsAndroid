package fr.harmoniamk.statsmk.compose.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.compose.ui.MKDialogState
import fr.harmoniamk.statsmk.enums.UserRole
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.extension.withName
import fr.harmoniamk.statsmk.model.firebase.AuthUserResponse
import fr.harmoniamk.statsmk.model.firebase.NewWarPositions
import fr.harmoniamk.statsmk.model.firebase.NewWarTrack
import fr.harmoniamk.statsmk.model.firebase.Shock
import fr.harmoniamk.statsmk.model.firebase.User
import fr.harmoniamk.statsmk.model.local.MKWar
import fr.harmoniamk.statsmk.repository.AuthenticationRepositoryInterface
import fr.harmoniamk.statsmk.repository.DatabaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import java.util.Locale
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class SignupViewModel @Inject constructor(
    private val firebaseRepository: FirebaseRepositoryInterface,
    private val preferencesRepository: PreferencesRepositoryInterface,
    private val authenticationRepository: AuthenticationRepositoryInterface,
    private val databaseRepository: DatabaseRepositoryInterface
) : ViewModel() {

    private val _sharedNext = MutableSharedFlow<Unit>()
    private val _sharedToast = MutableSharedFlow<String>()
    private val _sharedDialogValue = MutableStateFlow<MKDialogState?>(null)

    val sharedNext = _sharedNext.asSharedFlow()
    val sharedDialogValue = _sharedDialogValue.asStateFlow()
    val sharedToast = _sharedToast.asSharedFlow()

    fun onSignup(email: String, password: String, name: String, fc: String) {
        _sharedDialogValue.value = MKDialogState.Loading(R.string.creating_user)
        val createUser = authenticationRepository.createUser(email, password).shareIn(viewModelScope, SharingStarted.Lazily)
        createUser
            .mapNotNull { (it as? AuthUserResponse.Success)?.user?.uid }
            .mapNotNull { name }
            .flatMapLatest { authenticationRepository.updateProfile(it, "https://firebasestorage.googleapis.com/v0/b/stats-mk.appspot.com/o/mk_stats_logo.png?alt=media&token=930c6fdb-9e42-4b23-a9de-3c069d2f982b") }
            .mapNotNull { authenticationRepository.user }
            .map { fbUser ->
                _sharedDialogValue.value = MKDialogState.Loading(R.string.fetch_data)
                var finalUser: User? = null
                databaseRepository.getUsers().firstOrNull()?.singleOrNull { user ->
                    user.name?.toLowerCase(Locale.getDefault())
                        ?.trim() == name.toLowerCase(Locale.getDefault()).trim()
                }?.let {
                    finalUser = it
                    preferencesRepository.currentTeam = databaseRepository.getTeam(it.team.takeIf { it != "-1" }).firstOrNull()
                    firebaseRepository.deleteUser(it).first()
                    firebaseRepository.getNewWars(it.team ?: "-1").firstOrNull()?.let {
                        val hasPlayerWars = it.filter { MKWar(it).hasPlayer(finalUser?.mid) }
                        hasPlayerWars.forEach { war ->
                            val newWarTrack = mutableListOf<NewWarTrack>()
                            war.warTracks?.forEach { warTrack ->
                                val newPositions = mutableListOf<NewWarPositions>()
                                val newShocks = mutableListOf<Shock>()
                                warTrack.warPositions?.forEach { pos ->
                                    val newPosition = when (pos.playerId == finalUser?.mid) {
                                        true -> pos.apply { this.playerId = fbUser.uid }
                                        else -> pos
                                    }
                                    newPositions.add(newPosition)
                                }
                                warTrack.shocks?.forEach {
                                    val newShock = when (it.playerId == finalUser?.mid) {
                                        true -> it.apply { this.playerId = fbUser.uid }
                                        else -> it
                                    }
                                    newShocks.add(newShock)
                                }
                                newWarTrack.add(warTrack.apply {
                                    this.warPositions = newPositions
                                    this.shocks = newShocks
                                })
                            }
                            firebaseRepository.writeNewWar(war.apply { this.warTracks = newWarTrack }).first()
                            val mkWar = listOf(MKWar(war)).withName(databaseRepository).first()
                            mkWar.singleOrNull()?.let { databaseRepository.writeWar(it).first() }
                        }
                    }
                }
                User(
                    mid = fbUser.uid,
                    name = finalUser?.name ?: name,
                    role = UserRole.MEMBER.ordinal,
                    team = finalUser?.team ?: preferencesRepository.currentTeam?.mid ?: "-1",
                    currentWar = finalUser?.currentWar ?: preferencesRepository.currentWar?.mid ?: "-1",
                    picture = fbUser.photoUrl.toString(),
                    friendCode = fc
                )
            }
            .onEach {
                preferencesRepository.authEmail = email
                preferencesRepository.authPassword = password
                preferencesRepository.firstLaunch = false
                preferencesRepository.currentTeam = databaseRepository.getTeam(it.team.takeIf { it != "-1" }).firstOrNull()
            }
            .flatMapLatest { firebaseRepository.writeUser(it) }
            .onEach { _sharedDialogValue.value = null }
            .bind(_sharedNext, viewModelScope)

        createUser
            .mapNotNull { (it as? AuthUserResponse.Error)?.message }
            .onEach { _sharedDialogValue.value = null }
            .bind(_sharedToast, viewModelScope)
    }

}