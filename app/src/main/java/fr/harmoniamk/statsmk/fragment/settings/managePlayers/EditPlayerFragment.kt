package fr.harmoniamk.statsmk.fragment.settings.managePlayers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.model.firebase.User
import fr.harmoniamk.statsmk.databinding.FragmentEditPlayersBinding
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.extension.clicks
import fr.harmoniamk.statsmk.extension.onTextChanged
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

@FlowPreview
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class EditPlayerFragment(val user: User? = null) : BottomSheetDialogFragment() {

    lateinit var binding: FragmentEditPlayersBinding
    private val viewModel: EditPlayerViewModel by viewModels()

    val onPlayerEdit = MutableSharedFlow<User>()
    val onTeamLeave = MutableSharedFlow<User>()
    val onPlayerDelete = MutableSharedFlow<User>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentEditPlayersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        user?.let { player ->
            var role = player.role
            viewModel.bind(player,  binding.editRoleBtn.clicks(), binding.playernameEt.onTextChanged())
            binding.playernameEt.setText(player.name)
            viewModel.sharedRoleSelected.onEach { role = it }.launchIn(lifecycleScope)
            viewModel.sharedPlayerIsMember
                .onEach {
                    when (it) {
                        true -> {
                            binding.leaveTeamBtn.text = "Retirer ce joueur de l'équipe"
                            binding.leaveTeamBtn.setTextColor(ContextCompat.getColor(requireContext(), R.color.mario))
                        }
                        else ->  {
                            binding.leaveTeamBtn.text = "Intégrer ce joueur à l'équipe"
                            binding.leaveTeamBtn.setTextColor(ContextCompat.getColor(requireContext(), R.color.luigi))
                        }
                    }
                }.launchIn(lifecycleScope)

            viewModel.sharedLeaveTeamVisibility
                .onEach { binding.leaveTeamBtn.isVisible = it }
                .launchIn(lifecycleScope)

            viewModel.sharedPlayerHasAccount
                .onEach {
                    binding.playernameEt.isEnabled = !it
                    binding.modifyLabel.isVisible = !it
                    binding.roleLayout.isVisible = it
                }.launchIn(lifecycleScope)

            viewModel.sharedDeleteVisibility
                .onEach { binding.deleteBtn.isVisible = it }
                .launchIn(lifecycleScope)

            viewModel.sharedEditRoleVisibility
                .onEach { binding.editRoleBtn.visibility = it }
                .launchIn(lifecycleScope)

            viewModel.sharedUserRoleLabel
                .onEach { binding.roleTv.text = it }
                .launchIn(lifecycleScope)

            binding.nextBtn
                .clicks()
                .filterNot { binding.playernameEt.text.toString().isEmpty() }
                .map { player.apply {
                    this.name = binding.playernameEt.text.toString()
                    this.role = role
                } }
                .bind(onPlayerEdit, lifecycleScope)

            binding.deleteBtn
                .clicks()
                .map { player }
                .bind(onPlayerDelete, lifecycleScope)

            binding.leaveTeamBtn
                .clicks()
                .map { player.apply { this.team = "-1" } }
                .bind(onTeamLeave, lifecycleScope)


            viewModel.sharedButtonEnabled
                .onEach { binding.nextBtn.isEnabled = it }
                .launchIn(lifecycleScope)

            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                val dialog = EditRoleFragment(role)
                viewModel.bindDialog(onRoleSelected = dialog.onRoleChange)
                viewModel.sharedShowDialog.collect {
                    when (it) {
                        true -> dialog.show(childFragmentManager, null)
                        else -> dialog.dismiss()
                    }
                }
            }
        }


    }


}