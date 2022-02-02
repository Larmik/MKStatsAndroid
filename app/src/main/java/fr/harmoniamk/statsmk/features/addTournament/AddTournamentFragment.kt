package fr.harmoniamk.statsmk.features.addTournament

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.databinding.FragmentAddTournamentBinding
import fr.harmoniamk.statsmk.extension.checks
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
class AddTournamentFragment : Fragment(R.layout.fragment_add_tournament) {

    private val binding: FragmentAddTournamentBinding by viewBinding()
    private val viewModel: AddTournamentViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.bind(
            onNameAdded = binding.tmNameEt.onTextChanged(),
            onTotalTrackAdded = binding.trackNumberRg.checks(),
            onDifficultyAdded = binding.difficultyRg.checks(),
            onButtonClick = binding.startBtn.clicks()
        )
        viewModel.sharedClose
            .filter { findNavController().currentDestination?.id == R.id.addTournamentFragment }
            .onEach { findNavController().popBackStack() }
            .launchIn(lifecycleScope)
    }

}