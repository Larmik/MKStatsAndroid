package fr.harmoniamk.statsmk.features.manageTeams.viewModel

import android.view.View
import fr.harmoniamk.statsmk.database.model.Team
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalCoroutinesApi
class ManageTeamsItemViewModel(val team: Team, private val preferencesRepository: PreferencesRepositoryInterface) {

    val deleteButtonVisibility: Int
        get() = when (preferencesRepository.currentUser?.isAdmin) {
            true -> View.VISIBLE
            else -> View.GONE
        }

    val name: String?
        get() = team.name
}