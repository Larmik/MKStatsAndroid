package fr.harmoniamk.statsmk.compose.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.model.network.MKCPlayer
import fr.harmoniamk.statsmk.model.network.NetworkResponse
import fr.harmoniamk.statsmk.repository.MKCentralRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@HiltViewModel
class PlayerSettingsViewModel @Inject constructor(mkCentralRepository: MKCentralRepositoryInterface) : ViewModel() {

    private val _sharedPlayers = MutableStateFlow<List<MKCPlayer>>(listOf())
    private val _sharedLoader = MutableStateFlow(false)
    val sharedPlayers = _sharedPlayers.asStateFlow()
    val sharedLoader = _sharedLoader.asStateFlow()
    private val players = mutableListOf<MKCPlayer>()

    private val search = MutableStateFlow("")

    init {
        search
            .debounce(500)
            .filter { it.length >= 3 }
            .onEach { _sharedLoader.value = true }
            .flatMapLatest { mkCentralRepository.searchPlayers(it) }
            .mapNotNull { (it as? NetworkResponse.Success)?.response }
            .onEach {
                players.clear()
                players.addAll(it)
                _sharedPlayers.value = it
                _sharedLoader.value = false
            }
            .bind(_sharedPlayers, viewModelScope)

        search
            .filter { it.length < 3 }
            .onEach {
                players.clear()
                _sharedPlayers.value = listOf()
            }.launchIn(viewModelScope)
    }

    fun onSearch(search: String) {
      this.search.value = search
    }

}