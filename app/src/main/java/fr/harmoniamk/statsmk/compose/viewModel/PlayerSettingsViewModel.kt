package fr.harmoniamk.statsmk.compose.viewModel

import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.model.network.MKCLightPlayer
import fr.harmoniamk.statsmk.model.network.MKCPlayer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@HiltViewModel
class PlayerSettingsViewModel @Inject constructor() : ViewModel() {

    private val _sharedPlayers =
        MutableStateFlow<SnapshotStateList<MKCLightPlayer>>(SnapshotStateList())
    val sharedPlayers = _sharedPlayers.asStateFlow()
    private val players = mutableListOf<MKCPlayer>()

}