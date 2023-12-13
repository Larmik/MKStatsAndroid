package fr.harmoniamk.statsmk.compose.viewModel

import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.compose.ui.MKBottomSheetState
import fr.harmoniamk.statsmk.model.firebase.User
import fr.harmoniamk.statsmk.model.network.MKCFullTeam
import fr.harmoniamk.statsmk.model.network.MKCLightPlayer
import fr.harmoniamk.statsmk.repository.AuthenticationRepositoryInterface
import fr.harmoniamk.statsmk.repository.DatabaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.MKCentralRepositoryInterface
import fr.harmoniamk.statsmk.repository.NetworkRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@HiltViewModel
class TeamSettingsViewModel @Inject constructor(
    private val databaseRepository: DatabaseRepositoryInterface,
    private val mkCentralRepository: MKCentralRepositoryInterface
) : ViewModel() {

    private val _sharedPlayers = MutableStateFlow<List<MKCLightPlayer>>(listOf())
    private val _sharedAllies = MutableStateFlow<SnapshotStateList<MKCLightPlayer>>(SnapshotStateList())
    private val _sharedTeam = MutableStateFlow<MKCFullTeam?>(null)
    private val _sharedPictureLoaded = MutableStateFlow<String?>(null)
    private val _sharedBottomSheetValue = MutableStateFlow<MKBottomSheetState?>(null)

    val sharedPlayers = _sharedPlayers.asStateFlow()
    val sharedAllies = _sharedAllies.asStateFlow()
    val sharedTeam = _sharedTeam.asStateFlow()
    val sharedPictureLoaded =_sharedPictureLoaded.asStateFlow()
    val sharedBottomSheetValue = _sharedBottomSheetValue.asSharedFlow()

    private val allys = SnapshotStateList<MKCLightPlayer>()

    fun init() {
        mkCentralRepository.getTeam("874")
            .onEach {
                _sharedTeam.emit(it)
                _sharedPictureLoaded.emit(it.logoUrl)
                _sharedPlayers.emit(it.rosterList.orEmpty())
            }.launchIn(viewModelScope)

        databaseRepository.getRoster()
            .onEach { _sharedAllies.emit(createAllyList(list = it).first()) }
            .launchIn(viewModelScope)

    }

    fun dismissBottomSheet() {
        _sharedBottomSheetValue.value = null
    }


    private fun createAllyList(list: List<MKCLightPlayer>? = null): Flow<SnapshotStateList<MKCLightPlayer>> = flow {
        allys.clear()
        list?.sortedBy { it.name }?.forEach { player ->
            player.takeIf { it.isAlly == 1 }?.let {
                allys.add(it)
            }
        }
        emit(allys)
    }

}