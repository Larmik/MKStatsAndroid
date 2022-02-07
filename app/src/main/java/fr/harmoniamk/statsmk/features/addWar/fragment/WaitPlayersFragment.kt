package fr.harmoniamk.statsmk.features.addWar.fragment

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import fr.harmoniamk.statsmk.QuitWarDialogFragment
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.database.firebase.model.War
import fr.harmoniamk.statsmk.databinding.FragmentWaitPlayersBinding
import fr.harmoniamk.statsmk.extension.backPressedDispatcher
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.features.addWar.adapter.PlayerListAdapter
import fr.harmoniamk.statsmk.features.addWar.viewmodel.WaitPlayersViewModel
import fr.harmoniamk.statsmk.features.currentTournament.fragment.DeleteTournamentDialogFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

@ExperimentalCoroutinesApi
@FlowPreview
@AndroidEntryPoint
class WaitPlayersFragment: Fragment(R.layout.fragment_wait_players) {

    private val binding: FragmentWaitPlayersBinding by viewBinding()
    private val viewModel: WaitPlayersViewModel by viewModels()

    var onWarQuit = MutableSharedFlow<Unit>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = PlayerListAdapter()
        binding.playersRv.adapter = adapter
        viewModel.bind(requireActivity().backPressedDispatcher(viewLifecycleOwner))

        viewModel.sharedOnlinePlayers.onEach {
            adapter.addOrRemovePlayers(it)
        }.launchIn(lifecycleScope)

        viewModel.sharedBack.onEach {
            val dialog = QuitWarDialogFragment()
            viewModel.bindDialog(dialog.sharedWarLeft, dialog.sharedClose)
            if (!dialog.isAdded) dialog.show(childFragmentManager, null)
            viewModel.sharedCancel
                .filter { findNavController().currentDestination?.id == R.id.waitPlayersFragment }
                .onEach { dialog.dismiss() }
                .launchIn(lifecycleScope)
        }.launchIn(lifecycleScope)

        viewModel.sharedQuit
            .filter { findNavController().currentDestination?.id == R.id.waitPlayersFragment ||
                        findNavController().currentDestination?.id == R.id.addWarFragment}
            .onEach { findNavController().popBackStack() }
            .launchIn(lifecycleScope)

        viewModel.sharedWarName.onEach {
            binding.warNameTv.text = it
        }.launchIn(lifecycleScope)

    }

}