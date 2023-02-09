package fr.harmoniamk.statsmk.fragment.playerList

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.databinding.FragmentPlayerListBinding
import fr.harmoniamk.statsmk.extension.clicks
import fr.harmoniamk.statsmk.extension.onTextChanged
import fr.harmoniamk.statsmk.fragment.playerSelect.PlayerListAdapter
import fr.harmoniamk.statsmk.fragment.popup.PopupFragment
import fr.harmoniamk.statsmk.fragment.settings.managePlayers.AddPlayersFragment
import fr.harmoniamk.statsmk.fragment.settings.managePlayers.EditPlayerFragment
import fr.harmoniamk.statsmk.fragment.settings.managePlayers.ManagePlayersAdapter
import fr.harmoniamk.statsmk.fragment.subPlayer.SubPlayerAdapter
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

    private var addToTeamBehavior: Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addToTeamBehavior = arguments?.getBoolean("addToTeamBehavior")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = ManagePlayersAdapter(showAlly = true)
        val addToTeamAdapter = PlayerListAdapter()
        viewModel.bind(
            onAdd = binding.addPlayerBtn.clicks(),
            onEdit = adapter.sharedEdit,
            onSearch = binding.searchEt.onTextChanged(),
            onPlayerSelected = addToTeamAdapter.sharedUserSelected,
            onAddToTeam = binding.addPlayerToTeamBtn.clicks()
        )
        when (addToTeamBehavior) {
            true -> {
                binding.playersRv.adapter = addToTeamAdapter
                viewModel.sharedAddPlayerList.onEach {
                    addToTeamAdapter.addUsers(it)
                }.launchIn(lifecycleScope)
                viewModel.sharedPlayerAdded
                    .onEach { findNavController().popBackStack() }
                    .launchIn(lifecycleScope)
                viewModel.sharedAddToTeamButtonVisible
                    .onEach { binding.addPlayerToTeamBtn.isVisible = it }
                    .launchIn(lifecycleScope)

            }
            else -> {
                binding.playersRv.adapter = adapter
                viewModel.sharedPlayerList.onEach {
                    adapter.addPlayers(it)
                }.launchIn(lifecycleScope)
            }
        }

        viewModel.sharedAddPlayer
            .onEach {
                val addPlayerFragment = AddPlayersFragment()
                viewModel.bindAddDialog(addPlayerFragment.onUserAdded)
                addPlayerFragment.takeIf { !it.isAdded }?.show(childFragmentManager, null)
                addPlayerFragment.onUserAdded
                    .onEach { addPlayerFragment.dismiss() }
                    .launchIn(lifecycleScope)
            }.launchIn(lifecycleScope)

        viewModel.sharedEditName
            .onEach { user ->
                val changeNamePopup = PopupFragment(
                    message = "Modifier le pseudo",
                    positiveText = "Enregistrer",
                    editTextHint = user.name
                )
                viewModel.bindDialog(user, changeNamePopup.onTextChange, changeNamePopup.onPositiveClick, changeNamePopup.onNegativeClick)
                changeNamePopup.takeIf { !it.isAdded }?.show(childFragmentManager, null)
                viewModel.sharedNewName
                    .onEach { name ->
                        changeNamePopup.dismiss()
                        viewModel.takeIf { name != user.name }?.refresh()
                    }.launchIn(lifecycleScope)
            }.launchIn(lifecycleScope)

    }

}