package fr.harmoniamk.statsmk.compose.viewModel

import android.view.View
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
import java.util.*
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@HiltViewModel
class OpponentSettingsViewModel @Inject constructor(private val preferencesRepository: PreferencesRepositoryInterface, private val firebaseRepository: FirebaseRepositoryInterface, private val authenticationRepository: AuthenticationRepositoryInterface, private val databaseRepository: DatabaseRepositoryInterface, private val networkRepository: NetworkRepositoryInterface): ViewModel() {

    private val _sharedTeams = MutableStateFlow<List<Team>>(listOf())
    private val _sharedAddTeam = MutableSharedFlow<Unit>()
    private val _sharedAddTeamVisibility = MutableSharedFlow<Int>()
    private val _sharedOnEditClick = MutableSharedFlow<Team>()
    private val _sharedShowDialog = MutableSharedFlow<Boolean>()
    private val _sharedBottomSheetValue = MutableStateFlow<MKBottomSheetState?>(null)

    val sharedTeams = _sharedTeams.asStateFlow()
    val sharedBottomSheetValue = _sharedBottomSheetValue.asStateFlow()

    private val teams = mutableListOf<Team>()

    init {
        databaseRepository.getTeams()
            .map { it.sortedBy { it.name } }
            .map {
                teams.clear()
                teams.addAll(it.filterNot { team -> team.mid == preferencesRepository.currentTeam?.mid }); teams.sortedBy { it.name }
            }
            .bind(_sharedTeams, viewModelScope)

        authenticationRepository.userRole
            .mapNotNull { it >= UserRole.ADMIN.ordinal && networkRepository.networkAvailable}
            .mapNotNull {
                when (it) {
                    true -> View.VISIBLE
                    else -> View.GONE
                }
            }.bind(_sharedAddTeamVisibility, viewModelScope)
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

    fun bind(onAddTeam: Flow<Unit>, onEditClick: Flow<Team>, onSearch: Flow<String>) {
        onAddTeam.bind(_sharedAddTeam, viewModelScope)
        onEditClick.onEach {
            _sharedOnEditClick.emit(it)
            _sharedShowDialog.emit(true)
        }.launchIn(viewModelScope)

        onSearch
            .map { searched ->
                teams.filter {
                    it.shortName?.toLowerCase(Locale.ROOT)
                        ?.contains(searched.toLowerCase(Locale.ROOT)).isTrue || it.name?.toLowerCase(
                        Locale.ROOT)?.contains(searched.toLowerCase(Locale.ROOT)) ?: true
                }
            }.bind(_sharedTeams, viewModelScope)
    }

}