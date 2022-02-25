package fr.harmoniamk.statsmk.features.addWar.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.database.model.Team
import fr.harmoniamk.statsmk.databinding.FragmentWarTeamBinding
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.features.addWar.adapter.TeamListAdapter
import fr.harmoniamk.statsmk.features.addWar.viewModel.WarTeamViewModel
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
        viewModel.bind(adapter.onTeamClick)
        viewModel.sharedTeams.onEach { adapter.addTeams(it) }.launchIn(lifecycleScope)
        viewModel.sharedTeamSelected.bind(onSelectedTeam, lifecycleScope)
    }
}