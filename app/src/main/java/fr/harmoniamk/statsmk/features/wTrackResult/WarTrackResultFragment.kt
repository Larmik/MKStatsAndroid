package fr.harmoniamk.statsmk.features.wTrackResult

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.databinding.FragmentResultWarTrackBinding
import fr.harmoniamk.statsmk.enums.Maps
import fr.harmoniamk.statsmk.extension.backPressedDispatcher
import fr.harmoniamk.statsmk.extension.clicks
import fr.harmoniamk.statsmk.features.quitWar.QuitWarDialogFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@FlowPreview
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class WarTrackResultFragment : Fragment(R.layout.fragment_result_war_track) {

    private val binding : FragmentResultWarTrackBinding by viewBinding()
    private val viewModel: WarTrackResultViewModel by viewModels()
    private var warTrackId: String? = null
    private var track: Int? = null
    private val dialog =
        QuitWarDialogFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        warTrackId = arguments?.getString("warTrackId")
        track = arguments?.getInt("track").takeIf { it != -1 }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = WarTrackResultAdapter()
        binding.resultRv.adapter = adapter
        track?.let {
            val map = Maps.values()[it]
            binding.trackIv.clipToOutline = true
            binding.trackIv.setImageResource(map.picture)
            binding.cupIv.setImageResource(map.cup.picture)
            binding.shortname.text = map.name
            binding.name.setText(map.label)
        }
        viewModel.bind(
            warTrackId = warTrackId,
            onBack = requireActivity().backPressedDispatcher(viewLifecycleOwner),
            onBackDialog = dialog.sharedClose,
            onValid = binding.validateBtn.clicks(),
            onQuit = dialog.sharedWarLeft)

        viewModel.sharedWarPos
            .onEach { adapter.addResults(it) }
            .launchIn(lifecycleScope)

        viewModel.sharedScore
            .onEach {
                binding.trackScore.text = it.displayedResult
                binding.trackDiff.text = it.displayedDiff
            }
            .launchIn(lifecycleScope)

        viewModel.sharedBack
            .onEach {
                if (!dialog.isAdded) dialog.show(childFragmentManager, null)
                viewModel.sharedCancel
                    .filter { findNavController().currentDestination?.id == R.id.warTrackResultFragment }
                    .onEach { dialog.dismiss() }
                    .launchIn(lifecycleScope)
            }.launchIn(lifecycleScope)

        viewModel.sharedQuit
            .filter { findNavController().currentDestination?.id == R.id.warTrackResultFragment }
            .onEach { findNavController().popBackStack() }
            .launchIn(lifecycleScope)

        viewModel.sharedBackToCurrent
            .filter { findNavController().currentDestination?.id == R.id.warTrackResultFragment }
            .onEach { findNavController().navigate(WarTrackResultFragmentDirections.backToCurrent()) }
            .launchIn(lifecycleScope)

        viewModel.sharedGoToWarResume
            .filter { findNavController().currentDestination?.id == R.id.warTrackResultFragment }
            .onEach { findNavController().navigate(WarTrackResultFragmentDirections.goToWarDetails(it)) }
            .launchIn(lifecycleScope)
    }

}