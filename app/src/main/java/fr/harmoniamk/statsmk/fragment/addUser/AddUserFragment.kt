package fr.harmoniamk.statsmk.fragment.addUser

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
import fr.harmoniamk.statsmk.databinding.FragmentAddUserBinding
import fr.harmoniamk.statsmk.extension.backPressedDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import fr.harmoniamk.statsmk.extension.clicks
import fr.harmoniamk.statsmk.extension.onTextChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@ExperimentalCoroutinesApi
@FlowPreview
@AndroidEntryPoint
class AddUserFragment : Fragment(R.layout.fragment_add_user) {

    private val binding: FragmentAddUserBinding by viewBinding()
    private val viewModel: AddUserViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.bind(
            onName = binding.nameEt.onTextChanged(),
            onEmail = binding.emailEt.onTextChanged(),
            onPassword = binding.passwordEt.onTextChanged(),
            onNext = binding.nextBtn.clicks(),
            onSigninClick = binding.connectBtn.clicks()
        )

        viewModel.sharedNext
            .filter { findNavController().currentDestination?.id == R.id.addUserFragment }
            .onEach { findNavController().navigate(AddUserFragmentDirections.goToHome()) }
            .launchIn(lifecycleScope)
        viewModel.sharedToast
            .onEach {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }.launchIn(lifecycleScope)
        viewModel.sharedGoToConnect
            .filter { findNavController().currentDestination?.id == R.id.addUserFragment }
            .onEach { findNavController().navigate(AddUserFragmentDirections.goToConnect()) }
            .launchIn(lifecycleScope)

        requireActivity().backPressedDispatcher(viewLifecycleOwner)
            .onEach { requireActivity().finish() }
            .launchIn(lifecycleScope)

        viewModel.sharedButtonEnabled
            .onEach { binding.nextBtn.isEnabled = it }
            .launchIn(lifecycleScope)

    }

}