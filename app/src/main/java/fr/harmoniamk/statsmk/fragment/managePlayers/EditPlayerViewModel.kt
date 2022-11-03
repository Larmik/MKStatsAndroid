package fr.harmoniamk.statsmk.fragment.managePlayers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.repository.AuthenticationRepositoryInterface
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject

@HiltViewModel
class EditPlayerViewModel @Inject constructor(private val authenticationRepository: AuthenticationRepositoryInterface): ViewModel() {

    private val _sharedIsAdmin = MutableSharedFlow<Boolean>()
    val sharedIsAdmin = _sharedIsAdmin.asSharedFlow()

    init {
        authenticationRepository.isAdmin.bind(_sharedIsAdmin, viewModelScope)
    }

}