package fr.harmoniamk.statsmk.features.addWar.adapter

import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import fr.harmoniamk.statsmk.database.model.Team
import fr.harmoniamk.statsmk.database.model.User
import fr.harmoniamk.statsmk.features.addWar.fragment.PlayersWarFragment
import fr.harmoniamk.statsmk.features.addWar.fragment.WarTeamFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow

@FlowPreview
@ExperimentalCoroutinesApi
class AddWarPagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {

    val onTeamSelected = MutableSharedFlow<Team>()
    val onWarCreated = MutableSharedFlow<Unit>()
    val onUsersSelected = MutableSharedFlow<List<User>>()

    override fun getItemCount() = 2

    override fun createFragment(position: Int) = when (position) {
        0 -> WarTeamFragment(onTeamSelected)
        else -> PlayersWarFragment(onWarCreated, onUsersSelected)
    }

}