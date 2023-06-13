package fr.harmoniamk.statsmk.fragment.dispos

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.databinding.FragmentDispoBinding
import fr.harmoniamk.statsmk.extension.checks
import fr.harmoniamk.statsmk.extension.clicks
import fr.harmoniamk.statsmk.fragment.popup.PopupFragment
import fr.harmoniamk.statsmk.fragment.scheduleWar.ScheduleWarFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

@AndroidEntryPoint
@FlowPreview
@ExperimentalCoroutinesApi
class DispoFragment : Fragment(R.layout.fragment_dispo) {

    private val binding: FragmentDispoBinding by viewBinding()
    private val viewModel: DispoViewModel by viewModels()
    private var popup = PopupFragment(message = R.string.add_details_dispo_title, positiveText = R.string.valider, editTextHint = R.string.add_details)
    private var popupShowing = false


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = DispoAdapter()
        binding.dispoList.adapter = adapter
        viewModel.bind(
            onDispoSelected = adapter.sharedDispoSelected,
            onClickWarSchedule = adapter.onClickWarSchedule,
            onClickOtherPlayer = adapter.onClickOtherPlayer,
            onPopup = binding.dispoDetails.clicks()
        )
        viewModel.sharedDispo
            .onEach {
                adapter.addData(it)
                it.lastOrNull()?.first?.details?.let {
                    binding.dispoDetails.text = it
                }
                binding.dispoRadiogroup.isVisible = it.any { !it.first.lineUp.isNullOrEmpty() }
            }.launchIn(lifecycleScope)
        viewModel.sharedGoToScheduleWar
            .onEach {
                val fragment = ScheduleWarFragment(dispo = it)
                if (!fragment.isAdded) fragment.show(childFragmentManager, null)
            }.launchIn(lifecycleScope)

        viewModel.sharedShowOtherPlayers
            .onEach {
                val fragment = OtherPlayersFragment(dispo = it)
                if (!fragment.isAdded) fragment.show(childFragmentManager, null)
            }.launchIn(lifecycleScope)

        flowOf(binding.radioDisposOnly.checks().map { true }, binding.radioDisposLu.checks().map { false })
            .flattenMerge()
            .onEach { adapter.switchView(it) }
            .launchIn(lifecycleScope)

        viewModel.sharedPopupShowing
            .onEach {
                when (it) {
                    true -> {
                        if (!popup.isAdded && !popupShowing) {
                            popup = PopupFragment(message = R.string.add_details_dispo_title, positiveText = R.string.valider, editTextHint = R.string.add_details)
                            viewModel.bindPopup(onDetailsValidated = popup.onPositiveClick, onDetailsAdded = popup.onTextChange, onDismiss = popup.onNegativeClick)
                            popup.show(childFragmentManager, null)
                            popupShowing = true
                        }
                    }
                    else -> {
                        popup.dismiss()
                        popupShowing = false
                    }
                }
            }.launchIn(lifecycleScope)
    }

}