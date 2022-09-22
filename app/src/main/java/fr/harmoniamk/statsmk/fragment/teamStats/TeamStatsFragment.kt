package fr.harmoniamk.statsmk.fragment.teamStats

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
import fr.harmoniamk.statsmk.databinding.FragmentTeamStatsBinding
import fr.harmoniamk.statsmk.extension.clicks
import fr.harmoniamk.statsmk.extension.positionColor
import fr.harmoniamk.statsmk.fragment.indivStats.IndivStatsFragmentDirections
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@ExperimentalCoroutinesApi
@FlowPreview
@AndroidEntryPoint
class TeamStatsFragment : Fragment(R.layout.fragment_team_stats) {

    private val binding: FragmentTeamStatsBinding by viewBinding()
    private val viewModel: TeamStatsViewModel by viewModels()

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
            binding.mapAverage.text = it.averageMapPoints.toString()
            binding.bestTrackview.bind(it.bestMap)
            binding.worstTrackview.bind(it.worstMap)
            binding.mostPlayedTrackview.bind(it.mostPlayedMap)
            binding.lessPlayedTrackview.bind(it.lessPlayedMap)
            binding.highestVictory.bind(it.warStats.highestVictory)
            binding.highestDefeat.bind(it.warStats.loudestDefeat)
            binding.mostPlayedTeam.text = it.mostPlayedTeam?.teamName
            binding.mostPlayedTeamTotal.text = it.mostPlayedTeam?.totalPlayedLabel
        }.launchIn(lifecycleScope)

        viewModel.sharedMostDefeatedTeam
            .onEach {
                binding.mostDefeatedTeam.text = it?.first
                binding.mostDefeatedTeamTotal.text = "${it?.second} victoires"
            }.launchIn(lifecycleScope)
        viewModel.sharedLessDefeatedTeam
            .onEach {
                binding.lessDefeatedTeam.text = it?.first
                binding.lessDefeatedTeamTotal.text = "${it?.second} défaites"
            }.launchIn(lifecycleScope)
        viewModel.sharedTrackClick
            .filter { findNavController().currentDestination?.id == R.id.teamStatsFragment }
            .onEach { findNavController().navigate(TeamStatsFragmentDirections.toMapStats(it)) }
            .launchIn(lifecycleScope)
        viewModel.sharedWarClick
            .filter { findNavController().currentDestination?.id == R.id.teamStatsFragment }
            .onEach { findNavController().navigate(TeamStatsFragmentDirections.goToWarDetails(it)) }
            .launchIn(lifecycleScope)
    }


}