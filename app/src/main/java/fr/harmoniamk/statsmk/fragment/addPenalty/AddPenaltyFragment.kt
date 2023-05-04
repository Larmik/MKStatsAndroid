package fr.harmoniamk.statsmk.fragment.addPenalty

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.databinding.FragmentAddPenaltyBinding
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.extension.clicks
import fr.harmoniamk.statsmk.extension.dismiss
import fr.harmoniamk.statsmk.extension.onTextChanged
import fr.harmoniamk.statsmk.model.firebase.NewWar
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

@FlowPreview
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class AddPenaltyFragment(val war: NewWar? = null) : BottomSheetDialogFragment() {

    lateinit var binding: FragmentAddPenaltyBinding
    private val viewModel: AddPenaltyViewModel by viewModels()
    val onDismiss = MutableSharedFlow<Unit>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentAddPenaltyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        war?.let {
            viewModel.bind(
                war = it,
                onTeamSelected = flowOf(
                    binding.team1Label.clicks().mapNotNull { war.teamHost },
                    binding.team2Label.clicks().mapNotNull { war.teamOpponent },
                ).flattenMerge(),
                onAmountAdded = binding.addPenaltyEdit.onTextChanged(),
                onPenaltyClick = binding.addPenaltyBtn.clicks()
            )

            viewModel.sharedTeam1Label
                .onEach { binding.team1Label.text = it }
                .launchIn(lifecycleScope)

            viewModel.sharedTeam2Label
                .onEach { binding.team2Label.text = it }
                .launchIn(lifecycleScope)

            viewModel.sharedTeam1Selected
                .onEach {
                    binding.team1Label.setBackgroundColor(
                        ContextCompat.getColor(requireContext(),
                            when (it) {
                                true -> R.color.harmonia_dark
                                else -> R.color.transparent
                            })

                    )
                    binding.team1Label.setTextColor(
                        ContextCompat.getColor(requireContext(),
                            when (it) {
                                true -> R.color.white
                                else -> R.color.harmonia_dark
                            })
                    )
                    binding.team2Label.setBackgroundColor(
                        ContextCompat.getColor(requireContext(),
                            when (it) {
                                false -> R.color.harmonia_dark
                                else -> R.color.transparent
                            })

                    )
                    binding.team2Label.setTextColor(
                        ContextCompat.getColor(requireContext(),
                            when (it) {
                                false -> R.color.white
                                else -> R.color.harmonia_dark
                            })
                    )
                }.launchIn(lifecycleScope)

            flowOf(viewModel.sharedDismiss, dialog.dismiss()).flattenMerge().bind(onDismiss, lifecycleScope)
        }


    }

}