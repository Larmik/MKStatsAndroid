package fr.harmoniamk.statsmk.fragment.mapRanking

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.databinding.FragmentMapRankingBinding
import fr.harmoniamk.statsmk.extension.clicks
import fr.harmoniamk.statsmk.fragment.trackList.TrackListAdapter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

@ExperimentalCoroutinesApi
@FlowPreview
@AndroidEntryPoint
class MapRankingFragment : Fragment(R.layout.fragment_map_ranking) {

    private val binding: FragmentMapRankingBinding by viewBinding()
    private val viewModel: MapRankingViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mostPlayedAdapter = MapRankingAdapter()
        val mostWonAdapter = MapRankingAdapter()
        val mostLossAdapter = MapRankingAdapter()
        binding.mostPlayedRv.adapter = mostPlayedAdapter
        binding.mostWonRv.adapter = mostWonAdapter
        binding.mostLoseRv.adapter = mostLossAdapter
        viewModel.bind(
            onTrackClick = flowOf(mostPlayedAdapter.sharedClick, mostLossAdapter.sharedClick, mostWonAdapter.sharedClick).flattenMerge(),
            onAllTrackClick = binding.allMapsButton.clicks()
        )

        viewModel.sharedMostPlayedMaps
            .onEach { mostPlayedAdapter.addTracks(it) }
            .launchIn(lifecycleScope)
        viewModel.sharedMostWonMaps
            .onEach { mostWonAdapter.addTracks(it) }
            .launchIn(lifecycleScope)
        viewModel.sharedMostLossMaps
            .onEach { mostLossAdapter.addTracks(it) }
            .launchIn(lifecycleScope)
        viewModel.sharedGoToStats
            .filter { findNavController().currentDestination?.id == R.id.mapRankingFragment }
            .onEach { findNavController().navigate(MapRankingFragmentDirections.toMapStats(it)) }
            .launchIn(lifecycleScope)
        viewModel.sharedGoToTrackList
            .filter { findNavController().currentDestination?.id == R.id.mapRankingFragment }
            .onEach { findNavController().navigate(MapRankingFragmentDirections.toTrackList(true)) }
            .launchIn(lifecycleScope)
    }

}