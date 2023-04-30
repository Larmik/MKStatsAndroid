package fr.harmoniamk.statsmk.fragment.settings.managePlayers

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.databinding.FragmentManagePlayersBinding
import fr.harmoniamk.statsmk.extension.*
import fr.harmoniamk.statsmk.fragment.settings.manageTeams.EditTeamFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

@ExperimentalCoroutinesApi
@FlowPreview
@AndroidEntryPoint
class ManagePlayersFragment : Fragment(R.layout.fragment_manage_players) {

    private val binding: FragmentManagePlayersBinding by viewBinding()
    private val viewModel: ManagePlayersViewModel by viewModels()
    private val _onPictureSave = MutableSharedFlow<String>()
    private var isPicking = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = ManagePlayersAdapter()
        binding.playersRv.adapter = adapter
        viewModel.bind(
            onPictureClick = binding.editLogoBtn.clicks(),
            onPictureEdited = _onPictureSave,
            onAdd = binding.addPlayerBtn.clicks(),
            onEdit = adapter.sharedEdit,
            onSearch = binding.searchEt.onTextChanged(),
            onEditTeam = binding.editTeamBtn.clicks()
        )
        viewModel.sharedTeamName
            .filterNotNull()
            .filter { lifecycle.isResumed }
            .onEach { binding.teamName.text = it }
            .launchIn(lifecycleScope)


        viewModel.sharedPlayers.onEach {
            adapter.addPlayers(it)
        }.launchIn(lifecycleScope)

        viewModel.sharedAddPlayer
            .filter { findNavController().currentDestination?.id == R.id.managePlayersFragment }
            .onEach { findNavController().navigate(ManagePlayersFragmentDirections.toPlayerList(addToTeamBehavior = true)) }
            .launchIn(lifecycleScope)

        viewModel.sharedEditTeamVisibility
            .filter { lifecycle.isResumed }
            .onEach { binding.editTeamBtn.visibility = it }
            .launchIn(lifecycleScope)


        viewModel.sharedRedirectToSettings
            .filter { findNavController().currentDestination?.id == R.id.managePlayersFragment }
            .onEach { findNavController().popBackStack() }
            .launchIn(lifecycleScope)

        viewModel.sharedPictureLoaded
            .filter { lifecycle.isResumed }
            .onEach { binding.teamLogo.setImageURL(it) }
            .launchIn(lifecycleScope)

        viewModel.sharedEditPicture
            .filterNot { isPicking }
            .onEach {
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "image/*"
                startActivityForResult(intent, 456)
                isPicking = true
            }.launchIn(lifecycleScope)

        viewModel.sharedManageVisible
            .filter { lifecycle.isResumed }
            .onEach {
                binding.manageLayout.isVisible = it
                if (!it)
                    binding.teamLogo.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.mk_stats_logo_picture))
            }
            .launchIn(lifecycleScope)

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.sharedTeamEdit.collect {
                val teamDialog = EditTeamFragment(it)
                viewModel.bindEditTeamDialog(teamDialog.onTeamEdit)
                teamDialog.takeIf { !it.isAdded }?.show(childFragmentManager, null)
                teamDialog.onTeamEdit
                    .onEach { teamDialog.dismiss() }
                    .launchIn(lifecycleScope)
            }

        }


        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.sharedEdit.collect {
                val dialog = EditPlayerFragment(it)
                viewModel.bindEditDialog(
                    onDelete = dialog.onPlayerDelete,
                    onTeamLeft = dialog.onTeamLeave,
                    onPlayerEdited = dialog.onPlayerEdit
                )
                dialog.takeIf { !it.isAdded }?.show(childFragmentManager, null)
                viewModel.sharedPlayers
                    .onEach { dialog.dismiss() }
                    .launchIn(lifecycleScope)
            }

        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        isPicking = false
        if (requestCode == 456 && resultCode == Activity.RESULT_OK) {
            flowOf(data?.data?.toString())
                .filterNotNull()
                .bind(_onPictureSave, lifecycleScope)
        }
    }
}