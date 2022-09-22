package fr.harmoniamk.statsmk.fragment.indivStats

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

        viewModel.sharedStats.onEach {
            binding.progress.isVisible = false
            binding.mainLayout.isVisible = true
            binding.warPlayed.text = it.warStats.warsPlayed.toString()
            binding.warsWon.text = it.warStats.warsWon.toString()
            binding.winrate.text = it.warStats.winRate
            binding.totalAverage.text = it.averagePoints.toString()
            binding.mapAverage.text = it.averagePlayerMapPoints.toString()
            binding.mapAverage.setTextColor(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    requireContext().getColor(it.averagePlayerMapPoints.positionColor())
                else ContextCompat.getColor(requireContext(), it.averagePlayerMapPoints.positionColor())
            )
            binding.highestScore.text = it.highestScore?.score.toString()
            binding.highestScoreWarName.text = it.highestScore?.opponentLabel
            binding.highestScoreWarDate.text = it.highestScore?.war?.war?.createdDate
            binding.lowestScore.text = it.lowestScore?.score.toString()
            binding.lowestScoreWarName.text = it.lowestScore?.opponentLabel
            binding.lowestScoreWarDate.text = it.lowestScore?.war?.war?.createdDate
            binding.bestTrackview.bind(it.bestMap, shouldDisplayPosition = true)
            binding.worstTrackview.bind(it.worstMap, shouldDisplayPosition = true)
            binding.mostPlayedTrackview.bind(it.mostPlayedMap, shouldDisplayPosition = true)
            binding.lessPlayedTrackview.bind(it.lessPlayedMap, shouldDisplayPosition = true)
            binding.mostPlayedTeam.text = it.mostPlayedTeam?.teamName
            binding.mostPlayedTeamTotal.text = it.mostPlayedTeam?.totalPlayedLabel

            it.warStats.highestVictory?.let {
                binding.noVictory.isVisible = false
                binding.highestVictory.isVisible = true
                binding.highestVictory.bind(it)
            }
            it.warStats.loudestDefeat?.let {
                binding.noDefeat.isVisible = false
                binding.highestDefeat.isVisible = true
                binding.highestDefeat.bind(it)

            }
        }.launchIn(lifecycleScope)

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