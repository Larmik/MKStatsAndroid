package fr.harmoniamk.statsmk.fragment.mapStats

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.databinding.FragmentMapStatsBinding
import fr.harmoniamk.statsmk.extension.clicks
import fr.harmoniamk.statsmk.extension.positionColor
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.filter
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
        trackIndex?.let { index ->
            val adapter = MapStatsAdapter()
            binding.statTrackview.bind(index)
            binding.mapDetailsRv.adapter = adapter
            viewModel.bind(
                trackIndex = index,
                onMapClick = adapter.onMapClick,
                onVictoryClick = binding.highestVictory.clicks(),
                onDefeatClick = binding.loudestDefeat.clicks()
            )
            viewModel.sharedStats.onEach { stats ->
                binding.progress.isVisible = false
                binding.emptyLayout.isVisible = stats.list.isEmpty()
                binding.mainLayout.isVisible = stats.list.isNotEmpty()
                adapter.addTracks(stats.list)
                binding.mapPlayed.text = stats.trackPlayed.toString()
                binding.mapWon.text = stats.trackWon.toString()
                binding.statWinrate.text = stats.winRate
                binding.mapTeamAverage.text = stats.teamScore.toString()
                binding.mapPlayerAverage.text = when (stats.playerScore) {
                    0 -> "-"
                    else -> stats.playerScore.toString()
                }
                binding.mapPlayerAverage.setTextColor(
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                        requireContext().getColor(stats.playerScore.positionColor())
                    else ContextCompat.getColor(requireContext(), stats.playerScore.positionColor())
                )
                stats.highestVictory?.let {
                    binding.noVictory.isVisible = false
                    binding.highestVictory.isVisible = true
                    binding.highestVictory.bind(it.war, it.warTrack)
                }
                stats.loudestDefeat?.let {
                    binding.noDefeat.isVisible = false
                    binding.loudestDefeat.isVisible = true
                    binding.loudestDefeat.bind(it.war, it.warTrack)
                }
            }.launchIn(lifecycleScope)

            viewModel.sharedMapClick
                .filter { findNavController().currentDestination?.id == R.id.mapStatsFragment }
                .onEach { findNavController().navigate(MapStatsFragmentDirections.toTrackDetails(it.war.war, it.warTrack.track)) }
                .launchIn(lifecycleScope)

        }
    }

}