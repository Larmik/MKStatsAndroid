package fr.harmoniamk.statsmk.fragment.managePlayers

import android.view.View
import fr.harmoniamk.statsmk.model.firebase.User
import fr.harmoniamk.statsmk.extension.isTrue
import fr.harmoniamk.statsmk.repository.AuthenticationRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import java.util.concurrent.Flow

@FlowPreview
@ExperimentalCoroutinesApi
class ManagePlayersItemViewModel(val player: User? = null, val isCategory: Boolean = false, private val preferencesRepository: PreferencesRepositoryInterface? = null, private val authenticationRepository: AuthenticationRepositoryInterface? = null) {

    val buttonsVisibility
        get() = flow  {
            val isAdmin = authenticationRepository?.isAdmin?.firstOrNull()
            when {
                authenticationRepository?.user?.uid == player?.mid || isAdmin.isTrue -> emit(View.VISIBLE)
                else -> emit(View.INVISIBLE)
            }
        }

    val name: String?
        get() = player?.name

    val checkmarkVisibility: Int
        get() = if (hasAccount) View.VISIBLE
                else View.INVISIBLE

    private val hasAccount: Boolean
        get() = player?.accessCode != "null" && !player?.accessCode.isNullOrEmpty()

    val isAlly = player?.team != preferencesRepository?.currentTeam?.mid


}