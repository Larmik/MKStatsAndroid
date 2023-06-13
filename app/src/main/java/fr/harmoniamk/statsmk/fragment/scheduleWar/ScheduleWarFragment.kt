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
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.databinding.FragmentScheduleWarBinding
import fr.harmoniamk.statsmk.extension.clicks
import fr.harmoniamk.statsmk.extension.onTextChanged
import fr.harmoniamk.statsmk.extension.safeSubList
import fr.harmoniamk.statsmk.fragment.popup.PopupFragment
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
    private var teamHostPopup = PopupFragment(positiveText = R.string.valider)
    private var opponentHostPopuup = PopupFragment(positiveText= R.string.valider, editTextHint = R.string.code_ami)

    private var teamPopupShowing = false
    private var opponentPopupShowing = false

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
            onWarScheduled = binding.confirmLuBtn.clicks(),
            onOpponentHostClick = binding.opponentHostBtn.clicks(),
            onTeamHostClick = binding.teamHostBtn.clicks()
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

        viewModel.sharedShowTeamHostPopup
            .onEach {
                when (it != null) {
                    true -> {
                        if (!teamHostPopup.isAdded && !teamPopupShowing) {
                            teamHostPopup = PopupFragment(positiveText = R.string.valider, playerList = it)
                            viewModel.bindTeamPopup(onPlayerSelected = teamHostPopup.onPlayerSelected, onValidate = teamHostPopup.onPositiveClick, onDismiss = teamHostPopup.onNegativeClick)
                            teamHostPopup.show(childFragmentManager, null)
                            teamPopupShowing = true
                        }
                    }
                    else -> {
                        teamHostPopup.dismiss()
                        teamPopupShowing = false
                    }
                }
            }
            .launchIn(lifecycleScope)

        viewModel.sharedShowOpponentHostPopup
            .onEach {
                when (it) {
                    true -> {
                        if (!opponentHostPopuup.isAdded && !opponentPopupShowing) {
                            opponentHostPopuup = PopupFragment(positiveText= R.string.valider, editTextHint = R.string.code_ami, isFcCode = true)
                            viewModel.bindOpponentPopup(onCodeAdded = opponentHostPopuup.onTextChange, onValidate = opponentHostPopuup.onPositiveClick, onDismiss = opponentHostPopuup.onNegativeClick)
                            opponentHostPopuup.show(childFragmentManager, null)
                            opponentPopupShowing = true
                        }
                    }
                    else -> {
                        opponentHostPopuup.dismiss()
                        opponentPopupShowing = false
                    }
                }
            }
            .launchIn(lifecycleScope)

        viewModel.sharedChosenHost
            .onEach {
                binding.hostName.text = it
            }.launchIn(lifecycleScope)
    }
}