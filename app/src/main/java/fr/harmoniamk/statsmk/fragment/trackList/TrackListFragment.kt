package fr.harmoniamk.statsmk.fragment.trackList

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
import fr.harmoniamk.statsmk.extension.backPressedDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.launchIn
import fr.harmoniamk.statsmk.extension.onTextChanged
import fr.harmoniamk.statsmk.model.local.MKWar
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.onEach


@FlowPreview
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class TrackListFragment :
    Fragment(R.layout.fragment_track_list) {

    private val binding: FragmentTrackListBinding by viewBinding()
    private val viewModel: TrackListViewModel by viewModels()

    private var tmId : Int? = null
    private var warId: String? = null
    private val list = mutableListOf<MKWar>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tmId = arguments?.getInt("tmId").takeIf { it != 0 }
        warId = arguments?.getString("warId")
        (arguments?.get("wars") as? Array<out MKWar>)?.let {
            list.addAll(it)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = TrackListAdapter()
        binding.trackRv.adapter = adapter
        viewModel.bind(
            tournamentId = tmId,
            warId = warId,
            onTrackAdded = adapter.sharedClick,
            onSearch = binding.searchEt.onTextChanged(),
            onBack = requireActivity().backPressedDispatcher(viewLifecycleOwner),
        )
        viewModel.sharedSearchedItems
            .onEach { adapter.addTracks(it) }
            .launchIn(lifecycleScope)

        viewModel.sharedGoToTmPos
            .filter { findNavController().currentDestination?.id == R.id.trackListFragment }
            .onEach {
                findNavController().navigate(TrackListFragmentDirections.enterPositions(track = it, tmId = tmId ?: -1, warTrackId = null ))
            }
            .launchIn(lifecycleScope)

        viewModel.sharedGoToWarPos
            .filter { findNavController().currentDestination?.id == R.id.trackListFragment }
            .onEach {
                findNavController().navigate(TrackListFragmentDirections.enterPositions(track = it.trackIndex ?: -1, tmId = tmId ?: -1, warTrackId = it.mid))
            }
            .launchIn(lifecycleScope)

        viewModel.sharedQuit
            .filter { findNavController().currentDestination?.id == R.id.trackListFragment }
            .onEach { findNavController().popBackStack() }
            .launchIn(lifecycleScope)

    }
}