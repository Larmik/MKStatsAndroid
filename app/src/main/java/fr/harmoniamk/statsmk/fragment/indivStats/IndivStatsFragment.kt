package fr.harmoniamk.statsmk.fragment.indivStats

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.databinding.FragmentIndivStatsBinding
import fr.harmoniamk.statsmk.extension.clicks
import fr.harmoniamk.statsmk.extension.positionColor
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@ExperimentalCoroutinesApi
@FlowPreview
@AndroidEntryPoint
class IndivStatsFragment : Fragment(R.layout.fragment_indiv_stats) {

    private val binding: FragmentIndivStatsBinding by viewBinding()
    private val viewModel: IndivStatsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.bind(
            onBestClick = binding.bestTrackview.clicks(),
            onWorstClick = binding.worstTrackview.clicks(),
            onMostPlayedClick = binding.mostPlayedTrackview.clicks(),
            onLessPlayedClick = binding.lessPlayedTrackview.clicks(),
            onVictoryClick = binding.highestVictory.clicks(),
            onDefeatClick = binding.highestDefeat.clicks()
        )

        binding.highestDefeat.clipToOutline = true
        binding.highestVictory.clipToOutline = true

        viewModel.sharedWarsPlayed
            .onEach { binding.warPlayed.text = it.toString() }
            .launchIn(lifecycleScope)
        viewModel.sharedWarsWon
            .onEach { binding.warsWon.text = it.toString() }
            .launchIn(lifecycleScope)
        viewModel.sharedWinRate
            .onEach { binding.winrate.text = "$it %" }
            .launchIn(lifecycleScope)
        viewModel.sharedAveragePoints
            .onEach { binding.totalAverage.text = it.toString() }
            .launchIn(lifecycleScope)
        viewModel.sharedAverageMapPoints
            .onEach {
                binding.mapAverage.text = it.toString()
                binding.mapAverage.setTextColor(
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                        requireContext().getColor(it.positionColor())
                    else ContextCompat.getColor(requireContext(), it.positionColor())
                )
            }
            .launchIn(lifecycleScope)
        viewModel.sharedHighestScore
            .onEach {
                binding.highestScore.text = it?.second.toString()
                binding.highestScoreWarName.text = "vs ${it?.first?.name?.split('-')?.lastOrNull()?.trim()}"
                binding.highestScoreWarDate.text = it?.first?.war?.createdDate
            }.launchIn(lifecycleScope)
        viewModel.sharedLowestScore
            .onEach {
                binding.lowestScore.text = it?.second.toString()
                binding.lowestScoreWarName.text = "vs ${it?.first?.name?.split('-')?.lastOrNull()?.trim()}"
                binding.lowestScoreWarDate.text = it?.first?.war?.createdDate}
            .launchIn(lifecycleScope)
        viewModel.sharedBestMap
            .onEach { binding.bestTrackview.bind(it, shouldDisplayPosition = true) }
            .launchIn(lifecycleScope)
        viewModel.sharedWorstMap
            .onEach { binding.worstTrackview.bind(it, shouldDisplayPosition = true) }
            .launchIn(lifecycleScope)
        viewModel.sharedHighestVictory
            .onEach { binding.highestVictory.bind(it) }
            .launchIn(lifecycleScope)
        viewModel.sharedHighestDefeat
            .onEach { binding.highestDefeat.bind(it) }
            .launchIn(lifecycleScope)
        viewModel.sharedMostPlayedTeam
            .onEach {
                binding.mostPlayedTeam.text = it?.first
                binding.mostPlayedTeamTotal.text = "${it?.second} matchs joués"
            }.launchIn(lifecycleScope)
        viewModel.sharedMostPlayedMap
            .onEach { binding.mostPlayedTrackview.bind(it, shouldDisplayPosition = true) }
            .launchIn(lifecycleScope)
        viewModel.sharedLessPlayedMap
            .onEach { binding.lessPlayedTrackview.bind(it, shouldDisplayPosition = true) }
            .launchIn(lifecycleScope)
        viewModel.sharedTrackClick
            .filter { findNavController().currentDestination?.id == R.id.indivStatsFragment }
            .onEach { findNavController().navigate(IndivStatsFragmentDirections.toMapStats(it)) }
            .launchIn(lifecycleScope)
        viewModel.sharedWarClick
            .filter { findNavController().currentDestination?.id == R.id.indivStatsFragment }
            .onEach { findNavController().navigate(IndivStatsFragmentDirections.goToWarDetails(it)) }
            .launchIn(lifecycleScope)
    }

}