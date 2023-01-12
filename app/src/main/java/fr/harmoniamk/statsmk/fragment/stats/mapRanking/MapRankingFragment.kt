package fr.harmoniamk.statsmk.fragment.stats.mapRanking

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.firebase.database.core.Context
import dagger.hilt.android.AndroidEntryPoint
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.databinding.FragmentMapRankingBinding
import fr.harmoniamk.statsmk.enums.TrackSortType
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
                binding.totalPlaySortButton.clicks().map { TrackSortType.TOTAL_PLAYED },
                binding.totalWinSortButton.clicks().map { TrackSortType.TOTAL_WIN },
                binding.winrateSortButton.clicks().map { TrackSortType.WINRATE },
                binding.avgDiffSortButton.clicks().map { TrackSortType.AVERAGE_DIFF },
            ).flattenMerge(),
            onSearch = binding.searchEt.onTextChanged(),
            onIndivStatsSelected = flowOf(binding.indivBtn.clicks().map { true }, binding.teamBtn.clicks().map { false }).flattenMerge()
        )
        viewModel.sharedMaps
            .onEach { mostPlayedAdapter.addTracks(it) }
            .launchIn(lifecycleScope)
        viewModel.sharedSortTypeSelected
            .onEach {
                updateSortButton(binding.totalPlaySortButton, it, TrackSortType.TOTAL_PLAYED)
                updateSortButton(binding.totalWinSortButton, it, TrackSortType.TOTAL_WIN)
                updateSortButton(binding.winrateSortButton, it, TrackSortType.WINRATE)
                updateSortButton(binding.avgDiffSortButton, it, TrackSortType.AVERAGE_DIFF)
            }.launchIn(lifecycleScope)
        viewModel.sharedGoToStats
            .filter { findNavController().currentDestination?.id == R.id.mapRankingFragment }
            .onEach {
                findNavController().navigate(MapRankingFragmentDirections.toMapStats(it.first, it.second))
            }
            .launchIn(lifecycleScope)
        viewModel.sharedIndivStatsEnabled
            .onEach {
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

    private fun updateSortButton(button: TextView, initialType: TrackSortType, targetType: TrackSortType) {
        button.background.mutate().setTint(ContextCompat.getColor(requireContext(), if (initialType == targetType) R.color.harmonia_dark else R.color.transparent_white))
        button.setTextColor(ContextCompat.getColor(requireContext(), if (initialType == targetType) R.color.white else R.color.harmonia_dark))
    }

}