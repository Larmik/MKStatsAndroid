package fr.harmoniamk.statsmk.fragment.managePlayers

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
import fr.harmoniamk.statsmk.extension.isTrue
import fr.harmoniamk.statsmk.repository.PreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlin.coroutines.CoroutineContext

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
            viewModel.bind(player)
            val hasAccount = user.accessCode != "null" && !user.accessCode.isNullOrEmpty()
            binding.playernameEt.setText(player.name)
            viewModel.sharedIsAdmin
                .onEach { binding.adminCheckbox.isVisible = hasAccount && it }
                .launchIn(lifecycleScope)
            viewModel.sharedIsMember
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
            //binding.adminCheckbox.isChecked = player.isAdmin.isTrue
            binding.playernameEt.isEnabled = !hasAccount
            binding.modifyLabel.isVisible = !hasAccount
            binding.deleteBtn.isVisible = !hasAccount

            binding.nextBtn
                .clicks()
                .filterNot { binding.playernameEt.text.toString().isEmpty() }
                .map {
                    player.apply {
                        this.name = binding.playernameEt.text.toString()
                        //this.isAdmin = binding.adminCheckbox.isChecked
                    }
                }.bind(onPlayerEdit, lifecycleScope)
            binding.deleteBtn
                .clicks()
                .map { player }
                .bind(onPlayerDelete, lifecycleScope)
            binding.leaveTeamBtn
                .clicks()
                .map { player.apply { this.team = "-1" } }
                .bind(onTeamLeave, lifecycleScope)
        }
    }


}