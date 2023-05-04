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

    private val list = mutableListOf<MKWar>()
    private var isWeek = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       (arguments?.get("wars") as? Array<out MKWar>)?.let {
           list.addAll(it)
       }
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
            onWeekStatsSelected = flowOf(binding.weekBtn.clicks().map { true }, binding.monthBtn.clicks().map { false }).flattenMerge(),
            onMostDefeatedTeamClick = binding.mostDefeatedTeamLayout.clicks(),
            onMostPlayedTeamClick = binding.mostPlayedTeamLayout.clicks(),
            onLessDefeatedTeamClick = binding.lessDefeatedTeamLayout.clicks()
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
            binding.mostDefeatedTeam.text = it.mostDefeatedTeam?.teamName
            binding.mostDefeatedTeamTotal.text = "${it.mostDefeatedTeam?.totalPlayed} victoires"
            binding.lessDefeatedTeam.text = it.lessDefeatedTeam?.teamName
            binding.lessDefeatedTeamTotal.text = "${it.lessDefeatedTeam?.totalPlayed} défaites"
        }.launchIn(lifecycleScope)

        viewModel.sharedTrackClick
            .filter { findNavController().currentDestination?.id == R.id.periodicStatsFragment }
            .onEach { findNavController().navigate(PeriodicStatsFragmentDirections.toMapStats(it.second, isWeek = isWeek, isMonth = !isWeek, userId = it.first, wars = list.toTypedArray())) }
            .launchIn(lifecycleScope)
        viewModel.sharedWarClick
            .filter { findNavController().currentDestination?.id == R.id.periodicStatsFragment }
            .onEach { findNavController().navigate(PeriodicStatsFragmentDirections.goToWarDetails(it)) }
            .launchIn(lifecycleScope)
        viewModel.sharedWeekStatsEnabled
            .onEach {
                isWeek = it
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

        viewModel.sharedTeamClick
            .filter { findNavController().currentDestination?.id == R.id.periodicStatsFragment }
            .onEach { findNavController().navigate(PeriodicStatsFragmentDirections.toOpponentStats(it.second, userId = it.first)) }
            .launchIn(lifecycleScope)
    }

}