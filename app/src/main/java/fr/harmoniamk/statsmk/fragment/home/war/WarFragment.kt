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
import fr.harmoniamk.statsmk.fragment.home.HomeFragmentDirections
import fr.harmoniamk.statsmk.fragment.popup.PopupFragment
import fr.harmoniamk.statsmk.fragment.settings.manageTeams.AddTeamFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*

@FlowPreview
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class WarFragment : Fragment(R.layout.fragment_war) {

    private val binding: FragmentWarBinding by viewBinding()
    private val viewModel: WarViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val lastAdapter = LastWarAdapter()
        val separators = listOf(
            binding.separator3,
            binding.separator4
        )

        lifecycleScope.launchWhenResumed {
            binding.lastWarRv.adapter = lastAdapter
            viewModel.bind(
                onCreateWar = binding.createWarBtn.clicks(),
                onCurrentWarClick = binding.currentWarCard.clicks(),
                onWarClick = lastAdapter.sharedItemClick,
                onCreateTeam = binding.createTeamBtn.clicks()
            )

            viewModel.sharedHasTeam
                .onEach {
                    binding.noTeamLayout.isVisible = !it
                    binding.hasTeamLayout.isVisible = it
                    binding.separator1.isVisible = it
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
                    binding.currentWarLayout.isVisible = false
                    binding.createWarLayout.isVisible = it == null
                    binding.currentWarLayout.isVisible = it != null
                    binding.nameTv.text = it?.name
                    binding.timeTv.text = it?.war?.createdDate
                    binding.currentWarRemaining.text = it?.displayedState
                    binding.currentWarScore.text = it?.scoreLabel
                }.launchIn(lifecycleScope)

            viewModel.sharedCurrentWarClick
                .filter { findNavController().currentDestination?.id == R.id.homeFragment }
                .onEach {
                    when (it) {
                        true -> findNavController().navigate(HomeFragmentDirections.goToCurrentWar())
                        else -> Toast.makeText(requireContext(), "Vous ne pouvez pas accéder à la war en cours car vous êtes hors connexion.", Toast.LENGTH_SHORT).show()
                    }
                }.launchIn(lifecycleScope)

            viewModel.sharedLoaded
                .onEach {
                    binding.progress.isVisible = false
                    binding.mainWarLayout.isVisible = true
                }.launchIn(lifecycleScope)

            viewModel.sharedLastWars
                .filterNot { it.isEmpty() }
                .onEach {
                    binding.lastWarsLayout.isVisible = true
                    binding.lastWarRv.isVisible = it.isNotEmpty()
                    binding.allWarsBtn.isVisible = it.isNotEmpty()
                    separators.forEach { view -> view.isVisible = true }
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
                .onEach {
                    binding.createWarBtn.isVisible = it
                    binding.warHostTv.isVisible = it
                }.launchIn(lifecycleScope)

            viewModel.sharedShowUpdatePopup
                .onEach {
                    val popup = PopupFragment("L'application nécessite une mise à jour pour fonctionner correctement. \n \n Veuillez mettre à jour l'application pour continuer.", positiveText = "Mettre à jour", negativeText = "Retour")
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