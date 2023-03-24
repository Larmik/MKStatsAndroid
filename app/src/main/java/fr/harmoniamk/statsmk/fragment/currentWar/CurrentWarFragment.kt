package fr.harmoniamk.statsmk.fragment.currentWar

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
import fr.harmoniamk.statsmk.databinding.FragmentCurrentWarBinding
import fr.harmoniamk.statsmk.extension.backPressedDispatcher
import fr.harmoniamk.statsmk.extension.clicks
import fr.harmoniamk.statsmk.extension.isResumed
import fr.harmoniamk.statsmk.fragment.addPenalty.AddPenaltyFragment
import fr.harmoniamk.statsmk.fragment.popup.PopupFragment
import fr.harmoniamk.statsmk.fragment.subPlayer.SubPlayerFragment
import fr.harmoniamk.statsmk.model.local.MKWar
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

@FlowPreview
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class CurrentWarFragment : Fragment(R.layout.fragment_current_war) {

    private val binding : FragmentCurrentWarBinding by viewBinding()
    private val viewModel: CurrentWarViewModel by viewModels()
    private var war: MKWar? = null
    private var popup = PopupFragment("Êtes-vous sûr de vouloir supprimer le match ?", "Supprimer")

    private var subFragment = SubPlayerFragment()

    private var popupShowing = false


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = CurrentWarTrackAdapter()
        val penaltiesAdapter = PenaltyAdapter()
        val firstsPlayersAdapter = CurrentPlayerAdapter(isCurrent = true)
        val lastsPlayersAdapter = CurrentPlayerAdapter(isCurrent = true)
        binding.currentTracksRv.adapter = adapter
        binding.penaltiesRv.adapter = penaltiesAdapter
        binding.firstsPlayersRv.adapter = firstsPlayersAdapter
        binding.lastsPlayersRv.adapter = lastsPlayersAdapter
        viewModel.bind(
            onBack = requireActivity().backPressedDispatcher(viewLifecycleOwner),
            onNextTrack = binding.nextTrackBtn.clicks(),
            onTrackClick = adapter.sharedClick,
            onPopup = binding.deleteWarBtn.clicks(),
            onPenalty = binding.penaltyBtn.clicks(),
            onSub = binding.subBtn.clicks(),
            onSubDismiss = subFragment.sharedDismiss
        )

        viewModel.sharedCurrentWar
            .filter { lifecycle.isResumed }
            .onEach {
                war = it
                binding.progress.isVisible = false
                binding.mainLayout.isVisible = true
                binding.warTitleTv.text = it.name
                binding.warDateTv.text = it.war?.createdDate
                binding.currentWarTv.text = it.displayedState
                binding.scoreTv.text = it.displayedScore
                binding.diffScoreTv.text = it.displayedDiff
                val textColor = when  {
                    it.displayedDiff.contains("-") -> R.color.lose
                    it.displayedDiff.contains("+") -> R.color.green
                    else -> R.color.harmonia_dark
                }
                binding.diffScoreTv.setTextColor(
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                        requireContext().getColor(textColor)
                        else ContextCompat.getColor(requireContext(), textColor)
                )
            }.launchIn(lifecycleScope)

        viewModel.sharedButtonVisible
            .filter { lifecycle.isResumed }
            .onEach {
                binding.nextTrackBtn.isVisible = it
                binding.deleteWarBtn.isVisible = it
                binding.subBtn.isVisible = it
                binding.penaltyBtn.isVisible = it
            }.launchIn(lifecycleScope)

        viewModel.sharedQuit
            .filter { findNavController().currentDestination?.id == R.id.currentWarFragment }
            .onEach { findNavController().popBackStack() }
            .launchIn(lifecycleScope)
        viewModel.sharedBackToWars
            .filter { findNavController().currentDestination?.id == R.id.currentWarFragment }
            .onEach { findNavController().navigate(CurrentWarFragmentDirections.backToWars()) }
            .launchIn(lifecycleScope)

        viewModel.sharedSelectTrack
            .filter { findNavController().currentDestination?.id == R.id.currentWarFragment }
            .mapNotNull { war?.war?.mid }
            .onEach { findNavController().navigate(CurrentWarFragmentDirections.addTrack(it)) }
            .launchIn(lifecycleScope)

        viewModel.sharedTracks
            .filter { lifecycle.isResumed }
            .onEach {
                binding.playedLabel.isVisible = it.isNotEmpty()
                binding.emptyTrack.isVisible = it.isEmpty()
                adapter.addTracks(it)
            }
            .launchIn(lifecycleScope)

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
                firstsPlayersAdapter.addPlayers(firstHalfList)
                lastsPlayersAdapter.addPlayers(secondHalfList)
            }.launchIn(lifecycleScope)

        viewModel.sharedTrackClick
            .filter { findNavController().currentDestination?.id == R.id.currentWarFragment }
            .onEach { findNavController().navigate(CurrentWarFragmentDirections.toTrackDetails(war = war?.war, warTrack = war?.warTracks?.get(it)?.track, index = it)) }
            .launchIn(lifecycleScope)

        viewModel.sharedPopupShowing
            .onEach {
               when (it) {
                    true -> {
                        if (!popup.isAdded && !popupShowing) {
                            popup = PopupFragment("Êtes-vous sûr de vouloir supprimer le match ?", "Supprimer")
                            viewModel.bindPopup(onDelete = popup.onPositiveClick.onEach { popup.setLoading("Suppression de la war en cours, veuillez patienter") }, onDismiss = popup.onNegativeClick)
                            popup.show(childFragmentManager, null)
                            popupShowing = true
                        }
                    }
                    else -> {
                        popup.dismiss()
                        popupShowing = false
                    }
                }
            }
            .launchIn(lifecycleScope)

        viewModel.sharedAddPenalty
            .onEach {
                val penaltyFragment = AddPenaltyFragment(it)
                if (!penaltyFragment.isAdded)
                    penaltyFragment.show(childFragmentManager, null)
            }.launchIn(lifecycleScope)

        viewModel.sharedSubPlayer
            .onEach {
                subFragment = SubPlayerFragment()
                if (!subFragment.isAdded)
                    subFragment.show(childFragmentManager, null)
            }.launchIn(lifecycleScope)

        viewModel.sharedPenalties
            .filter { lifecycle.isResumed }
            .filterNotNull()
            .onEach {
                binding.penaltiesLayout.isVisible = true
                penaltiesAdapter.addPenalties(it)
            }
            .launchIn(lifecycleScope)

        viewModel.sharedShockCount
            .filter { lifecycle.isResumed }
            .onEach {
                binding.shockLayout.isVisible = true
                binding.shockCount.text = it
            }.launchIn(lifecycleScope)

        viewModel.sharedLoading
            .onEach {
                binding.progress.isVisible = it
                binding.mainLayout.isVisible = !it
            }.launchIn(lifecycleScope)
    }

}