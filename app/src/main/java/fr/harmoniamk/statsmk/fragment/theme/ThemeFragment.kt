package fr.harmoniamk.statsmk.fragment.theme

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.databinding.FragmentThemeBinding
import fr.harmoniamk.statsmk.extension.clicks
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach


@ExperimentalCoroutinesApi
@FlowPreview
@AndroidEntryPoint
class ThemeFragment : Fragment(R.layout.fragment_theme) {

    private val binding: FragmentThemeBinding by viewBinding()
    private val viewModel: ThemeViewModel by viewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.bind(
            binding.marioTheme.clicks(),
            binding.luigiTheme.clicks(),
            binding.warioTheme.clicks(),
            binding.waluigiTheme.clicks()
        )

        viewModel.onThemeSelected
            .onEach {
                val packageManager = requireContext().packageManager
                val intent = packageManager.getLaunchIntentForPackage(requireContext().packageName)
                val componentName = intent?.component
                val mainIntent = Intent.makeRestartActivityTask(componentName)
                requireContext().startActivity(mainIntent)
            }.launchIn(lifecycleScope)
    }

}