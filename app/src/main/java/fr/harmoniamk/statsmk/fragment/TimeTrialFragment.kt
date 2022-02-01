package fr.harmoniamk.statsmk.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.databinding.FragmentTimeTrialBinding
import fr.harmoniamk.statsmk.extension.clicks
import fr.harmoniamk.statsmk.extension.onTextChanged
import fr.harmoniamk.statsmk.viewModel.TimeTrialViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@FlowPreview
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class TimeTrialFragment : Fragment(R.layout.fragment_time_trial) {

    private val binding: FragmentTimeTrialBinding by viewBinding()
    private val viewModel: TimeTrialViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.bind(binding.teamCodeEt.onTextChanged(), binding.nextBtn.clicks())


        viewModel.sharedTeam
            .onEach {
                binding.nextBtn.visibility = View.INVISIBLE
                it?.let {
                    binding.nextBtn.visibility = View.VISIBLE
                    binding.nextBtn.text = "Intégrer ${it.shortName} - ${it.name}"
                }
            }
            .launchIn(lifecycleScope)

    }

}