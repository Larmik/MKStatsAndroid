package fr.harmoniamk.statsmk.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.adapter.TrackListAdapter
import fr.harmoniamk.statsmk.databinding.FragmentTrackListBinding
import fr.harmoniamk.statsmk.extension.onTextChanged
import fr.harmoniamk.statsmk.viewModel.TrackListViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class TrackListFragment(val onTrack: MutableSharedFlow<Int>? = null) :
    Fragment(R.layout.fragment_track_list) {

    private val binding: FragmentTrackListBinding by viewBinding()
    private val viewModel: TrackListViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = TrackListAdapter()
        binding.trackRv.adapter = adapter
        viewModel.bind(binding.searchEt.onTextChanged(), adapter.sharedClick)
        viewModel.sharedSearchedItems
            .onEach { adapter.addTracks(it) }
            .launchIn(lifecycleScope)
        viewModel.sharedClick
            .onEach { onTrack?.emit(it) }
            .launchIn(lifecycleScope)
    }
}