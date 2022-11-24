package fr.harmoniamk.statsmk.fragment.manageTeams


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
    val sharedDeleteVisible = _sharedDeleteVisible.asSharedFlow()

    init {
        authenticationRepository.userRole
            .map { it == UserRole.GOD.ordinal }
            .bind(_sharedDeleteVisible, viewModelScope)
    }

}