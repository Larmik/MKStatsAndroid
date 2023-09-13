package fr.harmoniamk.statsmk.compose.viewModel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.model.local.MapStats
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class WarTrackListViewModel @Inject constructor() : ViewModel() {

    private val _sharedMapStats = MutableStateFlow<MapStats?>(null)

    val sharedMapStats = _sharedMapStats.asStateFlow()
}