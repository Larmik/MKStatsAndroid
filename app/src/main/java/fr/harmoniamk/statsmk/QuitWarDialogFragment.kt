package fr.harmoniamk.statsmk

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
import fr.harmoniamk.statsmk.databinding.QuitWarDialogFragmentBinding
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.extension.clicks
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@ExperimentalCoroutinesApi
@FlowPreview
@AndroidEntryPoint
class QuitWarDialogFragment() : AppCompatDialogFragment() {

    private val binding: QuitWarDialogFragmentBinding by viewBinding()
    private val viewModel: QuitWarViewModel by viewModels()

    private val _sharedClose = MutableSharedFlow<Unit>()
    private val _sharedWarLeft = MutableSharedFlow<Unit>()

    val sharedWarLeft = _sharedWarLeft.asSharedFlow()
    val sharedClose = _sharedClose.asSharedFlow()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(
        R.layout.quit_war_dialog_fragment, container, false
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.bind(
            onQuit = binding.quitWarBtn.clicks(),
            onBack = binding.cancelBtn.clicks()
        )
        viewModel.onDismiss.bind(_sharedClose, lifecycleScope)
        viewModel.onWarQuit.onEach {
            dismissAllowingStateLoss()
            _sharedWarLeft.emit(Unit)
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