package fr.harmoniamk.statsmk.features.home.war

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.databinding.FragmentWarBinding
import fr.harmoniamk.statsmk.extension.clicks
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import fr.harmoniamk.statsmk.extension.onTextChanged

@FlowPreview
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class WarFragment : Fragment(R.layout.fragment_war) {

    private val binding: FragmentWarBinding by viewBinding()
    private val viewModel: WarViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.bind(binding.teamCodeEt.onTextChanged(), binding.nextBtn.clicks())

        viewModel.sharedTeam
            .onEach {
                binding.nextBtn.visibility = View.INVISIBLE
                it?.let {
                    binding.nextBtn.visibility = View.VISIBLE
                    binding.nextBtn.text = "Intégrer ${it.name} (${it.shortName})"
                }
            }
            .launchIn(lifecycleScope)

        viewModel.sharedHasTeam
            .onEach {
                binding.noTeamLayout.isVisible = it == null
                binding.mainWarLayout.isVisible = it != null
                binding.currentTeamTv.text = it?.name
            }.launchIn(lifecycleScope)

    }



}