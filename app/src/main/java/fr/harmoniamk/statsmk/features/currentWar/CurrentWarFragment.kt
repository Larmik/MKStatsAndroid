package fr.harmoniamk.statsmk.features.currentWar

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
import fr.harmoniamk.statsmk.databinding.FragmentCurrentWarBinding
import fr.harmoniamk.statsmk.extension.backPressedDispatcher
import fr.harmoniamk.statsmk.extension.clicks
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

@FlowPreview
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class CurrentWarFragment : Fragment(R.layout.fragment_current_war) {

    private val binding : FragmentCurrentWarBinding by viewBinding()
    private val viewModel: CurrentWarViewModel by viewModels()
    private var warId: String? = null
    private var warName: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = CurrentWarTrackAdapter()
        binding.currentTracksRv.adapter = adapter
        viewModel.bind(requireActivity().backPressedDispatcher(viewLifecycleOwner), binding.nextTrackBtn.clicks(), adapter.sharedClick)

        viewModel.sharedCurrentWar
            .onEach {
                warId = it.war?.mid
                warName = it.war?.name
                binding.warTitleTv.text = it.war?.name
                binding.warDateTv.text = it.war?.createdDate
                binding.currentWarTv.text = it.displayedState
                binding.scoreTv.text = it.displayedScore
                binding.diffScoreTv.text = it.displayedDiff
            }.launchIn(lifecycleScope)

        viewModel.sharedButtonVisible.onEach { binding.nextTrackBtn.isVisible = it }.launchIn(lifecycleScope)

        viewModel.sharedQuit
            .filter { findNavController().currentDestination?.id == R.id.currentWarFragment }
            .onEach { findNavController().popBackStack() }
            .launchIn(lifecycleScope)

        viewModel.sharedSelectTrack
            .filter { findNavController().currentDestination?.id == R.id.currentWarFragment }
            .mapNotNull { warId }
            .onEach { findNavController().navigate(CurrentWarFragmentDirections.addTrack(it)) }
            .launchIn(lifecycleScope)

        viewModel.sharedTracks
            .onEach {
                binding.playedLabel.isVisible = it.isNotEmpty()
                adapter.addTracks(it)
            }
            .launchIn(lifecycleScope)

        viewModel.sharedPlayers
            .onEach {
                it.forEachIndexed { index, s ->
                    when (index) {
                        0 -> binding.player1.text = s
                        1 -> binding.player2.text = s
                        2 -> binding.player3.text = s
                        3 -> binding.player4.text = s
                        4 -> binding.player5.text = s
                        else -> binding.player6.text = s
                    }
                }
            }.launchIn(lifecycleScope)

        viewModel.sharedTrackClick
            .filter { findNavController().currentDestination?.id == R.id.currentWarFragment }
            .onEach { findNavController().navigate(CurrentWarFragmentDirections.toTrackDetails(warTrack = it.second, warName = warName, number = it.first)) }
            .launchIn(lifecycleScope)
    }

}