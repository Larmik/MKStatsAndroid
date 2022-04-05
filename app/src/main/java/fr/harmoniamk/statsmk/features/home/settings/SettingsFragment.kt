package fr.harmoniamk.statsmk.features.home.settings

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.databinding.FragmentSettingsBinding
import fr.harmoniamk.statsmk.extension.clicks
import fr.harmoniamk.statsmk.features.home.HomeFragmentDirections
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.filter
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
        viewModel.bind(binding.logoutLayout.clicks(), binding.manageTeamBtn.clicks(), binding.themeBtn.clicks(), binding.managePlayersBtn.clicks())
        viewModel.sharedDisconnect
            .filter { findNavController().currentDestination?.id == R.id.homeFragment }
            .onEach { findNavController().navigate(HomeFragmentDirections.backToWelcome()) }
            .launchIn(lifecycleScope)
        viewModel.sharedManageTeam
            .filter { findNavController().currentDestination?.id == R.id.homeFragment }
            .onEach { findNavController().navigate(HomeFragmentDirections.manageTeams()) }
            .launchIn(lifecycleScope)
        viewModel.sharedManagePlayers
            .filter { findNavController().currentDestination?.id == R.id.homeFragment }
            .onEach { findNavController().navigate(HomeFragmentDirections.managePlayers()) }
            .launchIn(lifecycleScope)
        viewModel.sharedThemeClick
            .onEach { Toast.makeText(requireContext(), "Bientôt disponible", Toast.LENGTH_SHORT).show() }
            .launchIn(lifecycleScope)
        viewModel.sharedToast
            .onEach { Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show() }
            .launchIn(lifecycleScope)
        viewModel.sharedUserLabel
            .onEach { binding.currentUserLabel.text = "Connecté en tant que $it" }
            .launchIn(lifecycleScope)
    }
}