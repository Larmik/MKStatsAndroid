package fr.harmoniamk.statsmk.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import fr.harmoniamk.statsmk.fragment.PositionFragment
import fr.harmoniamk.statsmk.fragment.TrackListFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow

@ExperimentalCoroutinesApi
class AddTrackPagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {

    val onMap = MutableSharedFlow<Int>()
    val onPosition = MutableSharedFlow<Int>()

    override fun getItemCount() = 2

    override fun createFragment(position: Int): Fragment = when (position) {
        0 -> TrackListFragment(onMap)
        else -> PositionFragment(onPosition)
    }
}





