package fr.harmoniamk.statsmk.fragment.resetPassword

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import fr.harmoniamk.statsmk.databinding.FragmentResetPasswordBinding
import fr.harmoniamk.statsmk.extension.clicks
import fr.harmoniamk.statsmk.extension.onTextChanged
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class ResetPasswordFragment : DialogFragment() {
    lateinit var binding: FragmentResetPasswordBinding
    private val viewModel: ResetPasswordViewModel by viewModels()

    // dialog view is created
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentResetPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    //dialog view is ready
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        viewModel.bind(binding.emailEt.onTextChanged(), binding.resetBtn.clicks(), binding.cancelBtn.clicks())
        viewModel.sharedDismiss
            .onEach { dismiss() }
            .launchIn(lifecycleScope)
        viewModel.sharedToast
            .onEach { Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show() }
            .launchIn(lifecycleScope)
    }
}