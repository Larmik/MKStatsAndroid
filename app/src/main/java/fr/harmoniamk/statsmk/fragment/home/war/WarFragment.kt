package fr.harmoniamk.statsmk.fragment.home.war

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
import fr.harmoniamk.statsmk.databinding.FragmentWarBinding
import fr.harmoniamk.statsmk.extension.clicks
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import fr.harmoniamk.statsmk.extension.onTextChanged
import fr.harmoniamk.statsmk.fragment.home.HomeFragmentDirections
import fr.harmoniamk.statsmk.fragment.manageTeams.AddTeamFragment
import kotlinx.coroutines.flow.*

@FlowPreview
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class WarFragment : Fragment(R.layout.fragment_war) {

    private val binding: FragmentWarBinding by viewBinding()
    private val viewModel: WarViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val lastAdapter = LastWarAdapter()

        lifecycleScope.launchWhenResumed {
            binding.lastWarRv.adapter = lastAdapter
            viewModel.bind(
                onCreateWar = binding.createWarBtn.clicks(),
                onCurrentWarClick = binding.currentWarCard.clicks(),
                onWarClick = lastAdapter.sharedItemClick,
                onCreateTeam = binding.createTeamBtn.clicks()
            )

            viewModel.sharedHasTeam
                .onEach {
                    binding.noTeamLayout.isVisible = !it
                    binding.progress.isVisible = false
                    binding.mainWarLayout.isVisible = it
                }.launchIn(lifecycleScope)

            viewModel.sharedTeamName
                .onEach {
                    binding.progress.isVisible = false
                    binding.currentTeamTv.text = it
                    binding.createWarLayout.isVisible = true
                }
                .launchIn(lifecycleScope)

            viewModel.sharedCreateWar
                .filter { findNavController().currentDestination?.id == R.id.homeFragment }
                .onEach { findNavController().navigate(HomeFragmentDirections.createWar()) }
                .launchIn(lifecycleScope)

            viewModel.sharedCurrentWar
                .onEach {
                    binding.createWarLayout.isVisible = false
                    binding.currentWarLayout.isVisible = false
                    binding.createWarLayout.isVisible = it == null
                    binding.currentWarLayout.isVisible = it != null
                    binding.nameTv.text = it?.name
                    binding.timeTv.text = it?.war?.createdDate
                    binding.currentWarRemaining.text = it?.displayedState
                    binding.currentWarScore.text = it?.scoreLabel
                }.launchIn(lifecycleScope)

            viewModel.sharedCurrentWarClick
                .filter { findNavController().currentDestination?.id == R.id.homeFragment }
                .onEach { findNavController().navigate(HomeFragmentDirections.goToCurrentWar()) }
                .launchIn(lifecycleScope)

            viewModel.sharedLastWars
                .onEach {
                    binding.lastWarLayout.isVisible = it.isNotEmpty()
                    lastAdapter.addWars(it)
                }.launchIn(lifecycleScope)

            viewModel.sharedGoToWar
                .filter { findNavController().currentDestination?.id == R.id.homeFragment }
                .onEach { findNavController().navigate(HomeFragmentDirections.goToWarDetails(it)) }
                .launchIn(lifecycleScope)

            binding.allWarsBtn.clicks()
                .filter { findNavController().currentDestination?.id == R.id.homeFragment }
                .onEach { findNavController().navigate(HomeFragmentDirections.goToAllWars()) }
                .launchIn(lifecycleScope)

            viewModel.sharedButtonVisible
                .onEach {
                    binding.createWarBtn.isVisible = it
                    binding.warHostTv.isVisible = it
                }.launchIn(lifecycleScope)
        }

        lifecycleScope.launchWhenStarted {
            val dialog = AddTeamFragment(teamWithLeader = true)
            viewModel.bindAddTeamDialog(onTeamAdded = dialog.onTeamAdded)
            viewModel.sharedCreateTeamDialog.collect {
                when (it) {
                    true -> dialog.show(childFragmentManager, null)
                    else -> dialog.dismiss()
                }
            }
        }

    }



}