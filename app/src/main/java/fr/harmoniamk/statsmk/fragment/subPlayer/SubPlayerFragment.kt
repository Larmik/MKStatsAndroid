package fr.harmoniamk.statsmk.fragment.subPlayer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import fr.harmoniamk.statsmk.databinding.FragmentSubPlayerBinding
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.extension.clicks
import fr.harmoniamk.statsmk.extension.onTextChanged
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*


@ExperimentalCoroutinesApi
@FlowPreview
@AndroidEntryPoint
class SubPlayerFragment : BottomSheetDialogFragment() {

    lateinit var binding: FragmentSubPlayerBinding
    private val viewModel: SubPlayerViewModel by viewModels()

    val sharedDismiss = MutableSharedFlow<Unit>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentSubPlayerBinding.inflate(inflater, container, false)
        dialog?.setCancelable(false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val currentAdapter = SubPlayerAdapter()
        val otherAdapter = SubPlayerAdapter()
        binding.currentPlayersRv.adapter = currentAdapter
        binding.newPlayersRv.adapter = otherAdapter
        viewModel.bind(
            onSubClick = binding.subPlayersBtn.clicks(),
            onCancel = flowOf(binding.cancelBtn.clicks(), binding.backBtn.clicks()).flattenMerge(),
            onOldPlayerSelect = currentAdapter.sharedUserSelected.mapNotNull { it.user },
            onNewPlayerSelect = otherAdapter.sharedUserSelected.mapNotNull { it.user },
            onSearch = binding.searchSubEt.onTextChanged(),
            onNextClick = binding.nextBtn.clicks()
        )
        viewModel.sharedCurrentPlayers
            .onEach {
                currentAdapter.addUsers(it)
            }.launchIn(lifecycleScope)
        viewModel.sharedOtherPlayers
            .onEach {
                otherAdapter.addUsers(it)
            }
            .launchIn(lifecycleScope)

        viewModel.sharedPlayerSelected
            .onEach {
                binding.currentPlayerLayout.isVisible = false
                binding.newPlayerLayout.isVisible = true
                binding.subLabel.text = "Sélectionnez le joueur entrant"
                binding.subPlayersBtn.text = "Remplacer ${it.name}"
            }.launchIn(lifecycleScope)

        viewModel.sharedDismissDialog.bind(sharedDismiss, lifecycleScope)
    }

}