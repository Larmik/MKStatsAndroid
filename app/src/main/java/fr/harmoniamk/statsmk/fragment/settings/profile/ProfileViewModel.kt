package fr.harmoniamk.statsmk.fragment.settings.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.enums.UserRole
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.model.firebase.PictureResponse
import fr.harmoniamk.statsmk.model.firebase.ResetPasswordResponse
import fr.harmoniamk.statsmk.model.firebase.UploadPictureResponse
import fr.harmoniamk.statsmk.repository.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@HiltViewModel
class ProfileViewModel @Inject constructor(private val authenticationRepository: AuthenticationRepositoryInterface, private val storageRepository: StorageRepository, private val preferencesRepository: PreferencesRepositoryInterface, private val firebaseRepository: FirebaseRepositoryInterface) : ViewModel() {

    private val _sharedProfile = MutableSharedFlow<FirebaseUser>()
    private val _sharedEditPicture = MutableSharedFlow<Unit>()
    private val _sharedDisconnectPopup = MutableSharedFlow<Boolean>()
    private val _sharedPictureLoaded = MutableSharedFlow<String?>()
    private val _showResetPopup = MutableSharedFlow<String>()
    private val _sharedDisconnect = MutableSharedFlow<Unit>()
    private val _sharedTeam = MutableSharedFlow<String?>()
    private val _sharedNewName = MutableSharedFlow<String>()
    private val _sharedEditName = MutableSharedFlow<Unit>()
    private val _sharedEditEmail = MutableSharedFlow<Unit>()
    private val _sharedRole = MutableSharedFlow<String?>()
    private val _sharedLeavePopup = MutableSharedFlow<Pair<String, String?>>()
    private val _sharedCancelLeavePopup = MutableSharedFlow<Unit>()
    private val _sharedTeamLeft = MutableSharedFlow<Unit>()

    val sharedProfile =_sharedProfile.asSharedFlow()
    val sharedEditPicture =_sharedEditPicture.asSharedFlow()
    val sharedPictureLoaded =_sharedPictureLoaded.asSharedFlow()
    val showResetPopup =_showResetPopup.asSharedFlow()
    val sharedDisconnect =_sharedDisconnect.asSharedFlow()
    val sharedDisconnectPopup = _sharedDisconnectPopup.asSharedFlow()
    val sharedTeam = _sharedTeam.asSharedFlow()
    val sharedNewName = _sharedNewName.asSharedFlow()
    val sharedRole = _sharedRole.asSharedFlow()
    val sharedEditName = _sharedEditName.asSharedFlow()
    val sharedEditEmail = _sharedEditEmail.asSharedFlow()
    val sharedLeavePopup = _sharedLeavePopup.asSharedFlow()
    val sharedCancelLeavePopup = _sharedCancelLeavePopup.asSharedFlow()
    val sharedTeamLeft = _sharedTeamLeft.asSharedFlow()

