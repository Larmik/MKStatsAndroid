package fr.harmoniamk.statsmk.compose.ui.stats

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.compose.ui.MKText
import fr.harmoniamk.statsmk.compose.ui.MKTrackItem
import fr.harmoniamk.statsmk.compose.viewModel.StatsType
import fr.harmoniamk.statsmk.model.local.Stats

@Composable
fun MKMapsStatsView(
    stats: Stats,
    type: StatsType,
    periodic: String,
    onMostPlayedClick: (Int, String?, String?, String) -> Unit,
    onBestClick: (Int, String?, String?, String) -> Unit,
    onWorstClick: (Int, String?, String?, String) -> Unit
) {
    val isIndiv = type is StatsType.IndivStats || (type as? StatsType.OpponentStats)?.userId != null
    val bestMap = when (isIndiv) {
        true -> stats.bestPlayerMap
        else -> stats.bestMap
    }
    val worstMap = when (isIndiv) {
        true -> stats.worstPlayerMap
        else -> stats.worstMap
    }
    val userId =
        (type as? StatsType.IndivStats)?.userId ?: (type as? StatsType.OpponentStats)?.userId
    val teamId = (type as? StatsType.OpponentStats)?.teamId

    Column {
        MKText(text = "Circuits", font = R.font.montserrat_bold, fontSize = 16)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 10.dp)
        ) {
            MKText(
                text = R.string.circuit_le_plus_jou,
                fontSize = 12,
                modifier = Modifier.offset(y = 10.dp)
            )
            MKTrackItem(
                trackRanking = stats.mostPlayedMap,
                isVertical = true,
                isIndiv = type is StatsType.IndivStats || (type as? StatsType.OpponentStats)?.userId != null,
                onClick = { onMostPlayedClick(it, teamId, userId, periodic) })
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    MKText(
                        text = R.string.meilleur_circuit,
                        fontSize = 12,
                        modifier = Modifier.offset(y = 10.dp)
                    )
                    MKTrackItem(
                        trackRanking = bestMap,
                        isVertical = true,
                        isIndiv = type is StatsType.IndivStats || (type as? StatsType.OpponentStats)?.userId != null,
                        onClick = { onBestClick(it, teamId, userId, periodic) })
                }
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    MKText(
                        text = R.string.pire_circuit,
                        fontSize = 12,
                        modifier = Modifier.offset(y = 10.dp)
                    )
                    MKTrackItem(
                        trackRanking = worstMap,
                        isVertical = true,
                        isIndiv = type is StatsType.IndivStats || (type as? StatsType.OpponentStats)?.userId != null,
                        onClick = { onWorstClick(it, teamId, userId, periodic) })
                }
            }
        }
    }
}