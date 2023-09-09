package fr.harmoniamk.statsmk.compose.viewModel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.compose.ui.MKBottomSheetState
import fr.harmoniamk.statsmk.compose.ui.MKDialogState
import fr.harmoniamk.statsmk.enums.UserRole
import fr.harmoniamk.statsmk.extension.withName
import fr.harmoniamk.statsmk.model.firebase.PictureResponse
import fr.harmoniamk.statsmk.model.firebase.ResetPasswordResponse
import fr.harmoniamk.statsmk.model.firebase.UploadPictureResponse
import fr.harmoniamk.statsmk.model.local.MKWar
import fr.harmoniamk.statsmk.repository.AuthenticationRepositoryInterface
import fr.harmoniamk.statsmk.repository.DatabaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.NetworkRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import fr.harmoniamk.statsmk.repository.StorageRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authenticationRepository: AuthenticationRepositoryInterface,
    private val storageRepository: StorageRepository,
    private val preferencesRepository: PreferencesRepositoryInterface,
    private val firebaseRepository: FirebaseRepositoryInterface,
    private val databaseRepository: DatabaseRepositoryInterface,
    private val networkRepository: NetworkRepositoryInterface) : ViewModel() {

    private val _sharedPictureLoaded = MutableStateFlow<String?>(null)
    private val _sharedDisconnect = MutableSharedFlow<Unit>()
    private val _sharedTeam = MutableStateFlow<String?>(null)
    private val _sharedFriendCode = MutableStateFlow<String?>(null)
    private val _sharedRole = MutableStateFlow<Int?>(null)
    private val _sharedLocalPicture = MutableStateFlow<Int?>(null)
    private val _sharedBottomSheetValue = MutableStateFlow<MKBottomSheetState?>(null)
    private val _sharedDialogValue = MutableStateFlow<MKDialogState?>(null)
    private val _sharedName = MutableStateFlow<String?>(null)
    private val _sharedEmail = MutableStateFlow<String?>(null)

    val sharedPictureLoaded =_sharedPictureLoaded.asStateFlow()
    val sharedDisconnect =_sharedDisconnect.asSharedFlow()
    val sharedTeam = _sharedTeam.asStateFlow()
    val sharedFriendCode = _sharedFriendCode.asStateFlow()
    val sharedRole = _sharedRole.asStateFlow()
    val sharedLocalPicture = _sharedLocalPicture.asStateFlow()
    val sharedBottomSheetValue = _sharedBottomSheetValue.asSharedFlow()
    val sharedDialogValue = _sharedDialogValue.asSharedFlow()
    val sharedName = _sharedName.asStateFlow()
    val sharedEmail = _sharedEmail.asStateFlow()

    init { refresh() }

    fun refresh() {
        databaseRepository.getUser(authenticationRepository.user?.uid)
            .filterNotNull()
            .onEach {
                _sharedName.value = authenticationRepository.user?.displayName
                _sharedTeam.value = preferencesRepository.currentTeam?.name
                _sharedPictureLoaded.value = authenticationRepository.user?.photoUrl?.toString()
                _sharedEmail.value = authenticationRepository.user?.email
                _sharedLocalPicture.takeIf { !networkRepository.networkAvailable }?.value = R.drawable.mk_stats_logo_picture
                _sharedFriendCode.value = it.friendCode.takeIf { code -> code != "null" }
                it.role?.let { role -> _sharedRole.value = UserRole.values().getOrNull(role)?.labelId }
            }.launchIn(viewModelScope)
    }

    fun dismissBottomSheet() {
        _sharedBottomSheetValue.value = null
        refresh()
    }

    fun onEditNickname() {
        _sharedBottomSheetValue.value = MKBottomSheetState.EditUser(false)
    }

    fun onEditEmail() {
        _sharedBottomSheetValue.value = MKBottomSheetState.EditUser(true)
    }

    fun onEditPassword() {
        authenticationRepository.resetPassword(authenticationRepository.user?.email.toString())
            .onEach {
                _sharedDialogValue.value =
                MKDialogState.ChangePassword(
                   text =  when (it) {
                        is ResetPasswordResponse.Success -> R.string.reset_pwd_success
                        is ResetPasswordResponse.Error -> R.string.reset_pwd_error
                    },
                    onDismiss = {
                        _sharedDialogValue.value = null
                    }
                )

            }.launchIn(viewModelScope)
    }

    fun onLeaveTeam() {
        _sharedDialogValue.value = MKDialogState.LeaveTeam(
            onTeamLeft = {
                databaseRepository.getUser(authenticationRepository.user?.uid)
                    .mapNotNull {
                        val formerTeams = mutableListOf<String?>()
                        formerTeams.addAll(it?.formerTeams.orEmpty())
                        formerTeams.add(preferencesRepository.currentTeam?.mid)
                        it?.formerTeams?.takeIf { it.isNotEmpty() }?.let {
                            it.forEach {
                                val wars = firebaseRepository.getNewWars(it)
                                    .map { list -> list.map {  MKWar(it) } }
                                    .first()
                                val finalList = wars.withName(databaseRepository).first()
                                databaseRepository.writeWars(finalList).first()
                            }
                        }
                        it.apply {
                            this?.team = "-1"
                            this?.formerTeams = formerTeams.distinct().filterNotNull()
                            this?.role = UserRole.MEMBER.ordinal
                        }
                    }
                    .flatMapLatest { firebaseRepository.writeUser(it) }
                    .onEach {
                        preferencesRepository.currentTeam = null
                        _sharedDialogValue.value = null
                    }
                    .launchIn(viewModelScope)
            },
            onDismiss = {
                _sharedDialogValue.value = null
                refresh()
            }

        )
    }

    fun onLogout() {
        _sharedDialogValue.value = MKDialogState.Logout(
            onLogout = {
                viewModelScope.launch {
                    authenticationRepository.signOut()
                    preferencesRepository.currentTeam = null
                    preferencesRepository.authEmail = null
                    preferencesRepository.authPassword = null
                    _sharedDisconnect.emit(Unit)
                }
            },
            onDismiss = {
                _sharedDialogValue.value = null
            }
        )
    }

    fun onPictureEdited(pictureUri: Uri?) {
        pictureUri?.let { uri ->
            var url: String? = null
            storageRepository.uploadPicture(authenticationRepository.user?.uid, uri)
                .filter { it is UploadPictureResponse.Success }
                .mapNotNull { authenticationRepository.user?.uid }
                .flatMapLatest { storageRepository.getPicture(it) }
                .mapNotNull { url = (it as? PictureResponse.Success)?.url; url }
                .flatMapLatest { databaseRepository.getUser(authenticationRepository.user?.uid) }
                .filterNotNull()
                .flatMapLatest { firebaseRepository.writeUser(it.apply { this.picture = url }) }
                .flatMapLatest { authenticationRepository.updateProfile(authenticationRepository.user?.displayName.toString(), url) }
                .onEach { _sharedPictureLoaded.emit(url) }
                .launchIn(viewModelScope)
        }
    }

}