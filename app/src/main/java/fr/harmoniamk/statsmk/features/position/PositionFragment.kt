package fr.harmoniamk.statsmk.features.position

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.databinding.FragmentPositionBinding
import fr.harmoniamk.statsmk.enums.Maps
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.extension.clicks
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@FlowPreview
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class PositionFragment : Fragment(R.layout.fragment_position) {

    private val binding: FragmentPositionBinding by viewBinding()
    private val viewModel: PositionViewModel by viewModels()
    private var track: Int? = null
    private var tmId: Int? = null
    private var warId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tmId = arguments?.getInt("tmId")
        track = arguments?.getInt("track")
        warId = arguments?.getString("warId")
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        track?.let {
            val map = Maps.values()[it]
            binding.trackIv.clipToOutline = true
            binding.trackIv.setImageResource(map.picture)
            binding.cupIv.setImageResource(map.cup.picture)
            binding.shortname.text = map.name
            binding.name.setText(map.label)
            viewModel.bind(
                tournamentId = tmId,
                warId = warId,
                chosenTrack = it,
                onPos1 = binding.pos1.clicks(),
                onPos2 = binding.pos2.clicks(),
                onPos3 = binding.pos3.clicks(),
                onPos4 = binding.pos4.clicks(),
                onPos5 = binding.pos5.clicks(),
                onPos6 = binding.pos6.clicks(),
                onPos7 = binding.pos7.clicks(),
                onPos8 = binding.pos8.clicks(),
                onPos9 = binding.pos9.clicks(),
                onPos10 = binding.pos10.clicks(),
                onPos11 = binding.pos11.clicks(),
                onPos12 = binding.pos12.clicks()
            )
        }

        viewModel.validateTrack
            .filter { findNavController().currentDestination?.id == R.id.positionFragment }
            .onEach { when {
                tmId != null -> findNavController().popBackStack()
                warId != null -> findNavController().navigate(PositionFragmentDirections.backToCurrentWar())
            } }
            .launchIn(lifecycleScope)
    }
}