package fr.harmoniamk.statsmk.fragment.playerSelect

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.model.firebase.User
import fr.harmoniamk.statsmk.databinding.FragmentWarPlayersBinding
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.extension.checks
import fr.harmoniamk.statsmk.extension.clicks
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

@FlowPreview
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class PlayersWarFragment(private val onWarCreated: MutableSharedFlow<Unit>, private val onUsersSelected: MutableSharedFlow<List<User>>, private val onOfficialCheck: MutableSharedFlow<Boolean>) : Fragment(R.layout.fragment_war_players) {

    private val binding: FragmentWarPlayersBinding by viewBinding()
    private val viewModel: PlayersWarViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val usersAdapter = PlayerListAdapter()
        viewModel.bind(usersAdapter.sharedUserSelected, binding.official.checks())
        binding.playersRv.adapter = usersAdapter
        binding.startWarBtn.clicks().bind(onWarCreated, lifecycleScope)
        viewModel.sharedPlayers.onEach { usersAdapter.addUsers(it) }.launchIn(lifecycleScope)
        viewModel.sharedUsersSelected.bind(onUsersSelected, lifecycleScope)
        viewModel.sharedButtonEnabled.onEach { binding.startWarBtn.isEnabled = it }.launchIn(lifecycleScope)
        viewModel.sharedOfficial.bind(onOfficialCheck, lifecycleScope)
    }

}