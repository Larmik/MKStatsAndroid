package fr.harmoniamk.statsmk.features.managePlayers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import fr.harmoniamk.statsmk.database.model.User
import fr.harmoniamk.statsmk.databinding.FragmentEditPlayersBinding
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.extension.clicks
import fr.harmoniamk.statsmk.extension.isTrue
import fr.harmoniamk.statsmk.repository.PreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.map
import kotlin.coroutines.CoroutineContext

@FlowPreview
@ExperimentalCoroutinesApi
class EditPlayerFragment(val user: User? = null) : BottomSheetDialogFragment(), CoroutineScope {

    lateinit var binding: FragmentEditPlayersBinding

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
            val current =  PreferencesRepository(requireContext()).currentUser
            val hasAccount = user.accessCode != "null" && !user.accessCode.isNullOrEmpty()
            val isCurrent = current?.mid == user.mid
            binding.playernameEt.setText(player.name)
            player.accessCode?.let {
                binding.playercodeEt.setText(it.split('-')[0])
            }
            binding.playercodeLayout.isVisible = hasAccount && isCurrent
            binding.adminCheckbox.isVisible = hasAccount && current?.isAdmin.isTrue
            binding.adminCheckbox.isChecked = player.isAdmin.isTrue
            binding.playernameEt.isEnabled = isCurrent || !hasAccount
            binding.modifyLabel.isVisible = isCurrent || !hasAccount
            binding.deleteBtn.isVisible = isCurrent || !hasAccount
            binding.leaveTeamBtn.text = when (isCurrent) {
                true -> "Quitter l'équipe"
                else -> "Retirer ce joueur de l'équipe"
            }
            binding.deleteBtn.text = when (isCurrent) {
                true -> "Supprimer mon compte"
                else -> "Supprimer ce joueur"
            }
            binding.nextBtn
                .clicks()
                .filterNot {
                    binding.playernameEt.text.toString().isEmpty()
                }
                .filterNot { binding.playercodeEt.text.toString().isEmpty() }
                .filter { binding.playercodeEt.text.toString().length == 4 }
                .map {
                    player.apply {
                        this.name = binding.playernameEt.text.toString()
                        this.accessCode = binding.playercodeEt.text.toString()
                        this.isAdmin = binding.adminCheckbox.isChecked
                    }
                }.bind(onPlayerEdit, this)
            binding.deleteBtn
                .clicks()
                .map { player }
                .bind(onPlayerDelete, this)
            binding.leaveTeamBtn
                .clicks()
                .map { player.apply { this.team = "-1" } }
                .bind(onTeamLeave, this)
        }
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main
}