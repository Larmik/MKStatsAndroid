package fr.harmoniamk.statsmk.features.addWar.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.databinding.FragmentWaitPlayersBinding
import fr.harmoniamk.statsmk.features.addWar.viewmodel.WaitPlayersViewModel

class WaitPlayersFragment: Fragment(R.layout.fragment_wait_players) {

    private val binding: FragmentWaitPlayersBinding by viewBinding()
    private val viewModel: WaitPlayersViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

}