package fr.harmoniamk.statsmk.compose.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.compose.ui.MKScoreView
import fr.harmoniamk.statsmk.compose.ui.MKText
import fr.harmoniamk.statsmk.compose.ui.MKWarItem
import fr.harmoniamk.statsmk.model.local.MKStats
import fr.harmoniamk.statsmk.model.local.MapStats
import fr.harmoniamk.statsmk.model.local.Stats
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@Composable
fun MKTeamScoreStatView(
    stats: MKStats,
    onHighestClick: (String?) -> Unit,
    onLoudestClick: (String?) -> Unit)
{
    val warVictory = (stats as? Stats)?.warStats?.highestVictory
    val warDefeat = (stats as? Stats)?.warStats?.loudestDefeat
    val trackVictory = (stats as? MapStats)?.highestVictory
    val trackDefeat = (stats as? MapStats)?.loudestDefeat

    Row(
        Modifier
            .padding(bottom = 20.dp)
            .border(1.dp, colorResource(id = R.color.harmonia_dark), RoundedCornerShape(5.dp))
            .background(
                color = colorResource(id = R.color.transparent_white),
                shape = RoundedCornerShape(5.dp)
            )) {
        warVictory?.let { war ->
            Column(
                Modifier
                    .weight(1f)
                    .padding(top = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                MKText(text = R.string.plus_large_victoire, fontSize = 12)
                MKWarItem(war = war, onClick = onHighestClick, isForStats = true)
            }
        }
        trackVictory?.let {
            Column(
                Modifier
                    .weight(1f)
                    .padding(top = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                MKText(text = R.string.plus_large_victoire, fontSize = 12)
                MKScoreView(track = it.warTrack, modifier = Modifier.padding(bottom = 10.dp))
            }
        }
        warDefeat?.let {
            Column(
                Modifier
                    .weight(1f)
                    .padding(top = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                MKText(text = R.string.plus_lourde_d_faite, fontSize = 12)
                MKWarItem(war = it, onClick = onLoudestClick, isForStats = true)
            }
        }
        trackDefeat?.let {
            Column(
                Modifier
                    .weight(1f)
                    .padding(top = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                MKText(text = R.string.plus_lourde_d_faite, fontSize = 12)
                MKScoreView(track = it.warTrack, modifier = Modifier.padding(bottom = 10.dp))
            }
        }
    }
}