package fr.harmoniamk.statsmk.fragment.manageTeams

import android.view.View
import fr.harmoniamk.statsmk.enums.UserRole
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
            val isAdmin =(authenticationRepository.userRole.firstOrNull() ?: 0) >= UserRole.ADMIN.ordinal
            when (isAdmin) {
                true -> emit(View.VISIBLE)
                else -> emit(View.INVISIBLE)
        }
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