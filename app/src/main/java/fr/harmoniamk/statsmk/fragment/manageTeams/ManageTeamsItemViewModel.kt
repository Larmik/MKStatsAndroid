package fr.harmoniamk.statsmk.fragment.manageTeams

import android.view.View
import fr.harmoniamk.statsmk.model.firebase.Team
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalCoroutinesApi
class ManageTeamsItemViewModel(val team: Team, private val preferencesRepository: PreferencesRepositoryInterface) {

    val buttonVisibility: Int
        get() = when (preferencesRepository.currentUser?.isAdmin) {
            true -> View.VISIBLE
            else -> View.INVISIBLE
        }

    val checkMarkVisibility: Int
        get() = when (team.accessCode.isNullOrEmpty() || team.accessCode == "null") {
            true -> View.INVISIBLE
            else -> View.VISIBLE
        }

    val name: String?
        get() = team.name

    val shortName: String?
        get() = team.shortName
}