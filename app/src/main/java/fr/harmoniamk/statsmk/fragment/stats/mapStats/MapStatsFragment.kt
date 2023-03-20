package fr.harmoniamk.statsmk.fragment.stats.mapStats

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
import fr.harmoniamk.statsmk.extension.isTrue
import fr.harmoniamk.statsmk.extension.positionColor
import fr.harmoniamk.statsmk.extension.trackScoreToDiff
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
    private var isIndiv: Boolean? = null
    private var isWeek: Boolean? = null
    private var isMonth: Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        trackIndex = arguments?.getInt("trackId")
        isIndiv = arguments?.getBoolean("isIndiv")
        isWeek = arguments?.getBoolean("isWeek")
        isMonth = arguments?.getBoolean("isMonth")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        trackIndex?.let { index ->
            binding.statTrackview.bind(index)
            viewModel.bind(
                trackIndex = index,
                onVictoryClick = binding.highestVictory.clicks(),
                onDefeatClick = binding.loudestDefeat.clicks(),
                isIndiv = isIndiv,
                isWeek = isWeek,
                isMonth = isMonth,
                onDetailsClick = binding.showDetailsBtn.clicks()
            )
            viewModel.sharedStats.onEach { stats ->
                binding.progress.isVisible = false
                binding.emptyLayout.isVisible = stats.list.isEmpty()
                binding.mainLayout.isVisible = stats.list.isNotEmpty()
                binding.warPlayed.text = stats.trackPlayed.toString()
                binding.winText.text = stats.trackWon.toString()
                binding.tieText.text = stats.trackTie.toString()
                binding.loseText.text = stats.trackLoss.toString()
                binding.piechart.bind(stats.trackWon, stats.trackTie, stats.trackLoss)
                binding.shockCount.text = "${stats.shockCount}"
                binding.mapTeamAverage.text = stats.teamScore.trackScoreToDiff()
                val averageDiffColor = when  {
                    stats.teamScore.trackScoreToDiff().contains("-") -> R.color.lose
                    stats.teamScore.trackScoreToDiff().contains("+") -> R.color.green
                    else -> R.color.harmonia_dark
                }
                binding.mapTeamAverage.setTextColor(
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                        requireContext().getColor(averageDiffColor)
                    else ContextCompat.getColor(requireContext(), averageDiffColor)
                )
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
                viewModel.sharedDetailsClick
                    .filter { findNavController().currentDestination?.id == R.id.mapStatsFragment }
                    .onEach { findNavController().navigate(MapStatsFragmentDirections.toMapStatsDetails(index, stats.list.toTypedArray(), isIndiv.isTrue)) }
                    .launchIn(lifecycleScope)
            }.launchIn(lifecycleScope)

            viewModel.sharedMapClick
                .filter { findNavController().currentDestination?.id == R.id.mapStatsFragment }
                .onEach { findNavController().navigate(MapStatsFragmentDirections.toTrackDetails(it.war.war, it.warTrack.track)) }
                .launchIn(lifecycleScope)

        }
    }

}