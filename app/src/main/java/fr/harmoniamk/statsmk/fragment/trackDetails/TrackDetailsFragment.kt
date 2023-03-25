package fr.harmoniamk.statsmk.fragment.trackDetails

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.databinding.FragmentTrackDetailsBinding
import fr.harmoniamk.statsmk.extension.clicks
import fr.harmoniamk.statsmk.fragment.editWarPositions.EditWarPositionsFragment
import fr.harmoniamk.statsmk.fragment.editWarShocks.EditWarShocksFragment
import fr.harmoniamk.statsmk.fragment.editWarTrack.EditWarTrackFragment
import fr.harmoniamk.statsmk.fragment.warTrackResult.ShockAdapter
import fr.harmoniamk.statsmk.ui.TrackView
import fr.harmoniamk.statsmk.fragment.warTrackResult.WarTrackResultAdapter
import fr.harmoniamk.statsmk.model.firebase.NewWar
import fr.harmoniamk.statsmk.model.firebase.NewWarTrack
import fr.harmoniamk.statsmk.model.local.MKWarTrack
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@FlowPreview
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class TrackDetailsFragment : Fragment(R.layout.fragment_track_details) {

    private val binding: FragmentTrackDetailsBinding by viewBinding()
    private val viewModel: TrackDetailsViewModel by viewModels()
    private var war: NewWar? = null
    private var index: Int = 0
    private var warTrack: NewWarTrack? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        war = arguments?.get("war") as? NewWar
        warTrack = arguments?.get("warTrack") as? NewWarTrack
        index = arguments?.getInt("index") ?: 0
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = WarTrackResultAdapter(shockLayoutVisible = false)
        val shockAdapter = ShockAdapter()
        binding.resultRv.adapter = adapter
        binding.shockRv.adapter = shockAdapter
        war?.let { war ->
        val item = MKWarTrack(warTrack ?: war.warTracks?.get(index))
        val trackView = TrackView(requireContext())
        trackView.bind(item.track?.trackIndex)
        binding.trackView.addView(trackView)
        binding.subtitle.text = warTrack?.let { war.createdDate } ?: "Course ${index + 1}/12"
        binding.trackScore.text = item.displayedResult
        binding.trackDiff.text = item.displayedDiff
        var textColor = when  {
            item.displayedDiff.contains("-") -> R.color.lose
            item.displayedDiff.contains("+") -> R.color.green
            else -> R.color.harmonia_dark
        }
        binding.trackDiff.setTextColor(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                requireContext().getColor(textColor)
            else ContextCompat.getColor(requireContext(), textColor)
        )

        viewModel.bind(
            war = war,
            warTrack = warTrack,
            index = index,
            onEditTrack = binding.editTrackBtn.clicks(),
            onEditPositions = binding.resetPositionsBtn.clicks(),
            onEditShocks = binding.resetShocksBtn.clicks()
        )
            viewModel.sharedWarName
                .onEach {
                    binding.title.text = it
                }.launchIn(lifecycleScope)

        viewModel.sharedPositions
            .distinctUntilChanged()
            .onEach { adapter.addResults(it) }
            .launchIn(lifecycleScope)

            viewModel.sharedShocks
                .distinctUntilChanged()
                .onEach{
                    shockAdapter.addItems(it)
                    warTrack?.shocks = it.map { it.second }
                }
                .launchIn(lifecycleScope)

        viewModel.sharedEditTrackClick
            .onEach {
                val dialog = EditWarTrackFragment(war, index)
                if (!dialog.isAdded)
                    dialog.show(childFragmentManager, null)
                dialog.onDismiss
                    .onEach {
                        dialog.dismiss()
                        trackView.bind(it)
                    }.launchIn(lifecycleScope)
            }.launchIn(lifecycleScope)

        viewModel.sharedEditPositionsClick
            .onEach {
                val dialog = EditWarPositionsFragment(war, index)
                if (!dialog.isAdded)
                    dialog.show(childFragmentManager, null)
                dialog.onDismiss
                    .onEach {
                        dialog.dismiss()
                        viewModel.refresh(it)
                    }.launchIn(lifecycleScope)
            }.launchIn(lifecycleScope)

        viewModel.sharedEditShocksClick
            .onEach {
                val dialog = EditWarShocksFragment(war, warTrack)
                if (!dialog.isAdded)
                    dialog.show(childFragmentManager, null)
                dialog.onDismiss
                    .onEach {
                        dialog.dismiss()
                        viewModel.refresh(it)
                    }.launchIn(lifecycleScope)
            }.launchIn(lifecycleScope)

        viewModel.sharedTrackRefreshed
            .onEach {
                binding.trackScore.text = it.displayedResult
                binding.trackDiff.text = it.displayedDiff
                textColor = when  {
                    it.displayedDiff.contains("-") -> R.color.lose
                    it.displayedDiff.contains("+") -> R.color.green
                    else -> R.color.harmonia_dark
                }
                binding.trackDiff.setTextColor(
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                        requireContext().getColor(textColor)
                    else ContextCompat.getColor(requireContext(), textColor)
                )
            }.launchIn(lifecycleScope)

        viewModel.sharedButtonsVisible
            .onEach {
                binding.editTrackLayout.isVisible = it
            }.launchIn(lifecycleScope)

        }
    }
}

