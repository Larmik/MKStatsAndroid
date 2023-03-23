package fr.harmoniamk.statsmk.fragment.connectUser

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.databinding.FragmentConnectUserBinding
import fr.harmoniamk.statsmk.extension.backPressedDispatcher
import fr.harmoniamk.statsmk.extension.clicks
import fr.harmoniamk.statsmk.extension.onTextChanged
import fr.harmoniamk.statsmk.fragment.resetPassword.ResetPasswordFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

@FlowPreview
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class ConnectUserFragment: Fragment(R.layout.fragment_connect_user) {

    private val binding: FragmentConnectUserBinding by viewBinding()
    private val viewModel: ConnectUserViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.bind(
            onEmail = binding.emailEt.onTextChanged(),
            onPassword = binding.passwordEt.onTextChanged(),
            onConnect = binding.connectBtn.clicks(),
            onSignupClick = binding.signupBtn.clicks(),
            onResetPassword = binding.forgotPasswordButton.clicks()
        )

        viewModel.sharedNext
            .filter { findNavController().currentDestination?.id == R.id.connectUserFragment }
            .onEach { findNavController().navigate(ConnectUserFragmentDirections.goToHome()) }
            .launchIn(lifecycleScope)

        viewModel.sharedToast
            .onEach {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }.launchIn(lifecycleScope)

        viewModel.sharedGoToSignup
            .filter { findNavController().currentDestination?.id == R.id.connectUserFragment }
            .onEach { findNavController().navigate(ConnectUserFragmentDirections.goToSignup()) }
            .launchIn(lifecycleScope)

        viewModel.sharedButtonEnabled
            .onEach { binding.connectBtn.isEnabled = it }
            .launchIn(lifecycleScope)

        requireActivity().backPressedDispatcher(viewLifecycleOwner)
            .onEach { requireActivity().finish() }
            .launchIn(lifecycleScope)

        viewModel.sharedGoToReset
            .map { ResetPasswordFragment() }
            .onEach { it.show(childFragmentManager, null) }
            .launchIn(lifecycleScope)

        viewModel.sharedLoading
            .onEach {
                binding.progress.isVisible = it
                binding.mainLayout.isVisible = !it
            }.launchIn(lifecycleScope)
    }

}