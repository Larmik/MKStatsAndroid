package fr.harmoniamk.statsmk.features.addTrack

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import fr.harmoniamk.statsmk.features.position.PositionFragment
import fr.harmoniamk.statsmk.features.trackList.TrackListFragment
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





