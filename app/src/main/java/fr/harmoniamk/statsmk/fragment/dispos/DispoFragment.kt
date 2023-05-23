package fr.harmoniamk.statsmk.fragment.dispos

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.databinding.FragmentDispoBinding
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
@FlowPreview
@ExperimentalCoroutinesApi
class DispoFragment : Fragment(R.layout.fragment_dispo) {

    private val binding: FragmentDispoBinding by viewBinding()
    private val viewModel: DispoViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = DispoAdapter()
        binding.dispoList.adapter = adapter
        viewModel.bind(adapter.sharedDispoSelected, flowOf(), adapter.onClickWarSchedule)
        viewModel.sharedDispo
            .onEach {
                adapter.addData(it)
            }.launchIn(lifecycleScope)
    }

}