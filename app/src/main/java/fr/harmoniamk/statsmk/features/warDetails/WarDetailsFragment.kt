package fr.harmoniamk.statsmk.features.warDetails

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.database.model.War
import fr.harmoniamk.statsmk.databinding.FragmentWarDetailsBinding
import fr.harmoniamk.statsmk.enums.Maps
import fr.harmoniamk.statsmk.extension.clicks
import fr.harmoniamk.statsmk.features.currentWar.CurrentWarTrackAdapter
import fr.harmoniamk.statsmk.features.trackList.TrackListAdapter
import fr.harmoniamk.statsmk.model.MKWar
import fr.harmoniamk.statsmk.model.MKWarTrack
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class WarDetailsFragment : Fragment(R.layout.fragment_war_details) {

    private val binding : FragmentWarDetailsBinding by viewBinding()
    private val viewModel: WarDetailsViewModel by viewModels()
    private var war: MKWar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        war = arguments?.get("war") as? MKWar
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = CurrentWarTrackAdapter()
        binding.currentTracksRv.adapter = adapter
        war?.let { war ->
            binding.warTitleTv.text = war.war?.name
            binding.warDateTv.text = war.war?.createdDate
            binding.scoreTv.text = war.displayedScore
            binding.diffScoreTv.text = war.displayedDiff
            viewModel.bind(war.war?.mid)
            viewModel.sharedBestTrack.onEach { binding.bestTrack.bind(it) }.launchIn(lifecycleScope)
            viewModel.sharedWorstTrack.onEach { binding.worstTrack.bind(it) }.launchIn(lifecycleScope)
            viewModel.sharedWarPlayers.onEach { bindPlayers(it) }.launchIn(lifecycleScope)
            viewModel.sharedTracks.onEach {
                adapter.addTracks(it)
            }.launchIn(lifecycleScope)

        }
    }

    private fun bindPlayers(players : List<Pair<String?, Int>>) {
        players.forEachIndexed { index, pair ->
            when (index) {
                0 -> {
                    binding.player1.text = pair.first
                    binding.player1score.text = pair.second.toString()
                }
                1 -> {
                    binding.player2.text = pair.first
                    binding.player2score.text = pair.second.toString()
                }
                2 -> {
                    binding.player3.text = pair.first
                    binding.player3score.text = pair.second.toString()
                }
                3 -> {
                    binding.player4.text = pair.first
                    binding.player4score.text = pair.second.toString()
                }
                4 -> {
                    binding.player5.text = pair.first
                    binding.player5score.text = pair.second.toString()
                }
                5 -> {
                    binding.player6.text = pair.first
                    binding.player6score.text = pair.second.toString()
                }
            }
        }
    }
}