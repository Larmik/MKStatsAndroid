package fr.harmoniamk.statsmk.fragment.currentWar

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.databinding.FragmentCurrentWarBinding
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class CurrentWarFragment : Fragment(R.layout.fragment_current_war) {

    private val binding : FragmentCurrentWarBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = CurrentWarTrackAdapter()
        val penaltiesAdapter = PenaltyAdapter()
        val firstsPlayersAdapter = CurrentPlayerAdapter(isCurrent = true)
        val lastsPlayersAdapter = CurrentPlayerAdapter(isCurrent = true)
        binding.currentTracksRv.adapter = adapter
        binding.penaltiesRv.adapter = penaltiesAdapter
        binding.firstsPlayersRv.adapter = firstsPlayersAdapter
        binding.lastsPlayersRv.adapter = lastsPlayersAdapter

    }

}