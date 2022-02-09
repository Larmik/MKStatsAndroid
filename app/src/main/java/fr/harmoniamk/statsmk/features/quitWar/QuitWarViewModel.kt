package fr.harmoniamk.statsmk.features.quitWar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@HiltViewModel
class QuitWarViewModel @Inject constructor(private val firebaseRepository: FirebaseRepositoryInterface, private val preferencesRepository: PreferencesRepositoryInterface) : ViewModel() {

    private val _onDismiss = MutableSharedFlow<Unit>()
    private val _onWarQuit = MutableSharedFlow<Unit>()

    val onDismiss = _onDismiss.asSharedFlow()
    val onWarQuit = _onWarQuit.asSharedFlow()

    fun bind(onQuit: Flow<Unit>, onBack: Flow<Unit>) {
        onBack.bind(_onDismiss, viewModelScope)
        onQuit
            .mapNotNull { preferencesRepository.currentUser?.apply { this.currentWar = "-1" } }
            .flatMapLatest { firebaseRepository.writeUser(it) }
            .bind(_onWarQuit, viewModelScope)
    }

}