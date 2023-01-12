package fr.harmoniamk.statsmk.fragment.settings.manageTeams


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.enums.UserRole
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.repository.AuthenticationRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@HiltViewModel
class EditTeamViewModel @Inject constructor(private val authenticationRepository: AuthenticationRepositoryInterface): ViewModel() {

    private val _sharedDeleteVisible = MutableSharedFlow<Boolean>()
    private val _sharedButtonEnabled = MutableSharedFlow<Boolean>()
    val sharedButtonEnabled = _sharedButtonEnabled.asSharedFlow()
    val sharedDeleteVisible = _sharedDeleteVisible.asSharedFlow()

    fun bind(onName: Flow<String>, onShortname: Flow<String>) {
        var name: String? = null
        var shortName: String? = null
        onName.onEach {
            name = it
            _sharedButtonEnabled.emit(!name.isNullOrEmpty() && !shortName.isNullOrEmpty())
        }.launchIn(viewModelScope)
        onShortname.onEach {
            shortName = it
            _sharedButtonEnabled.emit(!name.isNullOrEmpty() && !shortName.isNullOrEmpty())
        }.launchIn(viewModelScope)
        authenticationRepository.userRole
            .map { it == UserRole.GOD.ordinal }
            .bind(_sharedDeleteVisible, viewModelScope)
    }



}