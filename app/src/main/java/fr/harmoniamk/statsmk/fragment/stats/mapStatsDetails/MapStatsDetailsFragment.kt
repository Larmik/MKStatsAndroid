package fr.harmoniamk.statsmk.fragment.stats.mapStatsDetails

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
import fr.harmoniamk.statsmk.databinding.FragmentMapStatsDetailsBinding
import fr.harmoniamk.statsmk.enums.WarFilterType
import fr.harmoniamk.statsmk.enums.WarSortType
import fr.harmoniamk.statsmk.extension.clicks
import fr.harmoniamk.statsmk.extension.isTrue
import fr.harmoniamk.statsmk.fragment.stats.mapStats.MapStatsAdapter
import fr.harmoniamk.statsmk.fragment.stats.mapStats.MapStatsFragmentDirections
import fr.harmoniamk.statsmk.model.local.MapDetails
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

@FlowPreview
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class MapStatsDetailsFragment : Fragment(R.layout.fragment_map_stats_details) {

    private val binding: FragmentMapStatsDetailsBinding by viewBinding()
    private val viewModel: MapStatsDetailsViewModel by viewModels()
    private var trackIndex: Int? = null
    private val mapDetails = mutableListOf<MapDetails>()
    private var isIndiv: Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        trackIndex = arguments?.getInt("trackId")
        isIndiv = arguments?.getBoolean("isIndiv")
        (arguments?.get("mapDetails") as? Array<out MapDetails>)?.let {
            mapDetails.addAll(it)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isIndiv.takeIf { it.isTrue }?.let {
            binding.scoreSortButton.text = "Position"
            binding.playFilterButton.visibility = View.INVISIBLE
        }
        trackIndex?.let { index ->
            val adapter = MapStatsAdapter()
            binding.statTrackview.bind(index)
            binding.mapDetailsRv.adapter = adapter
            adapter.addTracks(mapDetails)
            viewModel.bind(
                details = mapDetails,
                isIndiv = isIndiv.isTrue,
                onSortClick = flowOf(
                    binding.dateSortButton.clicks().map { WarSortType.DATE },
                    binding.teamSortButton.clicks().map { WarSortType.TEAM },
                    binding.scoreSortButton.clicks().map { WarSortType.SCORE },
                ).flattenMerge(),
                onFilterClick = flowOf(
                    binding.periodFilterButton.clicks().map { WarFilterType.WEEK },
                    binding.officialFilterButton.clicks().map { WarFilterType.OFFICIAL },
                    binding.playFilterButton.clicks().map { WarFilterType.PLAY }
                ).flattenMerge(),
                onItemClick = adapter.onMapClick
            )
            viewModel.sharedTracks.onEach {
                adapter.addTracks(it)
            }.launchIn(lifecycleScope)
            viewModel.sharedSortTypeSelected
                .onEach {
                    updateSortButton(binding.dateSortButton, it, WarSortType.DATE)
                    updateSortButton(binding.teamSortButton, it, WarSortType.TEAM)
                    updateSortButton(binding.scoreSortButton, it, WarSortType.SCORE)
                }.launchIn(lifecycleScope)
            viewModel.sharedFilterList
                .onEach {
                    updateFilterButton(binding.playFilterButton, WarFilterType.PLAY, it)
                    updateFilterButton(binding.officialFilterButton, WarFilterType.OFFICIAL, it)
                    updateFilterButton(binding.periodFilterButton, WarFilterType.WEEK, it)
                }.launchIn(lifecycleScope)
            viewModel.sharedTrackClick
                .filter { findNavController().currentDestination?.id == R.id.mapStatsDetailsFragment }
                .onEach { findNavController().navigate(MapStatsFragmentDirections.toTrackDetails(it.war.war, it.warTrack.track)) }
                .launchIn(lifecycleScope)
        }
    }

    private fun updateSortButton(
        button: TextView,
        initialType: WarSortType,
        targetType: WarSortType
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

    private fun updateFilterButton(
        button: TextView,
        initialType: WarFilterType,
        list: List<WarFilterType>
    ) {
        button.background.mutate().setTint(
            ContextCompat.getColor(
                requireContext(),
                if (list.contains(initialType)) R.color.harmonia_dark else R.color.transparent_white
            )
        )
        button.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                if (list.contains(initialType)) R.color.white else R.color.harmonia_dark
            )
        )
    }

}