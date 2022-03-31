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
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@ExperimentalCoroutinesApi
@FlowPreview
@AndroidEntryPoint
class ManagePlayersFragment : Fragment(R.layout.fragment_manage_players) {

    private val binding: FragmentManagePlayersBinding by viewBinding()
    private val viewModel: ManagePlayersViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = ManagePlayersAdapter()
        binding.playersRv.adapter = adapter
        viewModel.bind(adapter.sharedDelete, binding.addPlayerBtn.clicks())
        viewModel.sharedPlayers.onEach {
            adapter.addPlayers(it)
        }.launchIn(lifecycleScope)
        viewModel.sharedTitle.onEach { binding.managePlayersTitle.text = it }.launchIn(lifecycleScope)
        viewModel.sharedAddPlayer
            .filter { findNavController().currentDestination?.id == R.id.managePlayersFragment }
            .onEach { findNavController().navigate(ManagePlayersFragmentDirections.addPlayer()) }
            .launchIn(lifecycleScope)

    }
}