package fr.harmoniamk.statsmk.fragment.editWarShocks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import fr.harmoniamk.statsmk.databinding.FragmentEditWarShocksBinding
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.extension.clicks
import fr.harmoniamk.statsmk.fragment.warTrackResult.ShockAdapter
import fr.harmoniamk.statsmk.fragment.warTrackResult.WarTrackResultAdapter
import fr.harmoniamk.statsmk.model.firebase.NewWar
import fr.harmoniamk.statsmk.model.firebase.NewWarTrack
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class EditWarShocksFragment(val war: NewWar, val warTrack: NewWarTrack?): BottomSheetDialogFragment() {

    lateinit var binding: FragmentEditWarShocksBinding
    private val viewModel: EditWarShocksViewModel by viewModels()
    val onDismiss = MutableSharedFlow<NewWarTrack>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEditWarShocksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = WarTrackResultAdapter(positionVisible = false)
        val shockAdapter = ShockAdapter()
        binding.resultRv.adapter = adapter
        binding.shockRv.adapter = shockAdapter
        viewModel.bind(
            war = war,
            track = warTrack,
            onValid = binding.validateBtn.clicks(),
            onShockAdded = adapter.onShockAdded,
            onShockRemoved = adapter.onShockRemoved
        )
        viewModel.sharedShocks
            .onEach { shockAdapter.addItems(it) }
            .launchIn(lifecycleScope)
        viewModel.sharedPlayers
            .filterNotNull()
            .onEach { adapter.addResults(it) }
            .launchIn(lifecycleScope)
        viewModel.sharedDismiss.bind(onDismiss, lifecycleScope)
    }
}