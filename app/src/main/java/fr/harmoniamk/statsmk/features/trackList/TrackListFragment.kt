package fr.harmoniamk.statsmk.features.trackList

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.databinding.FragmentTrackListBinding
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.launchIn
import fr.harmoniamk.statsmk.extension.onTextChanged
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.onEach


@FlowPreview
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class TrackListFragment(val onTrack: MutableSharedFlow<Int>? = null) :
    Fragment(R.layout.fragment_track_list) {

    private val binding: FragmentTrackListBinding by viewBinding()
    private val viewModel: TrackListViewModel by viewModels()

    private var tmId = -1
    private var warId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tmId = arguments?.getInt("tmId") ?: -1
        warId = arguments?.getString("warId")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = TrackListAdapter()
        binding.trackRv.adapter = adapter
        viewModel.bind(tmId, warId, adapter.sharedClick, binding.searchEt.onTextChanged())
        viewModel.sharedSearchedItems
            .onEach { adapter.addTracks(it) }
            .launchIn(lifecycleScope)

        viewModel.sharedGoToPos
            .filter { findNavController().currentDestination?.id == R.id.trackListFragment }
            .onEach {
                findNavController().navigate(TrackListFragmentDirections.enterPositions(track = it, tmId = tmId, warId = warId))
            }
            .launchIn(lifecycleScope)

    }
}