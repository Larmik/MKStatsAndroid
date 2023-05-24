package fr.harmoniamk.statsmk.fragment.scheduleWar

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import fr.harmoniamk.statsmk.databinding.FragmentScheduleWarBinding
import fr.harmoniamk.statsmk.extension.clicks
import fr.harmoniamk.statsmk.extension.onTextChanged
import fr.harmoniamk.statsmk.extension.safeSubList
import fr.harmoniamk.statsmk.fragment.teamSelect.TeamListAdapter
import fr.harmoniamk.statsmk.model.firebase.WarDispo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*


@FlowPreview
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class ScheduleWarFragment(val dispo: WarDispo): BottomSheetDialogFragment() {

    lateinit var binding: FragmentScheduleWarBinding
    private val viewModel: ScheduleWarViewModel by viewModels()
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
        binding = FragmentScheduleWarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val behavior = BottomSheetBehavior.from(view.parent as View)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        val luFirstHalfAdapter = ScheduleLineUpAdapter()
        val luSecondtHalfAdapter = ScheduleLineUpAdapter()
        val teamAdapter = TeamListAdapter()
        binding.firstHalfLu.adapter = luFirstHalfAdapter
        binding.secondHalfLu.adapter = luSecondtHalfAdapter
        binding.teamRv.adapter = teamAdapter
        viewModel.bind(
            dispo = dispo,
            onSearch = binding.searchEt.onTextChanged(),
            onTeamClick = teamAdapter.onTeamClick,
            onPlayerDelete = flowOf(luFirstHalfAdapter.onPlayerDelete, luSecondtHalfAdapter.onPlayerDelete).flattenMerge(),
            onWarScheduled = binding.confirmLuBtn.clicks()
        )
        viewModel.sharedChosenLU
            .onEach {
                luSecondtHalfAdapter.addUsers(it.safeSubList(0, 3), it)
                luFirstHalfAdapter.addUsers(it.safeSubList(3, it.size), it)
            }.launchIn(lifecycleScope)
        viewModel.sharedTeams.onEach { teamAdapter.addTeams(it) }.launchIn(lifecycleScope)
        viewModel.sharedButtonVisible
            .onEach { binding.confirmLuBtn.isEnabled = it }
            .launchIn(lifecycleScope)
        viewModel.sharedDismiss
            .onEach { dismiss() }
            .launchIn(lifecycleScope)
    }
}