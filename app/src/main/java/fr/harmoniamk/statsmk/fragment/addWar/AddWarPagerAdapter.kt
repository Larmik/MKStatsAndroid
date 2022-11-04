package fr.harmoniamk.statsmk.fragment.addWar

import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import fr.harmoniamk.statsmk.fragment.playerSelect.PlayersWarFragment
import fr.harmoniamk.statsmk.fragment.teamSelect.WarTeamFragment
import fr.harmoniamk.statsmk.model.firebase.Team
import fr.harmoniamk.statsmk.model.firebase.User
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow

@FlowPreview
@ExperimentalCoroutinesApi
class AddWarPagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {

    val onTeamSelected = MutableSharedFlow<Team>()
    val onWarCreated = MutableSharedFlow<Unit>()
    val onOfficialCheck = MutableSharedFlow<Boolean>()
    val onUsersSelected = MutableSharedFlow<List<User>>()

    override fun getItemCount() = 2

    override fun createFragment(position: Int) = when (position) {
        0 -> WarTeamFragment(onTeamSelected)
        else -> PlayersWarFragment(onWarCreated, onUsersSelected, onOfficialCheck)
    }

}