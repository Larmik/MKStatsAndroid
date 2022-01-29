package fr.harmoniamk.statsmk.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.databinding.FragmentEditCurrentTrackBinding
import fr.harmoniamk.statsmk.enums.Maps
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.extension.clicks
import fr.harmoniamk.statsmk.viewModel.EditCurrentTrackViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@FlowPreview
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class EditCurrentTrackFragment(val trackId: Int, val onMapEdit: MutableSharedFlow<Unit>, val onPositionEdit: MutableSharedFlow<Unit>) : Fragment(R.layout.fragment_edit_current_track) {

    private val binding: FragmentEditCurrentTrackBinding by viewBinding()
    private val viewModel: EditCurrentTrackViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.bind(
            trackId = trackId,
            onTrackClick = binding.trackCard.clicks(),
            onPositionClick = binding.posTv.clicks()
        )
        viewModel.sharedTrackClick.bind(onMapEdit, lifecycleScope)
        viewModel.sharedPositionClick.bind(onPositionEdit, lifecycleScope)
        viewModel.sharedTrack
            .filterNotNull()
            .onEach {
                bindMap(Maps.values()[it.trackIndex])
                binding.posTv.text = it.displayedPos
            }.launchIn(lifecycleScope)
    }

    private fun bindMap(track: Maps) {
        binding.trackIv.clipToOutline = true
        binding.trackIv.setImageResource(track.picture)
        binding.cupIv.setImageResource(track.cup.picture)
        binding.shortname.text = track.name
        binding.name.setText(track.label)
    }
}