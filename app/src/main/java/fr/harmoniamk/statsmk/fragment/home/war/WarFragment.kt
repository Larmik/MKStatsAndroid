package fr.harmoniamk.statsmk.fragment.home.war

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.databinding.FragmentWarBinding
import fr.harmoniamk.statsmk.extension.clicks
import fr.harmoniamk.statsmk.extension.safeSubList
import fr.harmoniamk.statsmk.fragment.home.HomeFragmentDirections
import fr.harmoniamk.statsmk.fragment.popup.PopupFragment
import fr.harmoniamk.statsmk.fragment.scheduleWar.ScheduleLineUpAdapter
import fr.harmoniamk.statsmk.fragment.settings.manageTeams.AddTeamFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

@FlowPreview
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class WarFragment : Fragment(R.layout.fragment_war) {

    private val binding: FragmentWarBinding by viewBinding()
    private val viewModel: WarViewModel by viewModels()
    private val popup by lazy { PopupFragment(R.string.creating_war, loading = true) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val lastAdapter = LastWarAdapter()
        lifecycleScope.launchWhenResumed {
            binding.lastWarRv.adapter = lastAdapter
            viewModel.bind(
                onCreateWar = binding.createWarBtn.clicks(),
                onCurrentWarClick = binding.currentWarCard.clicks(),
                onWarClick = lastAdapter.sharedItemClick,
                onCreateTeam = binding.createTeamBtn.clicks(),
                onCreateScheduledWar = binding.startScheduleWarBtn.clicks()
            )

            viewModel.sharedHasTeam
                .onEach {
                    binding.noTeamLayout.isVisible = !it
                    binding.hasTeamLayout.isVisible = it
                }.launchIn(lifecycleScope)

            viewModel.sharedTeamName
                .onEach { binding.currentTeamTv.text = it }
                .launchIn(lifecycleScope)

            viewModel.sharedCreateWar
                .filter { findNavController().currentDestination?.id == R.id.homeFragment }
                .onEach { findNavController().navigate(HomeFragmentDirections.createWar()) }
                .launchIn(lifecycleScope)

            viewModel.sharedCurrentWar
                .onEach {
                    binding.progress.isVisible = false
                    binding.createWarLayout.isVisible = false
                    binding.currentWarCard.isVisible = false
                    binding.createWarLayout.isVisible = it == null
                    binding.currentWarCard.isVisible = it != null
                    binding.nameTv.text = it?.name
                    binding.timeTv.text = it?.war?.createdDate
                    binding.currentWarRemaining.text = it?.displayedState
                    binding.currentWarScore.text = it?.scoreLabel
                    binding.startScheduleWarBtn.isEnabled = it == null
                }.launchIn(lifecycleScope)

            viewModel.sharedCurrentWarClick
                .filter { findNavController().currentDestination?.id == R.id.homeFragment }
                .onEach {
                    when (it) {
                        true -> findNavController().navigate(HomeFragmentDirections.goToCurrentWar())
                        else -> Toast.makeText(requireContext(), "Vous ne pouvez pas accéder à la war en cours car vous êtes hors connexion.", Toast.LENGTH_SHORT).show()
                    }
                }.launchIn(lifecycleScope)

            viewModel.sharedLastWars
                .filterNot { it.isEmpty() }
                .onEach {
                    binding.lastWarsLayout.isVisible = true
                    binding.lastWarRv.isVisible = it.isNotEmpty()
                    binding.allWarsBtn.isVisible = it.isNotEmpty()
                    lastAdapter.addWars(it)
                }.launchIn(lifecycleScope)

            viewModel.sharedGoToWar
                .filter { findNavController().currentDestination?.id == R.id.homeFragment }
                .onEach { findNavController().navigate(HomeFragmentDirections.goToWarDetails(it)) }
                .launchIn(lifecycleScope)

            binding.disposBtn.clicks()
                .filter { findNavController().currentDestination?.id == R.id.homeFragment }
                .onEach { findNavController().navigate(HomeFragmentDirections.goToDispos()) }
                .launchIn(lifecycleScope)

            binding.allWarsBtn.clicks()
                .filter { findNavController().currentDestination?.id == R.id.homeFragment }
                .onEach { findNavController().navigate(HomeFragmentDirections.goToAllWars()) }
                .launchIn(lifecycleScope)

            viewModel.sharedButtonVisible
                .onEach { binding.createWarBtn.isVisible = it }
                .launchIn(lifecycleScope)

            viewModel.sharedShowUpdatePopup
                .onEach {
                    val popup = PopupFragment(R.string.need_update, positiveText = R.string.update, negativeText = R.string.back)
                    popup.onNegativeClick.onEach { requireActivity().finish() }.launchIn(lifecycleScope)
                    popup.onPositiveClick
                        .onEach {
                            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=${requireContext().packageName}")))
                        }.launchIn(lifecycleScope)
                    popup.takeIf { !it.isAdded }?.show(childFragmentManager, null)
                }.launchIn(lifecycleScope)

            viewModel.sharedDispoVisible
                .onEach { binding.dispoLayout.isVisible = it }
                .launchIn(lifecycleScope)

            viewModel.sharedNextScheduledWar
                .onEach {
                    binding.scheduleWarLayout.isVisible = true
                    binding.warName.text = it.opponentName
                    val firstHalfLuAdapter = ScheduleLineUpAdapter(nameList = it.lineupNames?.safeSubList(0, 3))
                    val secondHalfLuAdapter = ScheduleLineUpAdapter(nameList = it.lineupNames?.safeSubList(3, 6))
                    binding.firstHalfLu.adapter = firstHalfLuAdapter
                    binding.secondHalfLu.adapter = secondHalfLuAdapter
                    binding.hour.text = String.format(binding.root.context.getString(R.string.hour_placeholder), it.dispoHour.toString())
                    it.hostName?.let {
                        binding.hostLayout.isVisible = true
                        binding.hostName.text = it
                    }
                }.launchIn(lifecycleScope)

            viewModel.sharedLoading
                .onEach {
                    popup.takeIf { !it.isAdded }?.show(childFragmentManager, null)
                }
                .launchIn(lifecycleScope)

            viewModel.sharedStarted
                .filter { findNavController().currentDestination?.id == R.id.homeFragment }
                .onEach {
                    popup.dismiss()
                    binding.scheduleWarLayout.isVisible = false
                    findNavController().navigate(HomeFragmentDirections.goToCurrentWar())
                }
                .launchIn(lifecycleScope)

        }

        lifecycleScope.launchWhenStarted {
            val dialog = AddTeamFragment(teamWithLeader = true)
            viewModel.bindAddTeamDialog(onTeamAdded = dialog.onTeamAdded)
            viewModel.sharedCreateTeamDialog.collect {
                when (it) {
                    true -> dialog.show(childFragmentManager, null)
                    else -> dialog.dismiss()
                }
            }
        }

    }

}