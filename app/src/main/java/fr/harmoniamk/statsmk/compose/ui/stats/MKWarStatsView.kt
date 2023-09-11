package fr.harmoniamk.statsmk.compose.ui.stats

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.compose.ui.MKText
import fr.harmoniamk.statsmk.model.local.MKStats
import fr.harmoniamk.statsmk.model.local.MapStats
import fr.harmoniamk.statsmk.model.local.Stats
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@Composable
fun MKWarStatsView(mkStats: MKStats) {
    val stats = mkStats as? Stats
    val mapStats = mkStats as? MapStats

    val totalPlayed = stats?.warStats?.warsPlayed ?: mapStats?.trackPlayed
    val totalPlayedLabel = when (mkStats) {
        is Stats -> R.string.wars_jou_es
        else -> R.string.maps_jou_es
    }
    Column {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 20.dp)) {
            Column(
                Modifier
                    .weight(1f)
                    .padding(horizontal = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                MKText(text = totalPlayed.toString(), font = R.font.orbitron_semibold, fontSize = 20)
                MKText(text = totalPlayedLabel, font = R.font.orbitron_regular, fontSize = 16)
                Spacer(modifier = Modifier.height(15.dp))
                MKWinTieLossCell(stats = mkStats)
            }
            MKPieChart(Modifier.weight(1f), stats = mkStats)
        }
    }



}