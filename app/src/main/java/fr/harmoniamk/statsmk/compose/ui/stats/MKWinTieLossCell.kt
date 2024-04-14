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
import androidx.compose.ui.tooling.preview.Preview
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
fun MKWinTieLossCell(stats: MKStats?) {
    val warStats = (stats as? Stats)?.warStats
    val mapStats = stats as? MapStats
    val win = warStats?.warsWon ?: mapStats?.trackWon
    val tie = warStats?.warsTied ?: mapStats?.trackTie
    val loss = warStats?.warsLoss ?: mapStats?.trackLoss

    Column(
        modifier = Modifier
            .border(1.dp, colorResource(id = R.color.black), RoundedCornerShape(5.dp))
            .background(
                color = colorResource(id = R.color.transparent_white),
                shape = RoundedCornerShape(5.dp)
            )
    ) {
        Row(Modifier.padding(5.dp)) {
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                MKText(text = "V", font = R.font.montserrat_bold, modifier = Modifier.padding(vertical = 5.dp))
                MKText(text = win.toString(), font = R.font.orbitron_semibold, modifier = Modifier.padding(vertical = 5.dp), fontSize = 20)
            }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                MKText(text = "N", font = R.font.montserrat_bold, modifier = Modifier.padding(vertical = 5.dp))
                MKText(text = tie.toString(), font = R.font.orbitron_semibold, modifier = Modifier.padding(vertical = 5.dp), fontSize = 20)
            }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                MKText(text = "D", font = R.font.montserrat_bold, modifier = Modifier.padding(vertical = 5.dp))
                MKText(text = loss.toString(), font = R.font.orbitron_semibold, modifier = Modifier.padding(vertical = 5.dp), fontSize = 20)
            }
        }
    }
}

@Composable
@Preview
fun MKWinTieLossCellPreview() {
    MKWinTieLossCell(stats = null)
}