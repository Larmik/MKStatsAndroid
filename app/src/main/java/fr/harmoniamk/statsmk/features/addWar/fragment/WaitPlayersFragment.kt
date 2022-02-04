package fr.harmoniamk.statsmk.features.addWar.fragment

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@ExperimentalCoroutinesApi
@FlowPreview
@AndroidEntryPoint
class WaitPlayersFragment: Fragment(R.layout.fragment_wait_players) {

    private val binding: FragmentWaitPlayersBinding by viewBinding()
    private val viewModel: WaitPlayersViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = PlayerListAdapter()
        binding.playersRv.adapter = adapter
        viewModel.bind(requireActivity().backPressedDispatcher(viewLifecycleOwner))

        viewModel.sharedOnlinePlayers.onEach {
            adapter.addOrRemovePlayers(it)
        }.launchIn(lifecycleScope)

        viewModel.sharedBack.onEach {
            Toast.makeText(requireContext(), "On verra ça plus tard", Toast.LENGTH_SHORT).show()
        }.launchIn(lifecycleScope)

        viewModel.sharedWarName.onEach {
            binding.warNameTv.text = it
        }.launchIn(lifecycleScope)

    }

}