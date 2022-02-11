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
import fr.harmoniamk.statsmk.features.quitWar.QuitWarDialogFragment
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.database.firebase.model.TOTAL_TRACKS
import fr.harmoniamk.statsmk.database.firebase.model.War
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
    private var war: War? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        war = arguments?.get("war") as? War
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = CurrentWarTrackAdapter()
        binding.currentTracksRv.adapter = adapter
        viewModel.bind(war, requireActivity().backPressedDispatcher(viewLifecycleOwner), binding.nextTrackBtn.clicks())

        viewModel.sharedCurrentWar
            .onEach {
                warId = it.mid
                binding.warTitleTv.text = it.name
                binding.warDateTv.text = it.createdDate
                binding.currentWarTv.text = it.displayedState
                binding.scoreTv.text = it.displayedScore
                binding.diffScoreTv.text = it.displayedDiff
            }.launchIn(lifecycleScope)

        viewModel.sharedButtonVisible.onEach { binding.nextTrackBtn.isVisible = it }.launchIn(lifecycleScope)
        viewModel.sharedWaitingVisible.onEach { binding.waitingNextTrack.isVisible = it }.launchIn(lifecycleScope)

        viewModel.sharedBack
            .onEach {
                val dialog = QuitWarDialogFragment()
                viewModel.bindDialog(dialog.sharedWarLeft, dialog.sharedClose)
                if (!dialog.isAdded) dialog.show(childFragmentManager, null)
                viewModel.sharedCancel
                    .filter { findNavController().currentDestination?.id == R.id.currentWarFragment }
                    .onEach { dialog.dismiss() }
                    .launchIn(lifecycleScope)
            }.launchIn(lifecycleScope)

        viewModel.sharedQuit
            .filter { findNavController().currentDestination?.id == R.id.currentWarFragment }
            .onEach { findNavController().popBackStack() }
            .launchIn(lifecycleScope)

        viewModel.sharedSelectTrack
            .filter { findNavController().currentDestination?.id == R.id.currentWarFragment }
            .mapNotNull { warId }
            .onEach { findNavController().navigate(CurrentWarFragmentDirections.addTrack(it)) }
            .launchIn(lifecycleScope)

        viewModel.sharedGoToPos
            .filter { findNavController().currentDestination?.id == R.id.currentWarFragment }
            .onEach { findNavController().navigate(CurrentWarFragmentDirections.enterPositions(it.trackIndex ?: -1, warTrackId = it.mid)) }
            .launchIn(lifecycleScope)

        viewModel.sharedTracks
            .onEach {
                binding.playedLabel.isVisible = it.isNotEmpty()
                binding.connectedPlayers.isVisible = it.size < TOTAL_TRACKS
                adapter.addTracks(it)
            }
            .launchIn(lifecycleScope)

        viewModel.sharedPlayersConnected
            .onEach { binding.connectedPlayers.text = "Joueurs connectés: $it/6" }
            .launchIn(lifecycleScope)
    }

}