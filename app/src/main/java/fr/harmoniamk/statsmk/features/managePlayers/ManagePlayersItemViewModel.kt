package fr.harmoniamk.statsmk.features.managePlayers

import android.view.View
import fr.harmoniamk.statsmk.database.model.User
import fr.harmoniamk.statsmk.extension.isTrue
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalCoroutinesApi
class ManagePlayersItemViewModel(val player: User, private val preferencesRepository: PreferencesRepositoryInterface) {

    val buttonsVisibility: Int
        get() = when  {
            (preferencesRepository.currentUser?.mid == player.mid) ||
            (preferencesRepository.currentUser?.isAdmin.isTrue && !hasAccount) -> View.VISIBLE
            else -> View.INVISIBLE
        }

    val name: String?
        get() = player.name

    val checkmarkVisibility: Int
        get() = if (hasAccount) View.VISIBLE
                else View.INVISIBLE

    private val hasAccount: Boolean
        get() = player.accessCode != "null" && !player.accessCode.isNullOrEmpty()

}