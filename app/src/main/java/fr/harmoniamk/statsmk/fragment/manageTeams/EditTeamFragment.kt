package fr.harmoniamk.statsmk.fragment.manageTeams

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import fr.harmoniamk.statsmk.model.firebase.Team
import fr.harmoniamk.statsmk.databinding.FragmentEditTeamBinding
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.extension.clicks
import fr.harmoniamk.statsmk.extension.onTextChanged
import fr.harmoniamk.statsmk.fragment.managePlayers.EditPlayerViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlin.coroutines.CoroutineContext

@AndroidEntryPoint
@FlowPreview
@ExperimentalCoroutinesApi
class EditTeamFragment(val team: Team? = null) : BottomSheetDialogFragment(), CoroutineScope {

    lateinit var binding: FragmentEditTeamBinding
    private val viewModel: EditTeamViewModel by viewModels()
    
    val onTeamEdit = MutableSharedFlow<Team>()
    val onTeamDelete = MutableSharedFlow<Team>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentEditTeamBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.bind(binding.teamnameEt.onTextChanged(), binding.teamshortEt.onTextChanged())
        binding.teamnameEt.setText(team?.name)
        binding.teamshortEt.setText(team?.shortName)
        binding.nextBtn.clicks().mapNotNull {
            team.apply {
                this?.name = binding.teamnameEt.text.toString()
                this?.shortName = binding.teamshortEt.text.toString()
            }
        }.bind(onTeamEdit, this)

        viewModel.sharedDeleteVisible
            .onEach {
                binding.deleteBtn.isVisible = it
            }.launchIn(this)

        binding.deleteBtn.clicks().mapNotNull { team }.bind(onTeamDelete, this)

        viewModel.sharedButtonEnabled
            .onEach { binding.nextBtn.isEnabled = it }
            .launchIn(lifecycleScope)
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main
}