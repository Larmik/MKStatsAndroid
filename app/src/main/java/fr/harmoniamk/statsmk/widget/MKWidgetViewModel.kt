package fr.harmoniamk.statsmk.widget

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.extension.getCurrent
import fr.harmoniamk.statsmk.extension.withName
import fr.harmoniamk.statsmk.model.local.MKWar
import fr.harmoniamk.statsmk.repository.DatabaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@HiltViewModel
class MKWidgetViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepositoryInterface,
    private val firebaseRepository: FirebaseRepositoryInterface,
    private val databaseRepository: DatabaseRepositoryInterface
) : ViewModel() {

    val currentWar
        get() =   firebaseRepository.listenToNewWars()
            .map { it.map { MKWar(it) } }
            .flatMapLatest { it.withName(databaseRepository = databaseRepository) }
            .map { it.getCurrent(preferencesRepository.currentTeam?.mid ?: "-1") }

}