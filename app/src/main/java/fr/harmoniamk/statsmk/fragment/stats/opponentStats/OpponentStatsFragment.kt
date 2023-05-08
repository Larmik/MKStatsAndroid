package fr.harmoniamk.statsmk.fragment.stats.opponentStats

import android.os.Build
import android.os.Bundle
import android.util.TypedValue.COMPLEX_UNIT_SP
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.databinding.FragmentOpponentStatsBinding
import fr.harmoniamk.statsmk.extension.clicks
import fr.harmoniamk.statsmk.extension.isTrue
import fr.harmoniamk.statsmk.extension.positionColor
import fr.harmoniamk.statsmk.fragment.stats.opponentRanking.OpponentRankingItemViewModel
import fr.harmoniamk.statsmk.model.local.MKWar
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach


@FlowPreview
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class OpponentStatsFragment : Fragment(R.layout.fragment_opponent_stats) {

    private val binding: FragmentOpponentStatsBinding by viewBinding()
    private val viewModel: OpponentStatsViewModel by viewModels()
    private var stats: OpponentRankingItemViewModel? = null
    private var userId: String? = null
    private var isIndiv: Boolean? = null
    private val wars = mutableListOf<MKWar>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        stats = arguments?.get("stats") as? OpponentRankingItemViewModel
        userId = arguments?.getString("userId")
        isIndiv = arguments?.getBoolean("isIndiv")
        (arguments?.get("wars") as? Array<out MKWar>)?.let {
            wars.addAll(it)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.bind(stats = stats, userId = userId, isIndiv = isIndiv.isTrue,
            onDetailsClick = binding.showDetailsBtn.clicks(),
            onBestClick = binding.bestTrackview.clicks(),
            onWorstClick = binding.worstTrackview.clicks(),
            onMostPlayedClick = binding.mostPlayedTrackview.clicks(),
            onVictoryClick = binding.highestVictory.clicks(),
            onDefeatClick = binding.loudestDefeat.clicks(),
            onHighestScore = binding.highestScoreLayout.clicks(),
            onLowestScore = binding.lowestScoreLayout.clicks())
        viewModel.sharedLowestScore
            .filterNotNull()
            .onEach {
                binding.lowestScore.text = it.first.toString()
                binding.lowestScoreWarDate.text = it.second
            }.launchIn(lifecycleScope)
        viewModel.sharedHighestScore
            .filterNotNull()
            .onEach {
                binding.highestScore.text = it.first.toString()
                binding.highestScoreWarDate.text = it.second
            }.launchIn(lifecycleScope)
        viewModel.sharedDetailsClick
            .filter { findNavController().currentDestination?.id == R.id.opponentStatsFragment }
            .onEach { findNavController().navigate(OpponentStatsFragmentDirections.toOpponentWarDetails(stats?.stats, userId != null, stats?.teamName)) }
            .launchIn(lifecycleScope)

        stats?.let {
            val bestMap = when (userId != null && isIndiv.isTrue) {
                true -> it.stats.bestPlayerMap
                else -> it.stats.bestMap
            }
            val worstMap = when (userId != null && isIndiv.isTrue) {
                true -> it.stats.worstPlayerMap
                else -> it.stats.worstMap
            }
            binding.playerName.text = it.teamName
            binding.bestTrackview.bind(bestMap, shouldDisplayPosition = isIndiv.isTrue && userId != null)
            binding.worstTrackview.bind(worstMap, shouldDisplayPosition = isIndiv.isTrue && userId != null)
            binding.mostPlayedTrackview.bind(it.stats.mostPlayedMap, shouldDisplayPosition = isIndiv.isTrue && userId != null)
            it.stats.warStats.highestVictory?.let {
                binding.noVictory.isVisible = false
                binding.victoryLayout.isVisible = true
                binding.highestVictory.text = it.displayedScore
                binding.highestVictoryWarDate.text = it.war?.createdDate
            }
            it.stats.warStats.loudestDefeat?.let {
                binding.noDefeat.isVisible = false
                binding.defeatLayout.isVisible = true
                binding.loudestDefeat.text = it.displayedScore
                binding.loudestDefeatWarDate.text = it.war?.createdDate
            }


            binding.warPlayed.text = it.stats.warStats.warsPlayed.toString()
            binding.winText.text = it.stats.warStats.warsWon.toString()
            binding.tieText.text = it.stats.warStats.warsTied.toString()
            binding.loseText.text = it.stats.warStats.warsLoss.toString()
            binding.totalAverage.text = it.averageLabel
            binding.mapAverage.text = it.averageMapLabel
            binding.mapsWon.text = it.stats.mapsWon
            binding.shockCount.text = it.stats.shockCount.toString()
            binding.piechart.bind(
                it.stats.warStats.warsWon,
                it.stats.warStats.warsTied,
                it.stats.warStats.warsLoss
            )
            val averageWarColor = when  {
                userId != null && isIndiv.isTrue -> R.color.harmonia_dark
                it.stats.averagePointsLabel.contains("-") -> R.color.lose
                it.stats.averagePointsLabel.contains("+") -> R.color.green
                else -> R.color.harmonia_dark
            }
            val averageMapColor = when  {
                userId != null && isIndiv.isTrue -> it.averageMapLabel.toIntOrNull().positionColor()
                it.stats.averageMapPointsLabel.contains("-") -> R.color.lose
                it.stats.averageMapPointsLabel.contains("+") -> R.color.green
                else -> R.color.harmonia_dark
            }

            binding.totalAverage.setTextColor(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    requireContext().getColor(averageWarColor)
                else ContextCompat.getColor(requireContext(), averageWarColor)
            )
            binding.mapAverage.setTextColor(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    requireContext().getColor(averageMapColor)
                else ContextCompat.getColor(requireContext(), averageMapColor)
            )

            if (userId != null && isIndiv.isTrue) {
                val typeface = ResourcesCompat.getFont(requireContext(), R.font.mk_position)
                binding.playerScoresLayout.isVisible = true
                binding.mapAverage.typeface = typeface
                binding.mapAverage.setTextSize(COMPLEX_UNIT_SP, 26f)
                binding.mapAverageLabel.text = requireContext().getString(R.string.position_moyenne)
            }

            viewModel.sharedTrackClick
                .filter { findNavController().currentDestination?.id == R.id.opponentStatsFragment }
                .onEach { findNavController().navigate(OpponentStatsFragmentDirections.toMapStats(trackId = it.second, userId = it.first, teamId = stats?.team?.mid, isIndiv = isIndiv.isTrue, wars = stats?.stats?.warStats?.list?.toTypedArray() ?: arrayOf())) }
                .launchIn(lifecycleScope)
            viewModel.sharedWarClick
                .filter { findNavController().currentDestination?.id == R.id.opponentStatsFragment }
                .onEach { findNavController().navigate(OpponentStatsFragmentDirections.goToWarDetails(it)) }
                .launchIn(lifecycleScope)

        }
    }

}