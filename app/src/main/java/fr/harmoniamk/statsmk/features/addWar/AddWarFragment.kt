package fr.harmoniamk.statsmk.features.addWar

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.databinding.FragmentAddWarBinding
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@FlowPreview
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class AddWarFragment : Fragment(R.layout.fragment_add_war) {

    private val binding: FragmentAddWarBinding by viewBinding()
    private val viewModel: AddWarViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = AddWarPagerAdapter(requireActivity())
        binding.pager.adapter = adapter
        binding.pager.currentItem = 0
        binding.pager.isUserInputEnabled = false

        viewModel.bind(adapter.onTeamSelected, adapter.onWarCreated, adapter.onUsersSelected)

        viewModel.sharedTeamSelected.onEach {
            binding.pager.currentItem = 1
            binding.title.text = "Nouvelle war : $it"
        }.launchIn(lifecycleScope)

        viewModel.sharedStarted
            .filter { findNavController().currentDestination?.id == R.id.addWarFragment }
            .onEach { findNavController().popBackStack() }
            .launchIn(lifecycleScope)

        viewModel.sharedAlreadyCreated
            .onEach {
                Toast.makeText(requireContext(), "Une war a déjà été créée, retournez sur l'écran précédent pour y accéder.", Toast.LENGTH_SHORT).show()
            }.launchIn(lifecycleScope)
    }

}