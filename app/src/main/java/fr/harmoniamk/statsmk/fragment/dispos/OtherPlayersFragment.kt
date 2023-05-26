package fr.harmoniamk.statsmk.fragment.dispos

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import fr.harmoniamk.statsmk.databinding.FragmentOtherPlayersBinding
import fr.harmoniamk.statsmk.extension.clicks
import fr.harmoniamk.statsmk.fragment.playerSelect.PlayerListAdapter
import fr.harmoniamk.statsmk.model.firebase.WarDispo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

@ExperimentalCoroutinesApi
@FlowPreview
@AndroidEntryPoint
class OtherPlayersFragment(val dispo: WarDispo): BottomSheetDialogFragment() {

    lateinit var binding: FragmentOtherPlayersBinding
    private val viewModel: OtherPlayersViewModel by viewModels()

    val onDismiss = MutableSharedFlow<Unit>()
    var dialog: BottomSheetDialog? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        return dialog!!
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOtherPlayersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val behavior = BottomSheetBehavior.from(view.parent as View)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED

        val playersAdapter = PlayerListAdapter(singleSelection = true)
        binding.otherPalyersRv.adapter = playersAdapter
        viewModel.bind(
            dispo = dispo,
            onPlayerSelected = playersAdapter.sharedUserSelected,
            onButtonClick = flowOf(binding.otherCanBtn.clicks().map { 0 }, binding.otherCanSubBtn.clicks().map { 1 }).flattenMerge()
        )

        viewModel.sharedPlayers
            .onEach { playersAdapter.addUsers(it) }
            .launchIn(lifecycleScope)

        viewModel.sharedDismiss
            .onEach { dismiss() }
            .launchIn(lifecycleScope)

        viewModel.sharedLoading
            .onEach {
                binding.btnLayout.isVisible = false
                binding.progress.isVisible = true
            }.launchIn(lifecycleScope)

    }
}