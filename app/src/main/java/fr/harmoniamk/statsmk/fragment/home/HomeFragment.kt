package fr.harmoniamk.statsmk.fragment.home

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.databinding.FragmentHomeBinding
import fr.harmoniamk.statsmk.extension.backPressedDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@ExperimentalCoroutinesApi
@FlowPreview
@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private val binding: FragmentHomeBinding by viewBinding()
    private val viewModel: HomeViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val inputManager = requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.hideSoftInputFromWindow(binding.root.windowToken, 0)
        val adapter = HomePagerAdapter(requireActivity())
        binding.homepager.adapter = adapter
        binding.homepager.currentItem = 0
        binding.homepager.isUserInputEnabled = false
        TabLayoutMediator(binding.tablayout, binding.homepager) { tab, position ->
            tab.text = adapter.getTabTitle(position)
        }.attach()
        viewModel.bind(requireActivity().backPressedDispatcher(viewLifecycleOwner))
        viewModel.sharedClose
            .onEach { requireActivity().finish() }
            .launchIn(lifecycleScope)
    }
}