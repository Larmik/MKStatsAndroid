package fr.harmoniamk.statsmk.fragment.settings.manageTeams

import android.view.View
import fr.harmoniamk.statsmk.enums.UserRole
import fr.harmoniamk.statsmk.extension.isTrue
import fr.harmoniamk.statsmk.model.firebase.Team
import fr.harmoniamk.statsmk.repository.AuthenticationRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow

@FlowPreview
@ExperimentalCoroutinesApi
class ManageTeamsItemViewModel(val team: Team, private val authenticationRepository: AuthenticationRepositoryInterface) {

    val buttonVisibility
        get() = flow {
            val isAdmin = (authenticationRepository.userRole.firstOrNull() ?: 0) >= UserRole.ADMIN.ordinal && (!team.hasLeader.isTrue)
            when (isAdmin) {
                true -> emit(View.VISIBLE)
                else -> emit(View.INVISIBLE)
        }
        }

    val checkMarkVisibility: Int
        get() = when (team.hasLeader) {
            true -> View.VISIBLE
            else -> View.INVISIBLE
        }

    val name: String?
        get() = team.name

    val shortName: String?
        get() = team.shortName
}