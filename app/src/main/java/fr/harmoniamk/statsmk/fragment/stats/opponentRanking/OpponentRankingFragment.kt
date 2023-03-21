package fr.harmoniamk.statsmk.fragment.stats.opponentRanking

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.databinding.FragmentOpponentRankingBinding
import fr.harmoniamk.statsmk.enums.PlayerSortType
import fr.harmoniamk.statsmk.extension.clicks
import fr.harmoniamk.statsmk.extension.isTrue
import fr.harmoniamk.statsmk.extension.onTextChanged
import fr.harmoniamk.statsmk.model.firebase.Team
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

@FlowPreview
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class OpponentRankingFragment : Fragment(R.layout.fragment_opponent_ranking) {

    private val binding: FragmentOpponentRankingBinding by viewBinding()
    private val viewModel: OpponentRankingViewModel by viewModels()
    private val teams = mutableListOf<Team>()
    private var isIndiv = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (arguments?.get("teams") as? Array<out Team>)?.let {
            teams.addAll(it)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = OpponentRankingAdapter()
        lifecycleScope.launchWhenResumed {
            binding.searchEt.text.clear()

        }
        binding.mostPlayedRv.adapter = adapter
        viewModel.bind(
            list = teams,
            onTeamClick = adapter.sharedTeamSelected,
            onSortClick = flowOf(
                binding.nameSortButton.clicks().map { PlayerSortType.NAME },
                binding.winrateSortButton.clicks().map { PlayerSortType.WINRATE },
                binding.avgDiffSortButton.clicks().map { PlayerSortType.AVERAGE },
                binding.totalWinSortButton.clicks().map { PlayerSortType.TOTAL_WIN }
            ).flattenMerge(),
            onSearch = binding.searchEt.onTextChanged(),
            onIndivStatsSelected = flowOf(binding.indivBtn.clicks().map { true }, binding.teamBtn.clicks().map { false }).flattenMerge()
        )
        viewModel.sharedTeamList
            .onEach { adapter.addTeams(it) }
            .launchIn(lifecycleScope)

        viewModel.sharedGoToStats
            .filter { findNavController().currentDestination?.id == R.id.opponentRankingFragment }
            .onEach { findNavController().navigate(OpponentRankingFragmentDirections.toOpponentStats(stats = it.second, userId = it.first, isIndiv = isIndiv.isTrue)) }
            .launchIn(lifecycleScope)
        viewModel.sharedSortTypeSelected
            .onEach {
                updateSortButton(binding.nameSortButton, it, PlayerSortType.NAME)
                updateSortButton(binding.avgDiffSortButton, it, PlayerSortType.AVERAGE)
                updateSortButton(binding.totalWinSortButton, it, PlayerSortType.TOTAL_WIN)
                updateSortButton(binding.winrateSortButton, it, PlayerSortType.WINRATE)
            }.launchIn(lifecycleScope)

        viewModel.sharedLoading.onEach {
            binding.progress.isVisible = it
            binding.mostPlayedRv.isVisible = !it
        }.launchIn(lifecycleScope)

        viewModel.sharedIndivStatsEnabled
            .onEach {
                isIndiv = it
                binding.indivBtn.setBackgroundColor(ContextCompat.getColor(requireContext(),
                    when (it) {
                        true -> R.color.transparent_white
                        else -> R.color.transparent
                    })

                )
                binding.teamBtn.setBackgroundColor(ContextCompat.getColor(requireContext(),
                    when (it) {
                        false -> R.color.transparent_white
                        else -> R.color.transparent
                    })

                )
            }.launchIn(lifecycleScope)
    }

    private fun updateSortButton(
        button: TextView,
        initialType: PlayerSortType,
        targetType: PlayerSortType
    ) {
        button.background.mutate().setTint(
            ContextCompat.getColor(
                requireContext(),
                if (initialType == targetType) R.color.harmonia_dark else R.color.transparent_white
            )
        )
        button.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                if (initialType == targetType) R.color.white else R.color.harmonia_dark
            )
        )
    }
}