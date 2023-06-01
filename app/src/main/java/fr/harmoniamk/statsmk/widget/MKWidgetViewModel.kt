package fr.harmoniamk.statsmk.widget

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.extension.withName
import fr.harmoniamk.statsmk.repository.DatabaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@HiltViewModel
class MKWidgetViewModel @Inject constructor(
    private val firebaseRepository: FirebaseRepositoryInterface
) : ViewModel() {

    val currentWar
        get() =  firebaseRepository.listenToCurrentWar()


}