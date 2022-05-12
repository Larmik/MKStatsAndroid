package fr.harmoniamk.statsmk.fragment.allWars

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.databinding.FragmentAllWarsBinding
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

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
        viewModel.bind(adapter.sharedItemClick)
        viewModel.sharedWars.onEach { adapter.addWars(it) }.launchIn(lifecycleScope)
        viewModel.sharedTeamName.onEach { binding.teamName.text = it }.launchIn(lifecycleScope)
        viewModel.sharedWarClick
            .filter { findNavController().currentDestination?.id == R.id.allWarsFragment }
            .onEach { findNavController().navigate(AllWarsFragmentDirections.goToWarDetails(it)) }
            .launchIn(lifecycleScope)
    }

}