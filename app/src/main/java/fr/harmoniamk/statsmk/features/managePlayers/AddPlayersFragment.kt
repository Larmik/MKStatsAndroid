package fr.harmoniamk.statsmk.features.managePlayers

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.databinding.FragmentAddPlayersBinding
import fr.harmoniamk.statsmk.extension.clicks
import fr.harmoniamk.statsmk.extension.onTextChanged
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@ExperimentalCoroutinesApi
@FlowPreview
@AndroidEntryPoint
class AddPlayersFragment : Fragment(R.layout.fragment_add_players) {

    private val binding: FragmentAddPlayersBinding by viewBinding()
    private val viewModel: AddPlayersViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.bind(
            binding.playernameEt.onTextChanged(),
            binding.nextBtn.clicks()
        )

        viewModel.sharedUserAdded
            .filter { findNavController().currentDestination?.id == R.id.addPlayersFragment }
            .onEach { findNavController().popBackStack() }
            .launchIn(lifecycleScope)
    }

}