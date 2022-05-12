package fr.harmoniamk.statsmk.fragment.manageTeams

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.databinding.FragmentAddTeamBinding
import fr.harmoniamk.statsmk.extension.clicks
import fr.harmoniamk.statsmk.extension.onTextChanged
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class AddTeamFragment : Fragment(R.layout.fragment_add_team) {

    private val binding: FragmentAddTeamBinding by viewBinding()
    private val viewModel: AddTeamViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.bind(
            binding.teamnameEt.onTextChanged(),
            binding.shortnameEt.onTextChanged(),
            binding.codeEt.onTextChanged(),
            binding.nextBtn.clicks()
        )
        viewModel.sharedTeamAdded
            .filter { findNavController().currentDestination?.id == R.id.addTeamFragment }
            .onEach { findNavController().popBackStack() }
            .launchIn(lifecycleScope)
    }

}