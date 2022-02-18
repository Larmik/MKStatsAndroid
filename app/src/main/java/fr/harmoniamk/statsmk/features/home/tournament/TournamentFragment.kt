package fr.harmoniamk.statsmk.features.home.tournament

import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.database.room.model.Tournament
import fr.harmoniamk.statsmk.databinding.FragmentTournamentsBinding
import fr.harmoniamk.statsmk.extension.clicks
import fr.harmoniamk.statsmk.features.home.HomeFragmentDirections
import fr.harmoniamk.statsmk.features.home.tournament.adapter.BestTournamentAdapter
import fr.harmoniamk.statsmk.features.home.tournament.adapter.LastTournamentAdapter
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
            onClickTournament = flowOf(adapter.sharedItemClick, lastAdapter.sharedItemClick).flattenMerge()
        )
        viewModel.currentTournament
            .onEach { tm ->
                binding.currentTmLayout.isVisible = false
                binding.createTmLayout.isVisible = true
                tm?.let { bindCurrent(it) }
            }.launchIn(lifecycleScope)
        viewModel.sharedAdd
            .filter { findNavController().currentDestination?.id == R.id.homeFragment }
            .onEach { findNavController().navigate(HomeFragmentDirections.addFragment()) }
            .launchIn(lifecycleScope)
        viewModel.sharedLastTournaments
            .onEach {
                binding.welcomeTv.isVisible =
                    it.isNullOrEmpty() && !binding.currentTmLayout.isVisible
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                    binding.welcomeTv.text = Html.fromHtml(
                        requireContext().getString(R.string.welcome_message),
                        Html.FROM_HTML_MODE_LEGACY
                    )
                else binding.welcomeTv.text =
                    Html.fromHtml(requireContext().getString(R.string.welcome_message))
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

    private fun bindCurrent(current: Tournament) {
        binding.welcomeTv.isVisible = false
        binding.addTmBtn.isVisible = false
        binding.currentTmLayout.isVisible = true
        binding.nameTv.text = current.name
        binding.currentTmInfos.text = current.infos
        binding.currentTmScore.text = current.displayedScore
        binding.timeTv.text = current.updatedDate
        binding.currentTmCard.clicks()
            .filter { findNavController().currentDestination?.id == R.id.homeFragment }
            .onEach { findNavController().navigate(HomeFragmentDirections.gotoCurrent(current)) }
            .launchIn(lifecycleScope)
        viewModel.sharedRemainingTracks
            .onEach { binding.currentTmRemaining.text = current.displayedRemaining(it) }
            .launchIn(lifecycleScope)
    }

}