package fr.harmoniamk.statsmk.fragment.playerSelect

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.databinding.FragmentWarPlayersBinding
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.extension.checks
import fr.harmoniamk.statsmk.extension.clicks
import fr.harmoniamk.statsmk.model.firebase.User
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@FlowPreview
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class PlayersWarFragment : Fragment(R.layout.fragment_war_players) {

    private val binding: FragmentWarPlayersBinding by viewBinding()
    private val viewModel: PlayersWarViewModel by viewModels()

    var onWarCreated: MutableSharedFlow<Unit>? = null
    var onUsersSelected: MutableSharedFlow<List<User>>? = null
    var onOfficialCheck: MutableSharedFlow<Boolean>? = null

    companion object{
        fun instance(onWarCreated: MutableSharedFlow<Unit>, onUsersSelected: MutableSharedFlow<List<User>>, onOfficialCheck: MutableSharedFlow<Boolean>): PlayersWarFragment {
            val fragment = PlayersWarFragment()
            fragment.onWarCreated = onWarCreated
            fragment.onUsersSelected = onUsersSelected
            fragment.onOfficialCheck = onOfficialCheck
            return fragment
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val usersAdapter = PlayerListAdapter()
        viewModel.bind(usersAdapter.sharedUserSelected, binding.official.checks())
        binding.playersRv.adapter = usersAdapter
        onWarCreated?.let {
            binding.startWarBtn.clicks().bind(it, lifecycleScope)
        }

        viewModel.sharedPlayers.onEach { usersAdapter.addUsers(it) }.launchIn(lifecycleScope)
        onUsersSelected?.let {
            viewModel.sharedUsersSelected.bind(it, lifecycleScope)
        }
        onOfficialCheck?.let {
            viewModel.sharedOfficial.bind(it, lifecycleScope)
        }

        viewModel.sharedButtonEnabled.onEach { binding.startWarBtn.isEnabled = it }.launchIn(lifecycleScope)

    }

}