package fr.harmoniamk.statsmk.fragment.stats.playerStats

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
import fr.harmoniamk.statsmk.databinding.FragmentPlayerStatsBinding
import fr.harmoniamk.statsmk.extension.clicks
import fr.harmoniamk.statsmk.extension.positionColor
import fr.harmoniamk.statsmk.fragment.stats.indivStats.IndivStatsFragmentDirections
import fr.harmoniamk.statsmk.fragment.stats.playerRanking.PlayerRankingItemViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@ExperimentalCoroutinesApi
@FlowPreview
@AndroidEntryPoint
class PlayerStatsFragment : Fragment(R.layout.fragment_player_stats) {

    private val binding: FragmentPlayerStatsBinding by viewBinding()
    private val viewModel: PlayerStatsViewModel by viewModels()
    private var stats: PlayerRankingItemViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        stats = arguments?.get("stats") as? PlayerRankingItemViewModel
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        stats?.let {
            viewModel.bind(
                userStats = it,
                onBestClick = binding.bestTrackview.clicks(),
                onWorstClick = binding.worstTrackview.clicks(),
                onMostPlayedClick = binding.mostPlayedTrackview.clicks(),
                onVictoryClick = binding.highestVictory.clicks(),
                onDefeatClick = binding.highestDefeat.clicks(),
                onDetailsClick = binding.showDetailsBtn.clicks(),
                onHighestScore = binding.highestScoreLayout.clicks(),
                onLowestScore = binding.lowestScoreLayout.clicks(),
                onMostDefeatedTeamClick = binding.mostDefeatedTeamLayout.clicks(),
                onMostPlayedTeamClick = binding.mostPlayedTeamLayout.clicks(),
                onLessDefeatedTeamClick = binding.lessDefeatedTeamLayout.clicks()
            )
        }
        binding.highestDefeat.clipToOutline = true
        binding.highestVictory.clipToOutline = true
        binding.highestDefeat.isVisible = false
        binding.highestVictory.isVisible = false

        viewModel.sharedStats.onEach {
            binding.progress.isVisible = false
            binding.mainLayout.isVisible = true
            binding.playerName.text = it.user.name
            binding.piechart.bind(it.stats.warStats.warsWon, it.stats.warStats.warsTied, it.stats.warStats.warsLoss)
            binding.warPlayed.text = it.stats.warStats.warsPlayed.toString()
            binding.winText.text = it.stats.warStats.warsWon.toString()
            binding.tieText.text = it.stats.warStats.warsTied.toString()
            binding.loseText.text = it.stats.warStats.warsLoss.toString()
            binding.totalAverage.text = it.stats.averagePoints.toString()
            binding.mapAverage.text = it.stats.averagePlayerMapPoints.toString()
            binding.mapAverage.setTextColor(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    requireContext().getColor(it.stats.averagePlayerMapPoints.positionColor())
                else ContextCompat.getColor(requireContext(), it.stats.averagePlayerMapPoints.positionColor())
            )
            binding.highestScore.text = it.stats.highestScore?.score.toString()
            binding.highestScoreWarName.text = it.stats.highestScore?.opponentLabel
            binding.highestScoreWarDate.text = it.stats.highestScore?.war?.war?.createdDate
            binding.lowestScore.text = it.stats.lowestScore?.score.toString()
            binding.lowestScoreWarName.text = it.stats.lowestScore?.opponentLabel
            binding.lowestScoreWarDate.text = it.stats.lowestScore?.war?.war?.createdDate
            binding.bestTrackview.bind(it.stats.bestPlayerMap, shouldDisplayPosition = true)
            binding.worstTrackview.bind(it.stats.worstPlayerMap, shouldDisplayPosition = true)
            binding.mostPlayedTrackview.bind(it.stats.mostPlayedMap, shouldDisplayPosition = true)
            binding.mostPlayedTeam.text = it.stats.mostPlayedTeam?.teamName
            binding.mostPlayedTeamTotal.text = it.stats.mostPlayedTeam?.totalPlayedLabel
            binding.shockCount.text = it.stats.shockCount.toString()

            it.stats.warStats.highestVictory?.let {
                binding.noVictory.isVisible = false
                binding.highestVictory.isVisible = true
                binding.highestVictory.bind(it)
            }
            it.stats.warStats.loudestDefeat?.let {
                binding.noDefeat.isVisible = false
                binding.highestDefeat.isVisible = true
                binding.highestDefeat.bind(it)
            }
            when (it.stats.mostDefeatedTeam?.teamName) {
                "null" -> {
                    binding.mostDefeatedTeam.text = "Aucune"
                    binding.mostDefeatedTeamTotal.visibility = View.INVISIBLE
                }
                else -> {
                    binding.mostDefeatedTeam.text = it.stats.mostDefeatedTeam?.teamName
                    binding.mostDefeatedTeamTotal.text = "${it.stats.mostDefeatedTeam?.totalPlayed} victoires"
                }
            }
            when (it.stats.lessDefeatedTeam?.teamName) {
                "null" -> {
                    binding.lessDefeatedTeam.text = "Aucune"
                    binding.lessDefeatedTeamTotal.visibility = View.INVISIBLE
                }
                else -> {
                    binding.lessDefeatedTeam.text = it.stats.lessDefeatedTeam?.teamName
                    binding.lessDefeatedTeamTotal.text = "${it.stats.lessDefeatedTeam?.totalPlayed} défaites"
                }
            }
        }.launchIn(lifecycleScope)

        viewModel.sharedTrackClick
            .filter { findNavController().currentDestination?.id == R.id.playerStatsFragment }
            .onEach { findNavController().navigate(PlayerStatsFragmentDirections.toMapStats(trackId = it.second, userId = it.first, )) }
            .launchIn(lifecycleScope)
        viewModel.sharedWarClick
            .filter { findNavController().currentDestination?.id == R.id.playerStatsFragment }
            .onEach { findNavController().navigate(PlayerStatsFragmentDirections.goToWarDetails(it)) }
            .launchIn(lifecycleScope)
        viewModel.sharedGoToDetails
            .filter { findNavController().currentDestination?.id == R.id.playerStatsFragment }
            .onEach { findNavController().navigate(PlayerStatsFragmentDirections.goToWarList(it)) }
            .launchIn(lifecycleScope)
        viewModel.sharedTeamClick
            .filter { findNavController().currentDestination?.id == R.id.playerStatsFragment }
            .onEach { findNavController().navigate(PlayerStatsFragmentDirections.toOpponentStats(stats = it.second, userId = it.first, isIndiv = true)) }
            .launchIn(lifecycleScope)
    }

}