package fr.harmoniamk.statsmk.adapter

import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.fragment.SettingsFragment
import fr.harmoniamk.statsmk.fragment.TimeTrialFragment
import fr.harmoniamk.statsmk.fragment.TournamentFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalCoroutinesApi
class HomePagerAdapter(val fa: FragmentActivity) : FragmentStateAdapter(fa) {

    fun getTabTitle(position: Int): String = fa.getString(when (position) {
        0 -> R.string.tournament
        1 -> R.string.team_war
        else -> R.string.settings
    })

    override fun getItemCount() = 3

    override fun createFragment(position: Int) = when (position) {
        0 -> TournamentFragment()
        1 -> TimeTrialFragment()
        else -> SettingsFragment()
    }
}