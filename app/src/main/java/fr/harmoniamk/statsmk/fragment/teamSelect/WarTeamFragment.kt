package fr.harmoniamk.statsmk.fragment.teamSelect

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.model.firebase.Team
import fr.harmoniamk.statsmk.databinding.FragmentWarTeamBinding
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.extension.clicks
import fr.harmoniamk.statsmk.extension.onTextChanged
import fr.harmoniamk.statsmk.fragment.settings.manageTeams.AddTeamFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@FlowPreview
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class WarTeamFragment(val onSelectedTeam: MutableSharedFlow<Team>) : Fragment(R.layout.fragment_war_team) {

    private val binding: FragmentWarTeamBinding by viewBinding()
    private val viewModel: WarTeamViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = TeamListAdapter()
        binding.teamRv.adapter = adapter
        viewModel.bind(adapter.onTeamClick, binding.searchEt.onTextChanged(), binding.addTeamBtn.clicks())
        viewModel.sharedTeams.onEach { adapter.addTeams(it) }.launchIn(lifecycleScope)
        viewModel.sharedTeamSelected.bind(onSelectedTeam, lifecycleScope)
        viewModel.sharedAddTeam
            .onEach {
                val addTeamFragment = AddTeamFragment()
                viewModel.bindAddDialog(addTeamFragment.onTeamAdded)
                addTeamFragment.takeIf { !it.isAdded }?.show(childFragmentManager, null)
                addTeamFragment.onTeamAdded
                    .onEach { addTeamFragment.dismiss() }
                    .launchIn(lifecycleScope)
            }.launchIn(lifecycleScope)
    }
}