package fr.harmoniamk.statsmk.features.currentTournament.fragment

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.features.currentTournament.CurrentTrackAdapter
import fr.harmoniamk.statsmk.database.model.Tournament
import fr.harmoniamk.statsmk.databinding.FragmentCurrentTournamentBinding
import fr.harmoniamk.statsmk.extension.clicks
import fr.harmoniamk.statsmk.features.currentTournament.viewmodel.CurrentTournamentViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive

@ExperimentalCoroutinesApi
@FlowPreview
@AndroidEntryPoint
class CurrentTournamentFragment : Fragment(R.layout.fragment_current_tournament) {

    private val binding: FragmentCurrentTournamentBinding by viewBinding()
    private val viewModel: CurrentTournamentViewModel by viewModels()
    private var tournament: Tournament? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tournament = arguments?.get("tournament") as? Tournament
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = CurrentTrackAdapter()
        binding.currentTracksRv.adapter = adapter
        tournament?.let { tm ->
            binding.tmTitleTv.text = tm.name
            binding.tmDateTv.text = tm.createdDate
            binding.tmInfosTv.text = tm.infos
            binding.scoreTv.text = tm.points.toString()

            viewModel.bind(
                tmId = tm.mid,
                onAddTrackClick = binding.nextTrackBtn.clicks(),
                onCancel = binding.stopBtn.clicks(),
                onTrackEdit = adapter.sharedTrackToEdit
            )

            viewModel.sharedAddTrack
                .filter { findNavController().currentDestination?.id == R.id.currentTournamentFragment }
                .onEach { findNavController().navigate(CurrentTournamentFragmentDirections.addTrack(tm.mid)) }
                .launchIn(lifecycleScope)

            viewModel.sharedTracks
                .filter { lifecycle.currentState == Lifecycle.State.RESUMED && lifecycleScope.isActive }
                .onEach {
                    val isOver = it.size == tm.trackCount
                    binding.currentTmTv.text = tm.displayedState(it.size)
                    binding.nextTrackBtn.isVisible = !isOver
                    binding.stopBtn.isVisible = !isOver
                    binding.playedLabel.isVisible = it.isNotEmpty()
                    adapter.addTracks(it)
                }.launchIn(lifecycleScope)

            viewModel.sharedScore
                .filter { lifecycle.currentState == Lifecycle.State.RESUMED && lifecycleScope.isActive }
                .onEach { binding.scoreTv.text = it.toString() }
                .launchIn(lifecycleScope)
            viewModel.sharedCancel
                .onEach {
                    val deleteFragment = DeleteTournamentDialogFragment(tm)
                    viewModel.bindDialog(deleteFragment.sharedTmDeleted, deleteFragment.sharedClose)
                    if (!deleteFragment.isAdded) deleteFragment.show(childFragmentManager, null)
                    viewModel.sharedBack
                        .filter { findNavController().currentDestination?.id == R.id.currentTournamentFragment }
                        .onEach { deleteFragment.dismiss() }
                        .launchIn(lifecycleScope)
                }.launchIn(lifecycleScope)
            viewModel.sharedQuit
                .filter { findNavController().currentDestination?.id == R.id.currentTournamentFragment }
                .onEach { findNavController().popBackStack() }
                .launchIn(lifecycleScope)
            viewModel.sharedEditTrack
                .filter { !tm.isOver }
                .filter { findNavController().currentDestination?.id == R.id.currentTournamentFragment }
                .onEach { findNavController().navigate(CurrentTournamentFragmentDirections.editTrack(track = it)) }
                .launchIn(lifecycleScope)
        }

    }

}