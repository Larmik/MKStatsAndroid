package fr.harmoniamk.statsmk.features.wTrackResult

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.database.firebase.model.WarPosition
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
class WarTrackResultViewModel @Inject constructor(private val firebaseRepository: FirebaseRepositoryInterface, private val preferencesRepository: PreferencesRepositoryInterface) : ViewModel() {

    private val _sharedWarPos = MutableSharedFlow<List<WarPosition>>()
    private val _sharedBack = MutableSharedFlow<Unit>()
    private val _sharedQuit = MutableSharedFlow<Unit>()
    private val _sharedCancel = MutableSharedFlow<Unit>()

    val sharedWarPos = _sharedWarPos.asSharedFlow()
    val sharedBack = _sharedBack.asSharedFlow()
    val sharedQuit = _sharedQuit.asSharedFlow()
    val sharedCancel = _sharedCancel.asSharedFlow()

    fun bind(warTrackId: String? = null, onBack: Flow<Unit>, onQuit: Flow<Unit>, onBackDialog: Flow<Unit>) {
        warTrackId?.let { id ->
            firebaseRepository.listenToWarPositions()
                .map {
                    it.filter {
                            pos -> pos.warTrackId ==  warTrackId
                    } }
                .bind(_sharedWarPos, viewModelScope)
            onQuit
                .flatMapLatest { firebaseRepository.getWarPositions() }
                .mapNotNull {
                    it.lastOrNull {
                            pos -> pos.warTrackId == id && pos.playerId == preferencesRepository.currentUser?.name
                    }
                }
                .flatMapLatest {
                    firebaseRepository.deleteWarPosition(it)
                }
                .bind(_sharedQuit, viewModelScope)
        }

        onBack.bind(_sharedBack, viewModelScope)
        onBackDialog.bind(_sharedCancel, viewModelScope)

    }



}