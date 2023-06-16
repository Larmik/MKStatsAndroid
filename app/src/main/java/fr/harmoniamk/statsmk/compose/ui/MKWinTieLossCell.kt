package fr.harmoniamk.statsmk.compose.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.harmoniamk.statsmk.R

@Composable
fun MKWinTieLossCell() {
    Card() {
        Row(Modifier.fillMaxWidth()) {
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                MKText(text = "V", font = R.font.montserrat_bold, modifier = Modifier.padding(vertical = 5.dp))
                MKText(text = "13", font = R.font.orbitron_semibold, modifier = Modifier.padding(vertical = 5.dp), fontSize = 20)
            }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                MKText(text = "N", font = R.font.montserrat_bold, modifier = Modifier.padding(vertical = 5.dp))
                MKText(text = "1", font = R.font.orbitron_semibold, modifier = Modifier.padding(vertical = 5.dp), fontSize = 20)
            }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                MKText(text = "D", font = R.font.montserrat_bold, modifier = Modifier.padding(vertical = 5.dp))
                MKText(text = "9", font = R.font.orbitron_semibold, modifier = Modifier.padding(vertical = 5.dp), fontSize = 20)
            }
        }
    }
}

@Composable
@Preview
fun MKWinTieLossCellPreview() {
    MKWinTieLossCell()
}