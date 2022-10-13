package fr.harmoniamk.statsmk.fragment.mapRanking

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.databinding.FragmentMapRankingBinding
import fr.harmoniamk.statsmk.enums.SortType
import fr.harmoniamk.statsmk.extension.clicks
import fr.harmoniamk.statsmk.extension.onTextChanged
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

@ExperimentalCoroutinesApi
@FlowPreview
@AndroidEntryPoint
class MapRankingFragment : Fragment(R.layout.fragment_map_ranking) {

    private val binding: FragmentMapRankingBinding by viewBinding()
    private val viewModel: MapRankingViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mostPlayedAdapter = MapRankingAdapter()
        binding.mostPlayedRv.adapter = mostPlayedAdapter
        viewModel.bind(
            onTrackClick = mostPlayedAdapter.sharedClick,
            onSortClick = flowOf(
                binding.totalPlaySortButton.clicks().map { SortType.TOTAL_PLAYED },
                binding.totalWinSortButton.clicks().map { SortType.TOTAL_WIN },
                binding.winrateSortButton.clicks().map { SortType.WINRATE },
                binding.avgDiffSortButton.clicks().map { SortType.AVERAGE_DIFF },
            ).flattenMerge(),
            onSearch = binding.searchEt.onTextChanged()
        )
        viewModel.sharedMaps
            .onEach { mostPlayedAdapter.addTracks(it) }
            .launchIn(lifecycleScope)
        viewModel.sharedSortTypeSelected
            .onEach {
                updateSortButton(binding.totalPlaySortButton, it, SortType.TOTAL_PLAYED)
                updateSortButton(binding.totalWinSortButton, it, SortType.TOTAL_WIN)
                updateSortButton(binding.winrateSortButton, it, SortType.WINRATE)
                updateSortButton(binding.avgDiffSortButton, it, SortType.AVERAGE_DIFF)
            }.launchIn(lifecycleScope)
        viewModel.sharedGoToStats
            .filter { findNavController().currentDestination?.id == R.id.mapRankingFragment }
            .onEach { findNavController().navigate(MapRankingFragmentDirections.toMapStats(it)) }
            .launchIn(lifecycleScope)
    }

    private fun updateSortButton(button: TextView, initialType: SortType, targetType: SortType) {
        button.background.mutate().setTint(ContextCompat.getColor(requireContext(), if (initialType == targetType) R.color.harmonia_dark else R.color.transparent_white))
        button.setTextColor(ContextCompat.getColor(requireContext(), if (initialType == targetType) R.color.white else R.color.harmonia_dark))

    }

}