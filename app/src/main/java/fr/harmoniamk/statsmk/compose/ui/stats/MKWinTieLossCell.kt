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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.compose.ui.MKText
import fr.harmoniamk.statsmk.compose.viewModel.ColorsViewModel
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
    
    val colorsViewModel: ColorsViewModel = hiltViewModel()

    Column(
        modifier = Modifier
            .border(1.dp, colorsViewModel.mainTextColor, RoundedCornerShape(5.dp))
            .background(
                color = colorsViewModel.secondaryColorAlphaed,
                shape = RoundedCornerShape(5.dp)
            )
    ) {
        Row(Modifier.padding(5.dp)) {
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                MKText(text = stringResource(R.string.v), font = R.font.montserrat_bold, modifier = Modifier.padding(vertical = 5.dp))
                MKText(text = win.toString(), font = R.font.orbitron_semibold, modifier = Modifier.padding(vertical = 5.dp), fontSize = 20)
            }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                MKText(text = stringResource(R.string.n), font = R.font.montserrat_bold, modifier = Modifier.padding(vertical = 5.dp))
                MKText(text = tie.toString(), font = R.font.orbitron_semibold, modifier = Modifier.padding(vertical = 5.dp), fontSize = 20)
            }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                MKText(text = stringResource(R.string.d), font = R.font.montserrat_bold, modifier = Modifier.padding(vertical = 5.dp))
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