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
import fr.harmoniamk.statsmk.extension.clicks
import fr.harmoniamk.statsmk.fragment.popup.PopupFragment
import fr.harmoniamk.statsmk.fragment.scheduleWar.ScheduleWarFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
@FlowPreview
@ExperimentalCoroutinesApi
class DispoFragment : Fragment(R.layout.fragment_dispo) {

    private val binding: FragmentDispoBinding by viewBinding()
    private val viewModel: DispoViewModel by viewModels()
    private var popup = PopupFragment(message = "", positiveText = "Valider", editTextHint = "Ajouter une précision...")
    private var popupShowing = false


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = DispoAdapter()
        binding.dispoList.adapter = adapter
        viewModel.bind(
            onDispoSelected = adapter.sharedDispoSelected,
            onClickWarSchedule = adapter.onClickWarSchedule,
            onClickOtherPlayer = adapter.onClickOtherPlayer,
            onPopup = binding.detailsBtn.clicks()
        )
        viewModel.sharedDispo
            .onEach {
                adapter.addData(it)
                it.lastOrNull()?.details?.let {
                    binding.dispoDetails.text = it
                }
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

        viewModel.sharedPopupShowing
            .onEach {
                when (it) {
                    true -> {
                        if (!popup.isAdded && !popupShowing) {
                            popup = PopupFragment(message = "Ajoute un message pour ton équipe !", positiveText = "Valider", editTextHint = "Ajouter une précision...")
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