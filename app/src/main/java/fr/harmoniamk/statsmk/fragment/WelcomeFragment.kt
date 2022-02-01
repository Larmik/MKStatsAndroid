package fr.harmoniamk.statsmk.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.adapter.WelcomePagerAdapter
import fr.harmoniamk.statsmk.databinding.FragmentWelcomeBinding
import fr.harmoniamk.statsmk.viewModel.WelcomeViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@ExperimentalCoroutinesApi
@FlowPreview
@AndroidEntryPoint
class WelcomeFragment : Fragment(R.layout.fragment_welcome) {

    private val binding: FragmentWelcomeBinding by viewBinding()
    private val viewModel: WelcomeViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = WelcomePagerAdapter(requireActivity())
        binding.welcomepager.adapter = adapter
        binding.welcomepager.currentItem = 0
        binding.welcomepager.isUserInputEnabled = false

        viewModel.bind(adapter.onNext, adapter.onFinish)

        viewModel.sharedNext
            .onEach { binding.welcomepager.currentItem = 1 }
            .launchIn(lifecycleScope)

        viewModel.sharedFinish
            .filter { findNavController().currentDestination?.id == R.id.welcomeFragment }
            .onEach { findNavController().navigate(WelcomeFragmentDirections.goToHome()) }
            .launchIn(lifecycleScope)

    }
}