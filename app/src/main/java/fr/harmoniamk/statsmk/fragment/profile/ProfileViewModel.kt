package fr.harmoniamk.statsmk.fragment.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.model.firebase.PictureResponse
import fr.harmoniamk.statsmk.model.firebase.ResetPasswordResponse
import fr.harmoniamk.statsmk.model.firebase.UploadPictureResponse
import fr.harmoniamk.statsmk.repository.AuthenticationRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import fr.harmoniamk.statsmk.repository.StorageRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@HiltViewModel
class ProfileViewModel @Inject constructor(private val authenticationRepository: AuthenticationRepositoryInterface, private val storageRepository: StorageRepository, private val preferencesRepository: PreferencesRepositoryInterface) : ViewModel() {

    private val _sharedProfile = MutableSharedFlow<FirebaseUser>()
    private val _sharedEditPicture = MutableSharedFlow<Unit>()
    private val _sharedDisconnectPopup = MutableSharedFlow<Boolean>()
    private val _sharedPictureLoaded = MutableSharedFlow<String?>()
    private val _showResetPopup = MutableSharedFlow<String>()
    private val _sharedDisconnect = MutableSharedFlow<Unit>()

    val sharedProfile =_sharedProfile.asSharedFlow()
    val sharedEditPicture =_sharedEditPicture.asSharedFlow()
    val sharedPictureLoaded =_sharedPictureLoaded.asSharedFlow()
    val showResetPopup =_showResetPopup.asSharedFlow()
    val sharedDisconnect =_sharedDisconnect.asSharedFlow()
    val sharedDisconnectPopup = _sharedDisconnectPopup.asSharedFlow()

    fun bind(onPictureClick: Flow<Unit>, onPictureEdited: Flow<String>, onChangePasswordClick: Flow<Unit>, onLogout: Flow<Unit>, onPopup: Flow<Boolean>) {
        var url: String? = null

        storageRepository.getPicture(authenticationRepository.user?.uid)
            .mapNotNull { authenticationRepository.user }
            .bind(_sharedProfile, viewModelScope)

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
                preferencesRepository.currentUser = null
                preferencesRepository.currentTeam = null
                preferencesRepository.authEmail = null
                preferencesRepository.authPassword = null
            }.bind(_sharedDisconnect, viewModelScope)

        onPictureClick.bind(_sharedEditPicture, viewModelScope)
    }

}