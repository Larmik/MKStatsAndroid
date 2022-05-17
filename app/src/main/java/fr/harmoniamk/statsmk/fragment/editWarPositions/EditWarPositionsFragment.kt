package fr.harmoniamk.statsmk.fragment.editWarPositions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import fr.harmoniamk.statsmk.databinding.FragmentEditWarPositionsBinding
import fr.harmoniamk.statsmk.extension.backPressedDispatcher
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.extension.clicks
import fr.harmoniamk.statsmk.model.firebase.NewWar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.coroutines.CoroutineContext

@FlowPreview
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class EditWarPositionsFragment(val newWar: NewWar, val index: Int = 0) : BottomSheetDialogFragment(), CoroutineScope {

    lateinit var binding: FragmentEditWarPositionsBinding
    private val viewModel: EditWarPositionsViewModel by viewModels()

    val onDismiss = MutableSharedFlow<Unit>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentEditWarPositionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.bind(
            war = newWar,
            index = index,
            onPos1 = binding.pos1.clicks(),
            onPos2 = binding.pos2.clicks(),
            onPos3 = binding.pos3.clicks(),
            onPos4 = binding.pos4.clicks(),
            onPos5 = binding.pos5.clicks(),
            onPos6 = binding.pos6.clicks(),
            onPos7 = binding.pos7.clicks(),
            onPos8 = binding.pos8.clicks(),
            onPos9 = binding.pos9.clicks(),
            onPos10 = binding.pos10.clicks(),
            onPos11 = binding.pos11.clicks(),
            onPos12 = binding.pos12.clicks())

        viewModel.sharedDismiss.bind(onDismiss, this)

        viewModel.sharedSelectedPositions
            .onEach {
                showAllPositions()
                it.forEach { pos -> hidePosition(pos) }
            }.launchIn(lifecycleScope)

        viewModel.sharedPlayerLabel
            .onEach { binding.posTitle.text = "Sélectionnez la position de $it" }
            .launchIn(lifecycleScope)
    }

    private fun showAllPositions() {
        binding.pos1.isVisible = true
        binding.pos2.isVisible = true
        binding.pos3.isVisible = true
        binding.pos4.isVisible = true
        binding.pos5.isVisible = true
        binding.pos6.isVisible = true
        binding.pos7.isVisible = true
        binding.pos8.isVisible = true
        binding.pos9.isVisible = true
        binding.pos10.isVisible = true
        binding.pos11.isVisible = true
        binding.pos12.isVisible = true
    }

    private fun hidePosition(position: Int) = when (position) {
        1 -> binding.pos1.visibility = View.INVISIBLE
        2 -> binding.pos2.visibility = View.INVISIBLE
        3 -> binding.pos3.visibility = View.INVISIBLE
        4 -> binding.pos4.visibility = View.INVISIBLE
        5 -> binding.pos5.visibility = View.INVISIBLE
        6 -> binding.pos6.visibility = View.INVISIBLE
        7 -> binding.pos7.visibility = View.INVISIBLE
        8 -> binding.pos8.visibility = View.INVISIBLE
        9 -> binding.pos9.visibility = View.INVISIBLE
        10 -> binding.pos10.visibility = View.INVISIBLE
        11 -> binding.pos11.visibility = View.INVISIBLE
        12 -> binding.pos12.visibility = View.INVISIBLE
        else -> {}
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main
}