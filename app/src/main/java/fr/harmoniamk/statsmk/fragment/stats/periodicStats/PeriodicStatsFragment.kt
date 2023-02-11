package fr.harmoniamk.statsmk.fragment.stats.periodicStats

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
import fr.harmoniamk.statsmk.databinding.FragmentPeriodicStatsBinding
import fr.harmoniamk.statsmk.extension.clicks
import fr.harmoniamk.statsmk.fragment.stats.teamStats.TeamStatsFragmentDirections
import fr.harmoniamk.statsmk.model.local.MKWar
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

@ExperimentalCoroutinesApi
@FlowPreview
@AndroidEntryPoint
class PeriodicStatsFragment : Fragment(R.layout.fragment_periodic_stats) {

    private val binding: FragmentPeriodicStatsBinding by viewBinding()
    private val viewModel: PeriodicStatsViewModel by viewModels()

    private var list: List<MKWar>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        list = (arguments?.get("wars") as? Array<MKWar>)?.toList()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.bind(
            list = list,
            onBestClick = binding.bestTrackview.clicks(),
            onWorstClick = binding.worstTrackview.clicks(),
            onMostPlayedClick = binding.mostPlayedTrackview.clicks(),
            onVictoryClick = binding.highestVictory.clicks(),
            onDefeatClick = binding.highestDefeat.clicks(),
            onWeekStatsSelected = flowOf(binding.weekBtn.clicks().map { true }, binding.monthBtn.clicks().map { false }).flattenMerge()
        )
        binding.highestDefeat.clipToOutline = true
        binding.highestVictory.clipToOutline = true

        viewModel.sharedStats.onEach {
            binding.progress.isVisible = false
            binding.mainLayout.isVisible = true
            binding.piechart.bind(it.warStats.warsWon, it.warStats.warsTied, it.warStats.warsLoss)
            binding.warPlayed.text = it.warStats.warsPlayed.toString()
            binding.winText.text = it.warStats.warsWon.toString()
            binding.tieText.text = it.warStats.warsTied.toString()
            binding.loseText.text = it.warStats.warsLoss.toString()
            binding.totalAverage.text = it.averagePointsLabel
            binding.mapAverage.text = it.averageMapPointsLabel
            binding.bestTrackview.bind(it.bestMap)
            binding.worstTrackview.bind(it.worstMap)
            binding.mostPlayedTrackview.bind(it.mostPlayedMap)
            binding.highestVictory.bind(it.warStats.highestVictory)
            binding.highestDefeat.bind(it.warStats.loudestDefeat)
            binding.mostPlayedTeam.text = it.mostPlayedTeam?.teamName
            binding.mostPlayedTeamTotal.text = it.mostPlayedTeam?.totalPlayedLabel
            binding.mapsWon.text = it.mapsWon

            val averageWarColor = when  {
                it.averagePointsLabel.contains("-") -> R.color.lose
                it.averagePointsLabel.contains("+") -> R.color.green
                else -> R.color.harmonia_dark
            }
            val averageMapColor = when  {
                it.averageMapPointsLabel.contains("-") -> R.color.lose
                it.averageMapPointsLabel.contains("+") -> R.color.green
                else -> R.color.harmonia_dark
            }
            binding.totalAverage.setTextColor(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    requireContext().getColor(averageWarColor)
                else ContextCompat.getColor(requireContext(), averageWarColor)
            )
            binding.mapAverage.setTextColor(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    requireContext().getColor(averageMapColor)
                else ContextCompat.getColor(requireContext(), averageMapColor)
            )
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
        viewModel.sharedWeekStatsEnabled
            .onEach {
                binding.title.text = when (it) {
                    true -> "Statistiques hebdomadaires"
                    else -> "Statistiques mensuelles"
                }
                binding.weekBtn.setBackgroundColor(ContextCompat.getColor(requireContext(),
                    when (it) {
                        true -> R.color.transparent_white
                        else -> R.color.transparent
                    })

                )
                binding.monthBtn.setBackgroundColor(ContextCompat.getColor(requireContext(),
                    when (it) {
                        false -> R.color.transparent_white
                        else -> R.color.transparent
                    })

                )
            }.launchIn(lifecycleScope)
    }

}