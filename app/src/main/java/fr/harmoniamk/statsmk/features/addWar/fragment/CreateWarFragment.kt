package fr.harmoniamk.statsmk.features.addWar.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.databinding.FragmentCreateWarBinding
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.extension.clicks
import fr.harmoniamk.statsmk.features.addWar.adapter.TeamListAdapter
import fr.harmoniamk.statsmk.features.addWar.viewmodel.CreateWarViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
@FlowPreview
@ExperimentalCoroutinesApi
class CreateWarFragment(val onCreateWar: MutableSharedFlow<Unit>) : Fragment(R.layout.fragment_create_war) {

    private val binding: FragmentCreateWarBinding by viewBinding()
    private val viewModel: CreateWarViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = TeamListAdapter()
        binding.teamRv.adapter = adapter
        viewModel.bind(adapter.onTeamClick, binding.startWarBtn.clicks())

        viewModel.sharedTeams.onEach {
            adapter.addTeams(it)
        }.launchIn(lifecycleScope)

        viewModel.sharedTeamSelected.onEach {
                binding.createWarLayout.visibility = View.VISIBLE
                binding.startWarBtn.text = it
            }.launchIn(lifecycleScope)

        viewModel.sharedStarted.bind(onCreateWar, lifecycleScope)
    }

}