package fr.harmoniamk.statsmk.fragment.mapStats

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.databinding.FragmentMapStatsBinding
import fr.harmoniamk.statsmk.enums.Maps
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@FlowPreview
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class MapStatsFragment : Fragment(R.layout.fragment_map_stats) {

    private val binding: FragmentMapStatsBinding by viewBinding()
    private val viewModel : MapStatsViewModel by viewModels()
    private var trackIndex: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        trackIndex = arguments?.getInt("trackId")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        trackIndex?.let {
            val adapter = MapStatsAdapter()
            binding.statTrackview.bind(it)
            binding.mapDetailsRv.adapter = adapter
            viewModel.bind(it)
            viewModel.sharedTrackList
                .onEach {
                    binding.emptyLayout.isVisible = false
                    binding.mainLayout.isVisible = true
                    adapter.addTracks(it)
                }.launchIn(lifecycleScope)
            viewModel.sharedTrackPlayed
                .onEach { binding.mapPlayed.text = it.toString() }
                .launchIn(lifecycleScope)
            viewModel.sharedTrackWon
                .onEach { binding.mapWon.text = it.toString() }
                .launchIn(lifecycleScope)
            viewModel.sharedWinRate
                .onEach {
                    binding.statWinrate.text = "$it %"
                }
                .launchIn(lifecycleScope)
            viewModel.sharedTeamScore
                .onEach { binding.mapTeamAverage.text = it.toString() }
                .launchIn(lifecycleScope)
            viewModel.sharedPlayerScore
                .onEach { binding.mapPlayerAverage.text = it }
                .launchIn(lifecycleScope)
            viewModel.sharedHighestVictory
                .filterNotNull()
                .onEach {
                    binding.noVictory.isVisible = false
                    binding.highestVictory.isVisible = true
                    binding.highestVictory.bind(it.first, it.second)
                }
                .launchIn(lifecycleScope)
            viewModel.sharedLoudestDefeat
                .filterNotNull()
                .onEach {
                    binding.noDefeat.isVisible = false
                    binding.loudestDefeat.isVisible = true
                    binding.loudestDefeat.bind(it.first, it.second)
                }
                .launchIn(lifecycleScope)
        }
    }

}