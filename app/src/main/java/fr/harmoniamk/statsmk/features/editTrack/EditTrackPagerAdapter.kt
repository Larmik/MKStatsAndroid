package fr.harmoniamk.statsmk.features.editTrack

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import fr.harmoniamk.statsmk.features.editTrack.fragment.EditCurrentTrackFragment
import fr.harmoniamk.statsmk.features.position.PositionFragment
import fr.harmoniamk.statsmk.features.trackList.TrackListFragment
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

    override fun getItemCount() = 3

    override fun createFragment(position: Int): Fragment = when (position) {
        0 -> EditCurrentTrackFragment(trackId, onMapClick, onPositionClick)
        1 -> TrackListFragment(onMapEdit)
        else -> PositionFragment(onPositionEdit)
    }
}