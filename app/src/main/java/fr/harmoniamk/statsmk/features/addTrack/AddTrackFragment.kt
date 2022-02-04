package fr.harmoniamk.statsmk.features.addTrack

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.databinding.FragmentAddTrackBinding
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@FlowPreview
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class AddTrackFragment : Fragment(R.layout.fragment_add_track) {

    private val binding: FragmentAddTrackBinding by viewBinding()
    private val viewModel: AddTrackViewModel by viewModels()
    private var tmId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tmId = arguments?.getInt("tmId")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = AddTrackPagerAdapter(requireActivity())
        binding.addTrackVp.isUserInputEnabled = false
        binding.addTrackVp.adapter = adapter
        binding.addTrackVp.currentItem = 0
        tmId?.let {
            viewModel.bind(
                tournamentId = it,
                onTrackAdded = adapter.onMap,
                onPositionAdded = adapter.onPosition
            )
            viewModel.sharedGoToPos
                .onEach { binding.addTrackVp.setCurrentItem(1, true) }
                .launchIn(lifecycleScope)
            viewModel.validateTrack
                .filter { findNavController().currentDestination?.id == R.id.addTrackFragment }
                .onEach { findNavController().popBackStack() }
                .launchIn(lifecycleScope)
        }
    }

}