package fr.harmoniamk.statsmk.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.adapter.HomePagerAdapter
import fr.harmoniamk.statsmk.databinding.FragmentHomeBinding
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@ExperimentalCoroutinesApi
@FlowPreview
@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private val binding: FragmentHomeBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = HomePagerAdapter(requireActivity())
        binding.homepager.adapter = adapter
        binding.homepager.currentItem = 0
        binding.homepager.isUserInputEnabled = false
        TabLayoutMediator(binding.tablayout, binding.homepager) { tab, position ->
            tab.text = adapter.getTabTitle(position)
        }.attach()
    }
}