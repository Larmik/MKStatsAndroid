package fr.harmoniamk.statsmk.features.addWar.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import fr.harmoniamk.statsmk.features.addWar.fragment.CreateWarFragment
import fr.harmoniamk.statsmk.features.addWar.fragment.WaitPlayersFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow

@ExperimentalCoroutinesApi
@FlowPreview
class AddWarPagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {

    val onCreateWar = MutableSharedFlow<Unit>()
    val onWarQuit = MutableSharedFlow<Unit>()

    override fun getItemCount() = 2

    override fun createFragment(position: Int): Fragment = when (position) {
        0 -> CreateWarFragment(onCreateWar)
        else -> {
            val fragment = WaitPlayersFragment()
            fragment.onWarQuit = onWarQuit
            fragment
        }
    }
}
