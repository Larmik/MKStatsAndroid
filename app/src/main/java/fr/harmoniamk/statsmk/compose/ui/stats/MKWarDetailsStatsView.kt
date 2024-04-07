package fr.harmoniamk.statsmk.compose.ui.stats

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.compose.ui.MKText
import fr.harmoniamk.statsmk.compose.viewModel.StatsType
import fr.harmoniamk.statsmk.extension.isTrue
import fr.harmoniamk.statsmk.extension.pointsToPosition
import fr.harmoniamk.statsmk.extension.positionColor
import fr.harmoniamk.statsmk.extension.trackScoreToDiff
import fr.harmoniamk.statsmk.model.local.MKStats
import fr.harmoniamk.statsmk.model.local.MapStats
import fr.harmoniamk.statsmk.model.local.Stats
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@Composable
fun MKWarDetailsStatsView(mkStats: MKStats, type: StatsType) {
    val stats = mkStats as? Stats
    val mapStats = mkStats as? MapStats

    val diffColor = when {
        type is StatsType.IndivStats || (type as? StatsType.OpponentStats)?.userId != null -> R.color.black
        stats?.averagePointsLabel?.contains("+").isTrue || mapStats?.teamScore?.trackScoreToDiff()?.contains("+").isTrue -> R.color.win
        stats?.averagePointsLabel?.contains("-").isTrue || mapStats?.teamScore?.trackScoreToDiff()?.contains("-").isTrue -> R.color.lose
        else -> R.color.black
    }

    Column(
        modifier = Modifier
            .padding(bottom = 20.dp)
            .border(1.dp, colorResource(id = R.color.harmonia_dark), RoundedCornerShape(5.dp))
            .background(
                color = colorResource(id = R.color.transparent_white),
                shape = RoundedCornerShape(5.dp)
            )
    ) {
        type.takeIf { it !is StatsType.MapStats }?.let {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 10.dp)
            ) {
                Column(
                    Modifier
                        .weight(1f), horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    MKText(
                        text = when (type) {
                            is StatsType.MapStats -> R.string.diff_rence_quipe
                            is StatsType.IndivStats -> R.string.score_moyen
                            else -> R.string.moyenne_war
                        }, fontSize = 12
                    )
                    MKText(
                        text = when  {
                            type is StatsType.MapStats -> mapStats?.teamScore?.trackScoreToDiff().toString()
                            type is StatsType.IndivStats -> stats?.averagePoints.toString()
                            (type as? StatsType.OpponentStats)?.userId != null -> stats?.averagePoints.toString()
                            else -> stats?.averagePointsLabel.toString()
                        }, font = R.font.orbitron_semibold, fontSize = 20, textColor = diffColor)
                }
                type.takeIf { it is StatsType.OpponentStats }?.let {
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        MKText(text = R.string.maps_gagn_es, fontSize = 12)
                        MKText(text = stats?.mapsWon.toString(), fontSize = 16, font = R.font.montserrat_bold)
                    }
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 10.dp)
        ) {
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                MKText(
                    text = when {
                        type is StatsType.IndivStats || (type as? StatsType.OpponentStats)?.userId != null || (type as? StatsType.MapStats)?.userId != null -> R.string.position_moyenne
                        else -> R.string.moyenne_map
                    }, fontSize = 12
                )
                MKText(
                    text = when {
                        type is StatsType.IndivStats ||  (type as? StatsType.MapStats)?.userId != null || (type as? StatsType.OpponentStats)?.userId != null -> (stats?.averagePlayerPosition ?: mapStats?.playerPosition).toString()
                        else -> (stats?.averageMapPointsLabel ?: mapStats?.teamScore?.trackScoreToDiff()).toString()
                    }, font = when {
                        type is StatsType.IndivStats || (type as? StatsType.MapStats)?.userId != null || (type as? StatsType.OpponentStats)?.userId != null -> R.font.mk_position
                        else -> R.font.orbitron_semibold
                    }, fontSize = when {
                        type is StatsType.IndivStats || (type as? StatsType.MapStats)?.userId != null || (type as? StatsType.OpponentStats)?.userId != null -> 26
                        else -> 20
                    }, textColor = when {
                        type is StatsType.IndivStats || (type as? StatsType.MapStats)?.userId != null  || (type as? StatsType.OpponentStats)?.userId != null  -> (stats?.averagePlayerPosition ?: mapStats?.playerPosition).positionColor()
                        else -> diffColor
                    }
                )
            }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                val shockLabel = when (stats) {
                    null ->  R.string.shocks_rapport_s
                    else -> R.string.shocks_war
                }
                val shockCount = when (stats) {
                    null ->  mapStats?.shockCount.toString()
                    else -> String.format("%.2f", (stats.shockCount.toFloat() / stats.warStats.warsPlayedSinceShocks))
                }
                MKText(text = shockLabel, fontSize = 12)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.shock),
                        contentDescription = null,
                        modifier = Modifier.size(30.dp)
                    )
                    MKText(
                        text = shockCount,
                        fontSize = 16,
                        font = R.font.orbitron_semibold
                    )
                }
            }
        }
    }
}