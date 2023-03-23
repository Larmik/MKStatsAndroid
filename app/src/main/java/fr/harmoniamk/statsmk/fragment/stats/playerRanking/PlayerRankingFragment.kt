package fr.harmoniamk.statsmk.fragment.stats.playerRanking

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
import fr.harmoniamk.statsmk.databinding.FragmentPlayerRankingBinding
import fr.harmoniamk.statsmk.enums.PlayerSortType
import fr.harmoniamk.statsmk.extension.clicks
import fr.harmoniamk.statsmk.extension.onTextChanged
import fr.harmoniamk.statsmk.model.firebase.User
import fr.harmoniamk.statsmk.model.local.MKWar
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

@ExperimentalCoroutinesApi
@FlowPreview
@AndroidEntryPoint
class PlayerRankingFragment : Fragment(R.layout.fragment_player_ranking) {

    private val binding: FragmentPlayerRankingBinding by viewBinding()
    private val viewModel: PlayerRankingViewModel by viewModels()
    private val players = mutableListOf<User>()
    private val wars = mutableListOf<MKWar>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (arguments?.get("players") as? Array<out User>)?.let {
            players.addAll(it)
        }
        (arguments?.get("wars") as? Array<out MKWar>)?.let {
            wars.addAll(it)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = PlayerRankingAdapter()
        binding.mostPlayedRv.adapter = adapter
        viewModel.bind(
            list = players,
            warList = wars,
            onPlayerClick = adapter.sharedUserSelected,
            onSortClick = flowOf(
                binding.nameSortButton.clicks().map { PlayerSortType.NAME },
                binding.winrateSortButton.clicks().map { PlayerSortType.WINRATE },
                binding.avgDiffSortButton.clicks().map { PlayerSortType.AVERAGE },
                binding.totalWinSortButton.clicks().map { PlayerSortType.TOTAL_WIN }
            ).flattenMerge(),
            onSearch = binding.searchEt.onTextChanged(),
        )
        viewModel.sharedUserList
            .onEach { adapter.addUsers(it) }
            .launchIn(lifecycleScope)

        viewModel.sharedGoToStats
            .filter { findNavController().currentDestination?.id == R.id.playerRankingFragment }
            .onEach { findNavController().navigate(PlayerRankingFragmentDirections.toPlayerStats(it.first, it.second.toTypedArray())) }
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
            binding.mainLayout.isVisible = !it
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