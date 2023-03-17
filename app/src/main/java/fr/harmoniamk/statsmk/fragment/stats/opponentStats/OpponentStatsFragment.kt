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
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.databinding.FragmentOpponentStatsBinding
import fr.harmoniamk.statsmk.extension.isTrue
import fr.harmoniamk.statsmk.extension.positionColor
import fr.harmoniamk.statsmk.fragment.stats.opponentRanking.OpponentRankingItemViewModel
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach


@AndroidEntryPoint
class OpponentStatsFragment : Fragment(R.layout.fragment_opponent_stats) {

    private val binding: FragmentOpponentStatsBinding by viewBinding()
    private val viewModel: OpponentStatsViewModel by viewModels()
    private var stats: OpponentRankingItemViewModel? = null
    private var isIndiv: Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        stats = arguments?.get("stats") as? OpponentRankingItemViewModel
        isIndiv = arguments?.getBoolean("isIndiv")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.bind(stats = stats, isIndiv = isIndiv.isTrue)
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

        stats?.let {
            binding.playerName.text = it.teamName
            binding.bestTrackview.bind(it.stats.bestMap)
            binding.worstTrackview.bind(it.stats.worstMap)
            binding.mostPlayedTrackview.bind(it.stats.mostPlayedMap)
            binding.highestVictory.text = it.stats.warStats.highestVictory?.displayedScore
            binding.highestVictoryWarDate.text = it.stats.warStats.highestVictory?.war?.createdDate
            binding.loudestDefeat.text = it.stats.warStats.loudestDefeat?.displayedScore
            binding.loudestDefeatWarDate.text = it.stats.warStats.loudestDefeat?.war?.createdDate
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
                isIndiv.isTrue -> R.color.harmonia_dark
                it.stats.averagePointsLabel.contains("-") -> R.color.lose
                it.stats.averagePointsLabel.contains("+") -> R.color.green
                else -> R.color.harmonia_dark
            }
            val averageMapColor = when  {
                isIndiv.isTrue -> it.averageMapLabel.toIntOrNull().positionColor()
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

            if (isIndiv.isTrue) {
                val typeface = ResourcesCompat.getFont(requireContext(), R.font.mk_position)
                binding.playerScoresLayout.isVisible = true
                binding.mapAverage.typeface = typeface
                binding.mapAverage.setTextSize(COMPLEX_UNIT_SP, 26f)
                binding.mapAverageLabel.text = "Position moyenne"
            }

        }
    }

}