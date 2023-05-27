package fr.harmoniamk.statsmk.fragment.addUser

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.enums.UserRole
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.extension.withName
import fr.harmoniamk.statsmk.model.firebase.*
import fr.harmoniamk.statsmk.model.local.MKWar
import fr.harmoniamk.statsmk.repository.AuthenticationRepositoryInterface
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
class AddUserViewModel @Inject constructor(
    private val firebaseRepository: FirebaseRepositoryInterface,
    private val preferencesRepository: PreferencesRepositoryInterface,
    private val authenticationRepository: AuthenticationRepositoryInterface,
    private val databaseRepository: DatabaseRepositoryInterface) : ViewModel() {

    private val _sharedNext = MutableSharedFlow<Unit>()
    private val _sharedToast = MutableSharedFlow<String>()
    private val _sharedGoToConnect = MutableSharedFlow<Unit>()
    private val _sharedButtonEnabled = MutableSharedFlow<Boolean>()
    private val _sharedLoading = MutableSharedFlow<Boolean>()
    private val _sharedLoadingMessage = MutableSharedFlow<String>()
    val sharedButtonEnabled = _sharedButtonEnabled.asSharedFlow()
    val sharedNext = _sharedNext.asSharedFlow()
    val sharedLoading = _sharedLoading.asSharedFlow()
    val sharedLoadingMessage = _sharedLoadingMessage.asSharedFlow()
    val sharedToast = _sharedToast.asSharedFlow()
    val sharedGoToConnect = _sharedGoToConnect.asSharedFlow()

    fun bind(onName: Flow<String>, onEmail: Flow<String>, onPassword: Flow<String>, onNext: Flow<Unit>, onSigninClick: Flow<Unit>) {

        var name: String? = null
        var code: String? = null
        var email: String? = null

        onName.onEach {
            name = it
            _sharedButtonEnabled.emit(!name.isNullOrEmpty() && !email.isNullOrEmpty() && !code.isNullOrEmpty())
        }.launchIn(viewModelScope)
        onPassword.onEach {
            code = it
            _sharedButtonEnabled.emit(!name.isNullOrEmpty() && !email.isNullOrEmpty() && !code.isNullOrEmpty())
        }.launchIn(viewModelScope)
        onEmail.onEach {
            email = it
            _sharedButtonEnabled.emit(!name.isNullOrEmpty() && !email.isNullOrEmpty() && !code.isNullOrEmpty())
        }.launchIn(viewModelScope)

        val createUser =
            onNext
                .filter { name != null && code != null && email != null}
                .onEach { _sharedLoading.emit(true) }
                .flatMapLatest { authenticationRepository.createUser(email!!, code!!) }
                .shareIn(viewModelScope, SharingStarted.Lazily)

        createUser
            .mapNotNull { (it as? AuthUserResponse.Success)?.user?.uid }
            .mapNotNull { name }
            .flatMapLatest { authenticationRepository.updateProfile(it, "https://firebasestorage.googleapis.com/v0/b/stats-mk.appspot.com/o/mk_stats_logo.png?alt=media&token=930c6fdb-9e42-4b23-a9de-3c069d2f982b") }
            .mapNotNull { authenticationRepository.user }
            .map { fbUser ->
                _sharedLoadingMessage.emit("Récupération des données...")
                var finalUser: User? = null
                databaseRepository.getUsers().firstOrNull()?.singleOrNull { user ->
                    user.name?.toLowerCase(Locale.getDefault())
                        ?.trim() == name?.toLowerCase(Locale.getDefault())?.trim()
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
                    picture = fbUser.photoUrl.toString()
                )
            }
            .onEach {
                preferencesRepository.authEmail = email
                preferencesRepository.authPassword = code
                preferencesRepository.firstLaunch = false
                preferencesRepository.currentTeam = databaseRepository.getTeam(it.team.takeIf { it != "-1" }).firstOrNull()
            }
            .flatMapLatest { firebaseRepository.writeUser(it) }
            .onEach { _sharedLoading.emit(false) }
            .bind(_sharedNext, viewModelScope)

        createUser
            .mapNotNull { (it as? AuthUserResponse.Error)?.message }
            .onEach { _sharedLoading.emit(false) }
            .bind(_sharedToast, viewModelScope)

        onSigninClick.bind(_sharedGoToConnect, viewModelScope)

    }
}