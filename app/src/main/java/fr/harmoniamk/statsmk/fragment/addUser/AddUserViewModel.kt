package fr.harmoniamk.statsmk.fragment.addUser

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.enums.UserRole
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.model.firebase.AuthUserResponse
import fr.harmoniamk.statsmk.model.firebase.User
import fr.harmoniamk.statsmk.repository.AuthenticationRepositoryInterface
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@HiltViewModel
class AddUserViewModel @Inject constructor(private val firebaseRepository: FirebaseRepositoryInterface, private val preferencesRepository: PreferencesRepositoryInterface, private val authenticationRepository: AuthenticationRepositoryInterface) : ViewModel() {

    private val _sharedNext = MutableSharedFlow<Unit>()
    private val _sharedToast = MutableSharedFlow<String>()
    private val _sharedGoToConnect = MutableSharedFlow<Unit>()
    val sharedNext = _sharedNext.asSharedFlow()
    val sharedToast = _sharedToast.asSharedFlow()
    val sharedGoToConnect = _sharedGoToConnect.asSharedFlow()

    fun bind(onName: Flow<String>, onEmail: Flow<String>, onPassword: Flow<String>, onNext: Flow<Unit>, onSigninClick: Flow<Unit>) {

        var name: String? = null
        var code: String? = null
        var email: String? = null

        onName.onEach { name = it }.launchIn(viewModelScope)
        onPassword.onEach { code = it }.launchIn(viewModelScope)
        onEmail.onEach { email = it }.launchIn(viewModelScope)

        val createUser =
            onNext
                .filter { name != null && code != null && email != null}
                .flatMapLatest { authenticationRepository.createUser(email!!, code!!) }
                .shareIn(viewModelScope, SharingStarted.Lazily)

        createUser
            .mapNotNull { (it as? AuthUserResponse.Success)?.user?.uid }
            .mapNotNull { name }
            .flatMapLatest { authenticationRepository.updateProfile(it, "https://firebasestorage.googleapis.com/v0/b/stats-mk-debug.appspot.com/o/hr_logo.png?alt=media&token=6f4452bf-7028-4203-8d77-0c3eb0d8cd48") }
            .mapNotNull { authenticationRepository.user }
            .flatMapLatest {
                val user = User(
                    mid = it.uid,
                    name = name,
                    role = UserRole.MEMBER.ordinal,
                    team = preferencesRepository.currentTeam?.mid ?: "-1",
                    currentWar = preferencesRepository.currentWar?.mid ?: "-1",
                    picture = it.photoUrl.toString()
                )
                preferencesRepository.authEmail = email
                preferencesRepository.authPassword = code
                preferencesRepository.firstLaunch = false
                firebaseRepository.writeUser(user)
            }
            .bind(_sharedNext, viewModelScope)

        createUser
            .mapNotNull { (it as? AuthUserResponse.Error)?.message }
            .bind(_sharedToast, viewModelScope)

        onSigninClick.bind(_sharedGoToConnect, viewModelScope)

    }
}