package fr.harmoniamk.statsmk.fragment.playerList

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.databinding.FragmentPlayerListBinding
import fr.harmoniamk.statsmk.extension.clicks
import fr.harmoniamk.statsmk.extension.onTextChanged
import fr.harmoniamk.statsmk.fragment.managePlayers.AddPlayersFragment
import fr.harmoniamk.statsmk.fragment.managePlayers.EditPlayerFragment
import fr.harmoniamk.statsmk.fragment.managePlayers.ManagePlayersAdapter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@FlowPreview
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class PlayerListFragment : Fragment(R.layout.fragment_player_list) {

    private val binding: FragmentPlayerListBinding by viewBinding()
    private val viewModel: PlayerListViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = ManagePlayersAdapter(showAlly = true)
        var dialog = EditPlayerFragment()
        binding.playersRv.adapter = adapter
        viewModel.bind(
            onAdd = binding.addPlayerBtn.clicks(),
            onEdit = adapter.sharedEdit,
            onSearch = binding.searchEt.onTextChanged(),
        )

        viewModel.sharedPlayerList.onEach {
            adapter.addPlayers(it)
        }.launchIn(lifecycleScope)

        viewModel.sharedAddPlayer
            .onEach {
                val addPlayerFragment = AddPlayersFragment()
                viewModel.bindAddDialog(addPlayerFragment.onUserAdded)
                addPlayerFragment.takeIf { !it.isAdded }?.show(childFragmentManager, null)
                addPlayerFragment.onUserAdded
                    .onEach { addPlayerFragment.dismiss() }
                    .launchIn(lifecycleScope)
            }.launchIn(lifecycleScope)

        viewModel.sharedEdit
            .onEach {
                dialog = EditPlayerFragment(it)
                viewModel.bindEditDialog(
                    onDelete = dialog.onPlayerDelete,
                    onTeamIntegrate = dialog.onTeamLeave,
                    onPlayerEdited = dialog.onPlayerEdit
                )
            }.launchIn(lifecycleScope)

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.sharedShowDialog.collect {
                when (it) {
                    true -> dialog.show(childFragmentManager, null)
                    else -> dialog.dismiss()
                }
            }
        }


    }

}