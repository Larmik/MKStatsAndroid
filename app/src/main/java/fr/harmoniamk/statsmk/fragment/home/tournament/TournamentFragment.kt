package fr.harmoniamk.statsmk.fragment.home.tournament

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
import fr.harmoniamk.statsmk.model.local.MKTournament
import fr.harmoniamk.statsmk.databinding.FragmentTournamentsBinding
import fr.harmoniamk.statsmk.extension.clicks
import fr.harmoniamk.statsmk.fragment.home.HomeFragmentDirections
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

@ExperimentalCoroutinesApi
@FlowPreview
@AndroidEntryPoint
class TournamentFragment : Fragment(R.layout.fragment_tournaments) {

    private val binding: FragmentTournamentsBinding by viewBinding()
    private val viewModel: TournamentViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = BestTournamentAdapter()
        val lastAdapter = LastTournamentAdapter()
        binding.lastTmRv.adapter = lastAdapter
        binding.bestTmRv.adapter = adapter
        viewModel.bind(
            addTournamentClick = binding.addTmBtn.clicks(),
            onClickMKTournament = flowOf(adapter.sharedItemClick, lastAdapter.sharedItemClick).flattenMerge()
        )
        lifecycleScope.launchWhenResumed {
            viewModel.currentTournament
                .onEach { bindCurrent(null) }
                .filterNotNull()
                .onEach { bindCurrent(it) }
                .launchIn(lifecycleScope)
        }
        viewModel.sharedAdd
            .filter { findNavController().currentDestination?.id == R.id.homeFragment }
            .onEach { findNavController().navigate(HomeFragmentDirections.addFragment()) }
            .launchIn(lifecycleScope)
        viewModel.sharedLastTournaments
            .onEach {
                binding.lastTmLayout.isVisible = it.isNotEmpty()
                lastAdapter.addTournaments(it)
            }.launchIn(lifecycleScope)
        viewModel.sharedBestTournaments
            .onEach {
                binding.bestTmLayout.isVisible = it.size >= 3
                adapter.addTournaments(it)
            }.launchIn(lifecycleScope)
        viewModel.sharedGoToTM
            .filter { findNavController().currentDestination?.id == R.id.homeFragment }
            .onEach { findNavController().navigate(HomeFragmentDirections.gotoCurrent(it)) }
            .launchIn(lifecycleScope)
    }

    private fun bindCurrent(current: MKTournament?) {
        binding.addTmBtn.isVisible = current == null
        binding.createTmLayout.isVisible = current == null
        binding.currentTmLayout.isVisible = current != null
        current?.let {
            binding.nameTv.text = it.name
            binding.currentTmInfos.text = it.infos
            binding.currentTmScore.text = it.displayedScore
            binding.timeTv.text = it.updatedDate
            binding.currentTmCard.clicks()
                .filter { findNavController().currentDestination?.id == R.id.homeFragment }
                .onEach { findNavController().navigate(HomeFragmentDirections.gotoCurrent(current)) }
                .launchIn(lifecycleScope)
            viewModel.sharedRemainingTracks
                .onEach { tracks -> binding.currentTmRemaining.text = it.displayedRemaining(tracks) }
                .launchIn(lifecycleScope)
        }

    }

}