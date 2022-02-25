package fr.harmoniamk.statsmk.features.editTrack.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.database.model.PlayedTrack
import fr.harmoniamk.statsmk.databinding.FragmentEditTrackBinding
import fr.harmoniamk.statsmk.features.editTrack.EditTrackPagerAdapter
import fr.harmoniamk.statsmk.features.editTrack.viewmodel.EditTrackViewModel
import fr.harmoniamk.statsmk.extension.backPressedDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@FlowPreview
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class EditTrackFragment : Fragment(R.layout.fragment_edit_track) {

    private val binding: FragmentEditTrackBinding by viewBinding()
    private val viewModel: EditTrackViewModel by viewModels()
    private var track: PlayedTrack? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        track = arguments?.get("track") as? PlayedTrack
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        track?.let {
            val adapter = EditTrackPagerAdapter(it.mid, requireActivity())
            binding.editTrackVp.isUserInputEnabled = false
            binding.editTrackVp.adapter = adapter
            binding.editTrackVp.currentItem = 0
            viewModel.bind(
                track = it,
                onTrackAdded = adapter.onMapEdit,
                onPositionAdded = adapter.onPositionEdit,
                onTrackClick = adapter.onMapClick,
                onPositionClick = adapter.onPositionClick,
                onBack = requireActivity().backPressedDispatcher(viewLifecycleOwner)
            )
            viewModel.sharedGoToTrackEdit
                .onEach { binding.editTrackVp.setCurrentItem(1, true) }
                .launchIn(lifecycleScope)
            viewModel.sharedGoToPos
                .onEach { binding.editTrackVp.setCurrentItem(2, true) }
                .launchIn(lifecycleScope)
            viewModel.validateTrack
                .onEach { binding.editTrackVp.setCurrentItem(0, true) }
                .launchIn(lifecycleScope)
            viewModel.sharedBack
                .filter { findNavController().currentDestination?.id == R.id.editTrackFragment }
                .onEach {
                    if (binding.editTrackVp.currentItem != 0) binding.editTrackVp.currentItem = 0
                    else findNavController().popBackStack()
                }.launchIn(lifecycleScope)
        }
    }
}