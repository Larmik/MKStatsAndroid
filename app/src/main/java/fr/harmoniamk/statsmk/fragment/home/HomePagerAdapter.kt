package fr.harmoniamk.statsmk.fragment.home

import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.fragment.home.settings.SettingsFragment
import fr.harmoniamk.statsmk.fragment.home.stats.StatsFragment
import fr.harmoniamk.statsmk.fragment.home.war.WarFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalCoroutinesApi
class HomePagerAdapter(val fa: FragmentActivity) : FragmentStateAdapter(fa) {

    fun getTabTitle(position: Int): String = fa.getString(when (position) {
        //0 -> R.string.tournament
        0 -> R.string.team_war
        1 -> R.string.stats
        else -> R.string.settings
    })

    override fun getItemCount() = 3

    override fun createFragment(position: Int) = when (position) {
        0 -> WarFragment()
        1 -> StatsFragment()
        else -> SettingsFragment()
    }
}