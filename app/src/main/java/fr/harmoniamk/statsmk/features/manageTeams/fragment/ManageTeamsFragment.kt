package fr.harmoniamk.statsmk.features.manageTeams.fragment

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.databinding.FragmentManageTeamsBinding
import fr.harmoniamk.statsmk.extension.clicks
import fr.harmoniamk.statsmk.features.addWar.adapter.TeamListAdapter
import fr.harmoniamk.statsmk.features.manageTeams.ManageTeamsAdapter
import fr.harmoniamk.statsmk.features.manageTeams.viewModel.ManageTeamsViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@FlowPreview
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class ManageTeamsFragment : Fragment(R.layout.fragment_manage_teams) {

    private val binding: FragmentManageTeamsBinding by viewBinding()
    private val viewModel: ManageTeamsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = ManageTeamsAdapter()
        binding.teamRv.adapter = adapter
        viewModel.bind(binding.addTeamBtn.clicks(), binding.quitTeamBtn.clicks(), adapter.sharedDelete)
        viewModel.sharedTeams
            .onEach {
                adapter.addTeams(it)
            }.launchIn(lifecycleScope)
        viewModel.sharedAddTeam
            .filter { findNavController().currentDestination?.id == R.id.manageTeamsFragment }
            .onEach { findNavController().navigate(ManageTeamsFragmentDirections.addTeam()) }
            .launchIn(lifecycleScope)

        viewModel.sharedTeamQuit
            .filter { findNavController().currentDestination?.id == R.id.manageTeamsFragment }
            .onEach { findNavController().popBackStack() }
            .launchIn(lifecycleScope)
        viewModel.sharedCurrentTeamName
            .onEach {
                binding.currentTeamLayout.isVisible = true
                binding.currentTeamTv.text = it
            }
            .launchIn(lifecycleScope)
    }
}