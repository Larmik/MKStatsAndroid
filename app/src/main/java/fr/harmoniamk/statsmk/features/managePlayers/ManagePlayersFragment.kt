package fr.harmoniamk.statsmk.features.managePlayers

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.databinding.FragmentManagePlayersBinding
import fr.harmoniamk.statsmk.extension.clicks
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

@ExperimentalCoroutinesApi
@FlowPreview
@AndroidEntryPoint
class ManagePlayersFragment : Fragment(R.layout.fragment_manage_players) {

    private val binding: FragmentManagePlayersBinding by viewBinding()
    private val viewModel: ManagePlayersViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = ManagePlayersAdapter()
        var dialog = EditPlayerFragment()
        binding.playersRv.adapter = adapter
        viewModel.bind(
            onAdd = binding.addPlayerBtn.clicks(),
            onEdit = adapter.sharedEdit,
        )
        viewModel.sharedPlayers.onEach {
            adapter.addPlayers(it)
        }.launchIn(lifecycleScope)
        viewModel.sharedTitle.onEach { binding.managePlayersTitle.text = it }.launchIn(lifecycleScope)
        viewModel.sharedAddPlayer
            .filter { findNavController().currentDestination?.id == R.id.managePlayersFragment }
            .onEach { findNavController().navigate(ManagePlayersFragmentDirections.addPlayer()) }
            .launchIn(lifecycleScope)
        viewModel.sharedAddPlayerVisibility
            .onEach { binding.addPlayerBtn.visibility = it }
            .launchIn(lifecycleScope)
        viewModel.sharedEdit
            .onEach {
                dialog = EditPlayerFragment(it)
                viewModel.bindDialog(
                    onDelete = dialog.onPlayerDelete,
                    onTeamLeft = dialog.onTeamLeave,
                    onPlayerEdited = dialog.onPlayerEdit
                )
            }.launchIn(lifecycleScope)
        viewModel.sharedRedirectToSettings
            .filter { findNavController().currentDestination?.id == R.id.managePlayersFragment }
            .onEach { findNavController().popBackStack() }
            .launchIn(lifecycleScope)
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.sharedShowDialog.collect {
                when (it) {
                    true -> dialog.show(childFragmentManager, null)
                    else -> dialog.dismiss()
                }
            }
        }
        viewModel.sharedRedirectToWelcome
            .filter { findNavController().currentDestination?.id == R.id.managePlayersFragment }
            .onEach { findNavController().navigate(ManagePlayersFragmentDirections.backToWelcome()) }
            .launchIn(lifecycleScope)
    }
}