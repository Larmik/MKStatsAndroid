package fr.harmoniamk.statsmk.compose.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.model.firebase.NewWar
import fr.harmoniamk.statsmk.model.local.MKWar
import fr.harmoniamk.statsmk.model.mock.mock

@Composable
fun MKWarItem(war: MKWar, isForStats: Boolean = false) {
    val pin =  when (war.displayedDiff.first()) {
        '+' -> R.drawable.checked
        '0' -> R.drawable.circle_grey
        else -> R.drawable.close
    }
    Card(backgroundColor = colorResource(id = R.color.white_alphaed), shape = RoundedCornerShape(5.dp)) {
        when (isForStats) {
            true -> Column(Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(horizontalArrangement = Arrangement.Center) {
                    Image(painter = painterResource(id = pin), contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.padding(horizontal = 10.dp))
                    war.name?.let { MKText(text = it, fontSize = 18, font = R.font.orbitron_semibold) }
                }
                war.war?.createdDate?.let { MKText(text = it, modifier = Modifier.padding(bottom = 10.dp)) }
                MKScoreView(war = war)
            }
            else -> Row(modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Image(painter = painterResource(id = pin), contentDescription = null, modifier = Modifier.size(20.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    war.name?.let { MKText(text = it, fontSize = 18, font = R.font.orbitron_semibold) }
                    war.war?.createdDate?.let { MKText(text = it) }
                }
                Row() {
                    Column() {
                        MKText(text = stringResource(id = R.string.score))
                        MKText(text = stringResource(id = R.string.diff))
                        MKText(text = stringResource(id = R.string.maps))
                    }
                    Spacer(modifier = Modifier.width(5.dp))
                    Column() {
                        MKText(text = war.displayedScore, font = R.font.orbitron_regular)
                        MKText(text = war.displayedDiff, font = R.font.orbitron_regular)
                        MKText(text = war.mapsWon, font = R.font.orbitron_regular)
                    }
                }

            }
        }

    }
}

@Composable
@Preview
fun MKWarItemPreview() {
    MKWarItem(war = MKWar(NewWar.mock()).apply { this.name = "HR - Ev" })
}

@Composable
@Preview
fun MKWarItemStatsPreview() {
    MKWarItem(war = MKWar(NewWar.mock()).apply { this.name = "HR - Ev" }, isForStats = true)
}