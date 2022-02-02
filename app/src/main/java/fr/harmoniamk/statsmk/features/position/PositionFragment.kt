package fr.harmoniamk.statsmk.features.position

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.databinding.FragmentPositionBinding
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.extension.clicks
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class PositionFragment(val onPosition: MutableSharedFlow<Int>) :
    Fragment(R.layout.fragment_position) {

    private val binding: FragmentPositionBinding by viewBinding()
    private val viewModel: PositionViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.bind(
            binding.pos1.clicks(),
            binding.pos2.clicks(),
            binding.pos3.clicks(),
            binding.pos4.clicks(),
            binding.pos5.clicks(),
            binding.pos6.clicks(),
            binding.pos7.clicks(),
            binding.pos8.clicks(),
            binding.pos9.clicks(),
            binding.pos10.clicks(),
            binding.pos11.clicks(),
            binding.pos12.clicks()
        )
        viewModel.sharedPos.bind(onPosition, lifecycleScope)
    }
}