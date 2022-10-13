package fr.harmoniamk.statsmk.fragment.allWars

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
import fr.harmoniamk.statsmk.databinding.FragmentAllWarsBinding
import fr.harmoniamk.statsmk.enums.TrackSortType
import fr.harmoniamk.statsmk.enums.WarSortType
import fr.harmoniamk.statsmk.extension.clicks
import fr.harmoniamk.statsmk.extension.onTextChanged
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

@ExperimentalCoroutinesApi
@FlowPreview
@AndroidEntryPoint
class AllWarsFragment : Fragment(R.layout.fragment_all_wars) {

    private val binding: FragmentAllWarsBinding by viewBinding()
    private val viewModel: AllWarsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = AllWarsAdapter()
        binding.warRv.adapter = adapter
        viewModel.bind(adapter.sharedItemClick, binding.searchEt.onTextChanged(), flowOf(
            binding.dateSortButton.clicks().map { WarSortType.DATE },
            binding.teamSortButton.clicks().map { WarSortType.TEAM },
            binding.scoreSortButton.clicks().map { WarSortType.SCORE },
        ).flattenMerge())
        viewModel.sharedWars.onEach { adapter.addWars(it) }.launchIn(lifecycleScope)
        viewModel.sharedSortTypeSelected
            .onEach {
                updateSortButton(binding.dateSortButton, it, WarSortType.DATE)
                updateSortButton(binding.teamSortButton, it, WarSortType.TEAM)
                updateSortButton(binding.scoreSortButton, it, WarSortType.SCORE)
            }.launchIn(lifecycleScope)
        viewModel.sharedWarClick
            .filter { findNavController().currentDestination?.id == R.id.allWarsFragment }
            .onEach { findNavController().navigate(AllWarsFragmentDirections.goToWarDetails(it)) }
            .launchIn(lifecycleScope)
    }

    private fun updateSortButton(button: TextView, initialType: WarSortType, targetType: WarSortType) {
        button.background.mutate().setTint(ContextCompat.getColor(requireContext(), if (initialType == targetType) R.color.harmonia_dark else R.color.transparent_white))
        button.setTextColor(ContextCompat.getColor(requireContext(), if (initialType == targetType) R.color.white else R.color.harmonia_dark))

    }

}