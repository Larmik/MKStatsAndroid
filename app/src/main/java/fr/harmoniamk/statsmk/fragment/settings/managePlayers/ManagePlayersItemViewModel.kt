package fr.harmoniamk.statsmk.fragment.settings.managePlayers

import android.view.View
import fr.harmoniamk.statsmk.enums.UserRole
import fr.harmoniamk.statsmk.model.firebase.User
import fr.harmoniamk.statsmk.repository.AuthenticationRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.map

@FlowPreview
@ExperimentalCoroutinesApi
class ManagePlayersItemViewModel(val player: User? = null, val isCategory: Boolean = false, private val preferencesRepository: PreferencesRepositoryInterface? = null, private val authenticationRepository: AuthenticationRepositoryInterface? = null) {

    val buttonsVisibility
        get() = authenticationRepository?.userRole?.map {
            val isVisible =  authenticationRepository.user?.uid == player?.mid
                    || (!hasAccount && it >= UserRole.ADMIN.ordinal)
                    ||  (it >= UserRole.LEADER.ordinal && player?.team == preferencesRepository?.currentTeam?.mid)
                    || (!hasAccount && player?.team == "-1")
            when (isVisible) {
                true -> View.VISIBLE
                else -> View.INVISIBLE
            }

        }



    val name: String?
        get() = player?.name

    val checkmarkVisibility: Int
        get() = if (hasAccount) View.VISIBLE
                else View.INVISIBLE

    val hasAccount: Boolean
        get() = player?.mid?.toLongOrNull() == null

    val isAlly = player?.team != preferencesRepository?.currentTeam?.mid


}