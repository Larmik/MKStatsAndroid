package fr.harmoniamk.statsmk.fragment.warDetails

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.databinding.FragmentWarDetailsBinding
import fr.harmoniamk.statsmk.extension.clicks
import fr.harmoniamk.statsmk.extension.isResumed
import fr.harmoniamk.statsmk.fragment.currentWar.CurrentPlayerAdapter
import fr.harmoniamk.statsmk.fragment.currentWar.CurrentWarTrackAdapter
import fr.harmoniamk.statsmk.fragment.currentWar.PenaltyAdapter
import fr.harmoniamk.statsmk.model.local.MKWar
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

@FlowPreview
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class WarDetailsFragment : Fragment(R.layout.fragment_war_details) {

    private val binding : FragmentWarDetailsBinding by viewBinding()
    private val viewModel: WarDetailsViewModel by viewModels()
    private var war: MKWar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        war = arguments?.get("war") as? MKWar
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = CurrentWarTrackAdapter()
        val penaltiesAdapter = PenaltyAdapter()
        val firstsPlayersAdapter = CurrentPlayerAdapter()
        val lastsPlayersAdapter = CurrentPlayerAdapter()
        binding.firstsPlayersRv.adapter = firstsPlayersAdapter
        binding.lastsPlayersRv.adapter = lastsPlayersAdapter
        binding.currentTracksRv.adapter = adapter
        binding.penaltiesRv.adapter = penaltiesAdapter
        war?.let { war ->
            binding.warDateTv.text = war.war?.createdDate
            binding.scoreTv.text = war.displayedScore
            binding.diffScoreTv.text = war.displayedDiff
            val textColor = when  {
                war.displayedDiff.contains("-") -> R.color.lose
                war.displayedDiff.contains("+") -> R.color.green
                else -> R.color.harmonia_dark
            }
            binding.diffScoreTv.setTextColor(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    requireContext().getColor(textColor)
                else ContextCompat.getColor(requireContext(), textColor)
            )
            viewModel.bind(war.war?.mid, adapter.sharedClick, binding.deleteWarBtn.clicks())
            viewModel.sharedWarName
                .onEach {
                    binding.warTitleTv.text = it
                }.launchIn(lifecycleScope)
            viewModel.sharedBestTrack.onEach { track ->
                binding.bestTrack.bind(track)
                binding.bestTrack
                    .clicks()
                    .filter { findNavController().currentDestination?.id == R.id.warDetailsFragment }
                    .onEach { findNavController().navigate(WarDetailsFragmentDirections.toTrackDetails(war = war.war, warTrack = track.track, index = 0)) }
                    .launchIn(lifecycleScope)
            }.launchIn(lifecycleScope)
            viewModel.sharedWorstTrack.onEach { track ->
                binding.worstTrack.bind(track)
                binding.worstTrack
                    .clicks()
                    .filter { findNavController().currentDestination?.id == R.id.warDetailsFragment }
                    .onEach { findNavController().navigate(WarDetailsFragmentDirections.toTrackDetails(war = war.war, warTrack = track.track, index = 0)) }
                    .launchIn(lifecycleScope)
            }.launchIn(lifecycleScope)
            viewModel.sharedWarPlayers
                .filter { lifecycle.isResumed }
                .onEach {
                    val firstHalfList = when (it.size > 6) {
                        true -> it.subList(0, 4)
                        else -> it.subList(0, 3)
                    }
                    val secondHalfList = when (it.size > 6) {
                        true -> it.subList(4, it.size)
                        else -> it.subList(3, it.size)
                    }
                    binding.progress.isVisible = false
                    binding.mainLayout.isVisible = true
                    firstsPlayersAdapter.addPlayers(firstHalfList)
                    lastsPlayersAdapter.addPlayers(secondHalfList)
                }.launchIn(lifecycleScope)
            viewModel.sharedTracks.onEach {
                adapter.addTracks(it)
            }.launchIn(lifecycleScope)
            viewModel.sharedTrackClick
                .filter { findNavController().currentDestination?.id == R.id.warDetailsFragment }
                .onEach { findNavController().navigate(WarDetailsFragmentDirections.toTrackDetails(war = war.war, warTrack = null, index = it)) }
                .launchIn(lifecycleScope)
            viewModel.sharedWarDeleted
                .filter { findNavController().currentDestination?.id == R.id.warDetailsFragment }
                .onEach { findNavController().popBackStack() }
                .launchIn(lifecycleScope)
            viewModel.sharedDeleteWarVisible
                .onEach { binding.deleteWarBtn.isVisible = it }
                .launchIn(lifecycleScope)
            viewModel.sharedPlayerHost
                .onEach { binding.playerHostTv.text = it }
                .launchIn(lifecycleScope)
            viewModel.sharedPenalties
                .filterNotNull()
                .onEach {
                    binding.penaltiesLayout.isVisible = true
                    penaltiesAdapter.addPenalties(it)
                }
                .launchIn(lifecycleScope)
            viewModel.sharedShockCount
                .filterNotNull()
                .onEach {
                    binding.shockLayout.isVisible = true
                    binding.shockCount.text = it
                }.launchIn(lifecycleScope)
        }
    }
}