package fr.harmoniamk.statsmk.features.currentTournament.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.database.model.Tournament
import fr.harmoniamk.statsmk.databinding.DeleteTournamentDialogFragmentBinding
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.extension.clicks
import fr.harmoniamk.statsmk.features.currentTournament.viewmodel.DeleteTournamentViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@FlowPreview
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class DeleteTournamentDialogFragment(val tm: Tournament) : AppCompatDialogFragment() {

    private val binding: DeleteTournamentDialogFragmentBinding by viewBinding()
    private val viewModel: DeleteTournamentViewModel by viewModels()

    private val _sharedClose = MutableSharedFlow<Unit>()
    private val _sharedTmDeleted = MutableSharedFlow<Unit>()

    val sharedTmDeleted = _sharedTmDeleted.asSharedFlow()
    val sharedClose = _sharedClose.asSharedFlow()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(
        R.layout.delete_tournament_dialog_fragment, container, false
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.bind(
            tm = tm,
            onDelete = binding.deleteTmBtn.clicks(),
            onBack = binding.cancelBtn.clicks()
        )
        viewModel.onDismiss.bind(_sharedClose, lifecycleScope)
        viewModel.onTournamentDeleted.onEach {
            dismissAllowingStateLoss()
            _sharedTmDeleted.emit(Unit)
        }.launchIn(lifecycleScope)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
    }

}