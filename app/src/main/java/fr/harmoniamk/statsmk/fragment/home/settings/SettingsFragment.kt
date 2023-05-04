package fr.harmoniamk.statsmk.fragment.home.settings

import android.app.ProgressDialog
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
import fr.harmoniamk.statsmk.BuildConfig
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.databinding.FragmentSettingsBinding
import fr.harmoniamk.statsmk.extension.clicks
import fr.harmoniamk.statsmk.fragment.home.HomeFragmentDirections
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@ExperimentalCoroutinesApi
@FlowPreview
@AndroidEntryPoint
class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private val binding: FragmentSettingsBinding by viewBinding()
    private val viewModel: SettingsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.bind(
            onManageTeam = binding.manageTeamBtn.clicks(),
            onTheme = flowOf(),
            onManagePlayers = binding.managePlayersBtn.clicks(),
            onProfileClick = binding.profileBtn.clicks(),
            onPlayersClick = binding.playersBtn.clicks(),
            onSimulate = binding.simulateBtn.clicks()
        )

        binding.simulateBtn.isVisible = BuildConfig.DEBUG
        binding.simuLine.isVisible = BuildConfig.DEBUG

        viewModel.sharedManageTeam
            .filter { findNavController().currentDestination?.id == R.id.homeFragment }
            .onEach { findNavController().navigate(HomeFragmentDirections.manageTeams()) }
            .launchIn(lifecycleScope)
        viewModel.sharedManagePlayers
            .filter { findNavController().currentDestination?.id == R.id.homeFragment }
            .onEach { findNavController().navigate(HomeFragmentDirections.managePlayers()) }
            .launchIn(lifecycleScope)
        viewModel.sharedToast
            .onEach { Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show() }
            .launchIn(lifecycleScope)
        viewModel.sharedGoToProfile
            .filter { findNavController().currentDestination?.id == R.id.homeFragment }
            .onEach { findNavController().navigate(HomeFragmentDirections.toProfile()) }
            .launchIn(lifecycleScope)
        viewModel.sharedGoToPlayers
            .filter { findNavController().currentDestination?.id == R.id.homeFragment }
            .onEach { findNavController().navigate(HomeFragmentDirections.toPlayerList(addToTeamBehavior = false)) }
            .launchIn(lifecycleScope)
        var dialog = ProgressDialog(requireContext())

        viewModel.sharedProgress
            .onEach {
                when (it) {
                    true -> {
                        dialog = ProgressDialog(requireContext())
                        dialog.setMessage("Création des wars en cours ...")
                        dialog.show()
                    }
                    else -> dialog.dismiss()
                }
            }.launchIn(lifecycleScope)
    }
}