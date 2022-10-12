package fr.harmoniamk.statsmk.fragment.home.settings

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
import fr.harmoniamk.statsmk.fragment.home.HomeFragmentDirections
import fr.harmoniamk.statsmk.fragment.popup.PopupFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

@ExperimentalCoroutinesApi
@FlowPreview
@AndroidEntryPoint
class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private val binding: FragmentSettingsBinding by viewBinding()
    private val viewModel: SettingsViewModel by viewModels()
    private val disconnectPopup by lazy { PopupFragment("Êtes-vous sûr de vouloir vous déconnecter ?", "Se déconnecter") }
    private val themePopup by lazy { PopupFragment("La fonctionnalité des thèmes n'a pas pu être testée à fond et est encore au stade expérimental, il se peut qu'elle ne fonctionne pas correctement. Voulez-vous continuer malgré tout ?", positiveText = "Essayer les thèmes") }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.bind(
            onLogout = disconnectPopup.onPositiveClick,
            onManageTeam = binding.manageTeamBtn.clicks(),
            onTheme = themePopup.onPositiveClick,
            onManagePlayers = binding.managePlayersBtn.clicks(),
            onMigrate = binding.migrateBtn.clicks(),
            onPopup = flowOf(binding.logoutLayout.clicks().map { true }, disconnectPopup.onNegativeClick.map { false }).flattenMerge(),
            onPopupTheme =  themePopup.onNegativeClick.map { false }
        )
        viewModel.sharedDisconnectPopup
            .onEach {
                when (it) {
                    true -> disconnectPopup.takeIf { !it.isAdded }?.show(childFragmentManager, null)
                    else -> disconnectPopup.dismiss()
                }
            }
            .launchIn(lifecycleScope)
        viewModel.sharedDisconnect
            .filter { findNavController().currentDestination?.id == R.id.homeFragment }
            .onEach {
                disconnectPopup.dismiss()
                findNavController().navigate(HomeFragmentDirections.backToWelcome())
            }.launchIn(lifecycleScope)
        viewModel.sharedManageTeam
            .filter { findNavController().currentDestination?.id == R.id.homeFragment }
            .onEach { findNavController().navigate(HomeFragmentDirections.manageTeams()) }
            .launchIn(lifecycleScope)
        viewModel.sharedManagePlayers
            .filter { findNavController().currentDestination?.id == R.id.homeFragment }
            .onEach { findNavController().navigate(HomeFragmentDirections.managePlayers()) }
            .launchIn(lifecycleScope)
        viewModel.sharedThemeClick
            .filter { findNavController().currentDestination?.id == R.id.homeFragment }
            .onEach {
                themePopup.dismiss()
                findNavController().navigate(HomeFragmentDirections.manageTheme())
            }
            .launchIn(lifecycleScope)

        viewModel.sharedThemePopup
            .onEach {
                when (it) {
                    true -> themePopup.takeIf { !it.isAdded }?.show(childFragmentManager, null)
                    else -> themePopup.dismiss()
                }
            }.launchIn(lifecycleScope)
        viewModel.sharedToast
            .onEach { Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show() }
            .launchIn(lifecycleScope)
        viewModel.sharedUserLabel
            .onEach { binding.currentUserLabel.text = "Connecté en tant que $it" }
            .launchIn(lifecycleScope)
    }
}