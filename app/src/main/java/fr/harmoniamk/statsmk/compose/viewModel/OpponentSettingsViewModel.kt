package fr.harmoniamk.statsmk.compose.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.compose.ui.MKBottomSheetState
import fr.harmoniamk.statsmk.enums.UserRole
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.extension.isTrue
import fr.harmoniamk.statsmk.model.firebase.Team
import fr.harmoniamk.statsmk.repository.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@HiltViewModel
class OpponentSettingsViewModel @Inject constructor(private val preferencesRepository: PreferencesRepositoryInterface, private val firebaseRepository: FirebaseRepositoryInterface, private val authenticationRepository: AuthenticationRepositoryInterface, private val databaseRepository: DatabaseRepositoryInterface, private val networkRepository: NetworkRepositoryInterface): ViewModel() {

    private val _sharedTeams = MutableStateFlow<List<Team>>(listOf())
    private val _sharedAddTeamVisibility = MutableStateFlow(false)
    private val _sharedBottomSheetValue = MutableStateFlow<MKBottomSheetState?>(null)

    val sharedTeams = _sharedTeams.asStateFlow()
    val sharedBottomSheetValue = _sharedBottomSheetValue.asStateFlow()
    val sharedAddTeamVisibility = _sharedAddTeamVisibility.asStateFlow()

    private val teams = mutableListOf<Team>()

    init {
        databaseRepository.getTeams()
            .map { it.sortedBy { it.name } }
            .map {
                teams.clear()
                teams.addAll(it.filterNot { team -> team.mid == preferencesRepository.currentTeam?.mid }); teams.sortedBy { it.name }
            }
            .bind(_sharedTeams, viewModelScope)

        authenticationRepository.takeIf { preferencesRepository.currentTeam != null }
            ?.userRole
            ?.mapNotNull { it >= UserRole.ADMIN.ordinal && networkRepository.networkAvailable}
            ?.onEach { _sharedAddTeamVisibility.value = it }
            ?.launchIn(viewModelScope)
    }

    fun onAddTeam() {
        _sharedBottomSheetValue.value = MKBottomSheetState.CreateTeam()
    }

    fun onEditTeam(id: String) {
        _sharedBottomSheetValue.value = MKBottomSheetState.EditTeam(id)
    }

    fun dismissBottomSheet() {
        _sharedBottomSheetValue.value = null
    }

    fun onSearch(search: String) {
        _sharedTeams.value = when (search.isNotEmpty()) {
            true -> teams.filter { it.name?.lowercase()?.contains(search).isTrue || it.shortName?.lowercase()?.contains(search).isTrue }
            else -> teams
        }
    }


}