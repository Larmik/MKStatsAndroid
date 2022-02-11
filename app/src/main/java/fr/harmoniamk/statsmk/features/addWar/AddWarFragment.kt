package fr.harmoniamk.statsmk.features.addWar

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.databinding.FragmentAddWarBinding
import fr.harmoniamk.statsmk.extension.clicks
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@FlowPreview
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class AddWarFragment : Fragment(R.layout.fragment_add_war) {

    private val binding: FragmentAddWarBinding by viewBinding()
    private val viewModel: AddWarViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = TeamListAdapter()
        binding.teamRv.adapter = adapter
        viewModel.bind(adapter.onTeamClick, binding.startWarBtn.clicks())

        viewModel.sharedTeams.onEach {
            adapter.addTeams(it)
        }.launchIn(lifecycleScope)

        viewModel.sharedTeamSelected.onEach {
            binding.createWarLayout.visibility = View.VISIBLE
            binding.startWarBtn.text = it
        }.launchIn(lifecycleScope)

        viewModel.sharedStarted
            .filter { findNavController().currentDestination?.id == R.id.addWarFragment }
            .onEach { findNavController().popBackStack() }
            .launchIn(lifecycleScope)
    }

}