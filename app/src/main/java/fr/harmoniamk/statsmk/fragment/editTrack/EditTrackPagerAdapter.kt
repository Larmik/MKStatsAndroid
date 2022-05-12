package fr.harmoniamk.statsmk.fragment.editTrack

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import fr.harmoniamk.statsmk.fragment.trackList.TrackListFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow

@FlowPreview
@ExperimentalCoroutinesApi
class EditTrackPagerAdapter(val trackId: Int, fa: FragmentActivity) : FragmentStateAdapter(fa) {

    val onMapClick = MutableSharedFlow<Unit>()
    val onMapEdit = MutableSharedFlow<Int>()
    val onPositionClick = MutableSharedFlow<Unit>()
    val onPositionEdit = MutableSharedFlow<Int>()

    override fun getItemCount() = 2

    override fun createFragment(position: Int): Fragment = when (position) {
        0 -> EditCurrentTrackFragment(trackId, onMapClick, onPositionClick)
        else -> TrackListFragment(onMapEdit)
    }
}