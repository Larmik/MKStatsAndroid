package fr.harmoniamk.statsmk.fragment.teamStats

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.databinding.FragmentTeamStatsBinding
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
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
        viewModel.bind()

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
            .onEach { binding.mapAverage.text = it.toString() }
            .launchIn(lifecycleScope)
        viewModel.sharedBestMap
            .onEach { binding.bestTrackview.bind(it) }
            .launchIn(lifecycleScope)
        viewModel.sharedWorstMap
            .onEach { binding.worstTrackview.bind(it) }
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
        viewModel.sharedMostPlayedMap
            .onEach { binding.mostPlayedTrackview.bind(it) }
            .launchIn(lifecycleScope)
        viewModel.sharedLessPlayedMap
            .onEach { binding.lessPlayedTrackview.bind(it) }
            .launchIn(lifecycleScope)
    }

}