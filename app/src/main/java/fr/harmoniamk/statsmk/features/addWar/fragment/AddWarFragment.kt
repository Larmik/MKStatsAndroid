package fr.harmoniamk.statsmk.features.addWar.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.databinding.FragmentAddWarBinding
import fr.harmoniamk.statsmk.features.addWar.adapter.AddWarPagerAdapter
import fr.harmoniamk.statsmk.features.addWar.viewmodel.AddWarViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@FlowPreview
@ExperimentalCoroutinesApi
class AddWarFragment : Fragment(R.layout.fragment_add_war) {

    private val binding: FragmentAddWarBinding by viewBinding()
    private val viewModel: AddWarViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = AddWarPagerAdapter(requireActivity())
        binding.addWarPager.isUserInputEnabled = false
        binding.addWarPager.adapter = adapter
        binding.addWarPager.currentItem = 0
        viewModel.bind(adapter.onCreateWar)
        viewModel.sharedGoToWait
            .onEach { binding.addWarPager.currentItem = 1 }
            .launchIn(lifecycleScope)

    }

}