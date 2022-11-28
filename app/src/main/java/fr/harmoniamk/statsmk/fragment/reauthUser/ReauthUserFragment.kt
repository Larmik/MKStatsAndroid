package fr.harmoniamk.statsmk.fragment.reauthUser

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
import fr.harmoniamk.statsmk.databinding.FragmentReauthUserBinding
import fr.harmoniamk.statsmk.extension.clicks
import fr.harmoniamk.statsmk.extension.onTextChanged
import fr.harmoniamk.statsmk.fragment.resetPassword.ResetPasswordFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

@ExperimentalCoroutinesApi
@FlowPreview
@AndroidEntryPoint
class ReauthUserFragment: Fragment(R.layout.fragment_reauth_user) {

    private val binding: FragmentReauthUserBinding by viewBinding()
    private val viewModel: ReauthUserViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.bind(
            binding.passwordEt.onTextChanged(),
            binding.connectBtn.clicks(),
            binding.forgotPasswordButton.clicks()
        )

        viewModel.sharedNext
            .filter { findNavController().currentDestination?.id == R.id.reauthUserFragment }
            .onEach { findNavController().navigate(ReauthUserFragmentDirections.goToHome()) }
            .launchIn(lifecycleScope)

        viewModel.sharedToast
            .onEach {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }.launchIn(lifecycleScope)

        viewModel.sharedGoToReset
            .map { ResetPasswordFragment() }
            .onEach { it.show(childFragmentManager, null) }
            .launchIn(lifecycleScope)

        viewModel.sharedButtonEnabled
            .onEach { binding.connectBtn.isEnabled = it }
            .launchIn(lifecycleScope)

    }

}