    fun bind(onPictureClick: Flow<Unit>, onPictureEdited: Flow<String>, onChangePasswordClick: Flow<Unit>, onLogout: Flow<Unit>, onPopup: Flow<Boolean>, onChangeNameClick: Flow<Unit>, onChangeEmailClick: Flow<Unit>, onLeaveTeam: Flow<Unit>) {
        var url: String? = null

        storageRepository.getPicture(authenticationRepository.user?.uid)
            .mapNotNull { authenticationRepository.user }
            .onEach { _sharedProfile.emit(it) }
            .flatMapLatest { firebaseRepository.getUser(it.uid) }
            .mapNotNull { it?.role  }
            .onEach {
                _sharedRole.emit(when (it){
                    UserRole.MEMBER.ordinal -> "Membre"
                    UserRole.ADMIN.ordinal -> "Admin"
                    UserRole.LEADER.ordinal -> "Leader"
                    UserRole.GOD.ordinal -> "Dieu"
                    else -> null
                } )
                _sharedTeam.emit(preferencesRepository.currentTeam?.name)
            }
            .launchIn(viewModelScope)

        onPopup.bind(_sharedDisconnectPopup, viewModelScope)

        onPictureEdited
            .flatMapLatest { storageRepository.uploadPicture(authenticationRepository.user?.uid, Uri.parse(it)) }
            .filter { it is UploadPictureResponse.Success }
            .mapNotNull { authenticationRepository.user?.uid }
            .flatMapLatest { storageRepository.getPicture(it) }
            .mapNotNull { url = (it as? PictureResponse.Success)?.url; url }
            .flatMapLatest { authenticationRepository.updateProfile(authenticationRepository.user?.displayName.toString(), it) }
            .onEach { _sharedPictureLoaded.emit(url) }
            .launchIn(viewModelScope)

        onChangePasswordClick
            .flatMapLatest { authenticationRepository.resetPassword(authenticationRepository.user?.email.toString()) }
            .map {
                when (it) {
                    is ResetPasswordResponse.Success -> "Un email contenant un lien a été envoyé. Une fois changé, le mot de passe prendra effet lors de la prochaine reconnexion."
                    is ResetPasswordResponse.Error -> "Une erreur est survenue durant l'envoi du mail. Vérifier l'adresse mail et la connexion ou réessayer plus tard"
                }
            }.bind(_showResetPopup, viewModelScope)

        onLogout
            .onEach {
                authenticationRepository.signOut()
                preferencesRepository.currentTeam = null
                preferencesRepository.authEmail = null
                preferencesRepository.authPassword = null
            }.bind(_sharedDisconnect, viewModelScope)

        onPictureClick.bind(_sharedEditPicture, viewModelScope)

        onChangeNameClick.bind(_sharedEditName, viewModelScope)
        onChangeEmailClick.bind(_sharedEditEmail, viewModelScope)

        onLeaveTeam
            .flatMapLatest { firebaseRepository.getUsers() }
            .map { it.filter { user -> user.team == preferencesRepository.currentTeam?.mid && (user.role ?: 0) >= UserRole.LEADER.ordinal }.size > 1 }
            .onEach {
                val role = authenticationRepository.userRole.firstOrNull() ?: 0
                when ((it && role >= UserRole.LEADER.ordinal) || role < UserRole.LEADER.ordinal) {
                    true -> _sharedLeavePopup.emit(Pair("Êtres-vous sûr de voiloir quitter l'équipe ?", "Confirmer"))
                    else -> _sharedLeavePopup.emit(Pair("Vous ne pouvez pas quitter une équipe dont vous êtes le seul leader. \n \n Vous devez d'abord nommer un autre leader avant de renouveler l'opération.", null))
                }
            }.launchIn(viewModelScope)
    }

    fun bindDialog(isEmail: Boolean, onTextChange: Flow<String>, onValidate: Flow<Unit>, onDismiss: Flow<Unit>) {
        when (isEmail) {
            true -> {
                var email = authenticationRepository.user?.email.toString()
                onTextChange.onEach { email = it }.launchIn(viewModelScope)
                onValidate
                    .flatMapLatest { authenticationRepository.updateEmail(email) }
                    .onEach {
                        preferencesRepository.authEmail = email
                        _sharedNewName.emit(email)
                    }
                    .launchIn(viewModelScope)
                onDismiss.mapNotNull { email }.bind(_sharedNewName, viewModelScope)
            }
            else -> {
                var name = authenticationRepository.user?.displayName.toString()
                onTextChange.onEach { name = it }.launchIn(viewModelScope)
                onValidate
                    .flatMapLatest { authenticationRepository.updateProfile(name, null) }
                    .flatMapLatest { firebaseRepository.getUser(authenticationRepository.user?.uid) }
                    .mapNotNull { it?.copy(name = name) }
                    .flatMapLatest { firebaseRepository.writeUser(it) }
                    .onEach { _sharedNewName.emit(name) }
                    .launchIn(viewModelScope)
                onDismiss.mapNotNull { name }.bind(_sharedNewName, viewModelScope)

            }
        }


    }

    fun bindLeavePopup(onCancel: Flow<Unit>, onLeave: Flow<Unit>) {
        onCancel.bind(_sharedCancelLeavePopup, viewModelScope)
        onLeave
            .flatMapLatest { firebaseRepository.getUser(authenticationRepository.user?.uid) }
            .mapNotNull { it.apply { this?.team = "-1" } }
            .flatMapLatest { firebaseRepository.writeUser(it) }
            .bind(_sharedTeamLeft, viewModelScope)
    }

}