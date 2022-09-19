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
import kotlinx.coroutines.flow.*

@FlowPreview
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class WarFragment : Fragment(R.layout.fragment_war) {

    private val binding: FragmentWarBinding by viewBinding()
    private val viewModel: WarViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bestAdapter = BestWarAdapter()
        val lastAdapter = LastWarAdapter()

        lifecycleScope.launchWhenResumed {
            binding.bestWarRv.adapter = bestAdapter
            binding.lastWarRv.adapter = lastAdapter
            viewModel.bind(
                onCodeTeam = binding.teamCodeEt.onTextChanged(),
                onTeamClick = binding.nextBtn.clicks(),
                onCreateWar = binding.createWarBtn.clicks(),
                onCurrentWarClick = binding.currentWarCard.clicks(),
                onWarClick = flowOf(lastAdapter.sharedItemClick, bestAdapter.sharedItemClick).flattenMerge()
            )

            viewModel.sharedTeam
                .onEach { team ->
                    binding.nextBtn.visibility = View.INVISIBLE
                    team?.team?.let {
                        binding.nextBtn.visibility = View.VISIBLE
                        binding.nextBtn.text = team.integrationLabel
                    }
                }
                .launchIn(lifecycleScope)

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
            viewModel.sharedBestWars
                .onEach {
                    binding.bestWarLayout.isVisible = it.isNotEmpty()
                    bestAdapter.addWars(it)
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

    }



}