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
import fr.harmoniamk.statsmk.extension.safeSubList
import fr.harmoniamk.statsmk.fragment.addPenalty.AddPenaltyFragment
import fr.harmoniamk.statsmk.fragment.popup.PopupFragment
import fr.harmoniamk.statsmk.fragment.subPlayer.SubPlayerFragment
import fr.harmoniamk.statsmk.model.firebase.TOTAL_TRACKS
import fr.harmoniamk.statsmk.model.local.MKWar
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

@FlowPreview
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class CurrentWarFragment : Fragment(R.layout.fragment_current_war) {

    private val binding : FragmentCurrentWarBinding by viewBinding()
    private val viewModel: OldCurrentWarViewModel by viewModels()
    private var war: MKWar? = null
    private var popup = PopupFragment(R.string.delete_war_confirm, R.string.delete)
    private var validatePopup = PopupFragment(R.string.validate_war_confirm, R.string.confirm)
    private var penaltyFragment = AddPenaltyFragment()
    private var subFragment = SubPlayerFragment()

    private var popupShowing = false
    private var penaltyShowing = false
    private var subShowing = false


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
            onPopup = flowOf(
                binding.deleteWarBtn.clicks().map { CurrentWarViewModel.PopupType.DELETE },
                binding.validateWarBtn.clicks().map { CurrentWarViewModel.PopupType.VALIDATE }
            ).flattenMerge(),
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
                binding.currentWarTv.text = when (it.isOver) {
                    true -> requireContext().getString(R.string.war_over)
                    else -> String.format(
                        requireContext().getString(R.string.war_in_progress),
                        it.trackPlayed.toString(),
                        TOTAL_TRACKS.toString()
                    )
                }
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

        viewModel.sharedValidateWar
            .filter { lifecycle.isResumed }
            .onEach {
                binding.nextTrackBtn.isVisible = !it
                binding.validateWarBtn.isVisible = it
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
                    true -> it.safeSubList(0, 4)
                    else -> it.safeSubList(0, 3)
                }
                val secondHalfList = when (it.size > 6) {
                    true -> it.safeSubList(4, it.size)
                    else -> it.safeSubList(3, it.size)
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
               when (it.second) {
                    true -> {
                        if (!popupShowing) {
                            when (it.first) {
                                CurrentWarViewModel.PopupType.DELETE -> {
                                    if (!popup.isAdded) {
                                        popup = PopupFragment(R.string.delete_war_confirm, R.string.delete)
                                        viewModel.bindPopup(onDelete = popup.onPositiveClick.onEach { popup.setLoading(R.string.delete_war_in_progress) }, onDismiss = popup.onNegativeClick)
                                        popup.show(childFragmentManager, null)
                                        popupShowing = true
                                    }
                                }
                                CurrentWarViewModel.PopupType.VALIDATE -> {
                                    if (!validatePopup.isAdded) {
                                        validatePopup = PopupFragment(R.string.validate_war_confirm, R.string.confirm)
                                        viewModel.bindValidatePopup(onValidate = validatePopup.onPositiveClick.onEach { validatePopup.setLoading(R.string.creating_war) }, onDismiss = validatePopup.onNegativeClick)
                                        validatePopup.show(childFragmentManager, null)
                                        popupShowing = true
                                    }
                                }
                            }
                        }
                    }
                    else -> {
                        when (it.first) {
                            CurrentWarViewModel.PopupType.DELETE -> popup.dismiss()
                            CurrentWarViewModel.PopupType.VALIDATE -> validatePopup.dismiss()
                        }
                        popupShowing = false
                    }
                }
            }
            .launchIn(lifecycleScope)

        viewModel.sharedAddPenalty
            .onEach {
                if (!penaltyFragment.isAdded && !penaltyShowing) {
                    penaltyFragment = AddPenaltyFragment(it)
                    penaltyFragment.show(childFragmentManager, null)
                    penaltyShowing = true
                    penaltyFragment.onDismiss
                        .onEach {
                            penaltyFragment.dismiss()
                            penaltyShowing = false
                        }.launchIn(lifecycleScope)
                }
            }.launchIn(lifecycleScope)

        viewModel.sharedSubPlayer
            .onEach {
                if (!subFragment.isAdded && !subShowing) {
                    subFragment = SubPlayerFragment()
                    subFragment.show(childFragmentManager, null)
                    subShowing = true
                    subFragment.sharedDismiss
                        .onEach {
                            subFragment.dismiss()
                            subShowing = false
                        }.launchIn(lifecycleScope)
                }
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
            .filter { lifecycle.isResumed }
            .onEach {
                binding.progress.isVisible = it
                binding.mainLayout.isVisible = !it
            }.launchIn(lifecycleScope)

        viewModel.sharedGoToWarResume
            .filter { findNavController().currentDestination?.id == R.id.currentWarFragment }
            .onEach { findNavController().navigate(CurrentWarFragmentDirections.goToWarDetails(it)) }
            .launchIn(lifecycleScope)
    }

}