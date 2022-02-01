package fr.harmoniamk.statsmk.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.databinding.FragmentAddUserBinding
import fr.harmoniamk.statsmk.viewModel.AddUserViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.extension.clicks
import fr.harmoniamk.statsmk.extension.onTextChanged
import kotlinx.coroutines.flow.MutableSharedFlow



@ExperimentalCoroutinesApi
@FlowPreview
@AndroidEntryPoint
class AddUserFragment(val onNext: MutableSharedFlow<Unit>) : Fragment(R.layout.fragment_add_user) {

    private val binding: FragmentAddUserBinding by viewBinding()
    private val viewModel: AddUserViewModel by viewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.bind(
            binding.usernameEt.onTextChanged(),
            binding.codeEt.onTextChanged(),
            binding.nextBtn.clicks(),
        )

        viewModel.sharedNext.bind(onNext, lifecycleScope)
    }

}