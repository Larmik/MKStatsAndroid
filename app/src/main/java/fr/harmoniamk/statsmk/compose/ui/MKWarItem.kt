package fr.harmoniamk.statsmk.compose.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
fun MKWarItem(war: MKWar, isForStats: Boolean = false, onClick: (String?) -> Unit) {
    val pin =  when (war.displayedDiff.first()) {
        '+' -> R.drawable.checked
        '0' -> R.drawable.circle_grey
        else -> R.drawable.close
    }
    Card(backgroundColor = colorResource(id = R.color.white_alphaed), modifier = Modifier.padding(bottom = 5.dp).clickable { onClick(war.war?.mid) }, elevation = 0.dp) {
        when (isForStats) {
            true -> Column(Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(horizontalArrangement = Arrangement.Center) {
                    Image(painter = painterResource(id = pin), contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.padding(horizontal = 10.dp))
                    war.name?.let { MKText(text = it, fontSize = 18, font = R.font.montserrat_bold) }
                }
                war.war?.createdDate?.let { MKText(text = it, modifier = Modifier.padding(bottom = 10.dp)) }
                MKScoreView(war = war)
            }
            else -> Row(modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Image(painter = painterResource(id = pin), contentDescription = null, modifier = Modifier.size(15.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    war.name?.let { MKText(text = it, fontSize = 16, font = R.font.montserrat_bold) }
                    war.war?.createdDate?.let { MKText(text = it) }
                }
                Row {
                    Column {
                        MKText(text = stringResource(id = R.string.score), fontSize = 12)
                        MKText(text = stringResource(id = R.string.diff), fontSize = 12)
                        MKText(text = stringResource(id = R.string.maps), fontSize = 12)
                    }
                    Spacer(modifier = Modifier.width(5.dp))
                    Column {
                        MKText(text = war.displayedScore, font = R.font.montserrat_bold, fontSize = 12)
                        MKText(text = war.displayedDiff, font = R.font.montserrat_bold, fontSize = 12)
                        MKText(text = war.mapsWon, font = R.font.montserrat_bold, fontSize = 12)
                    }
                }
            }
        }
    }
}

@Composable
@Preview
fun MKWarItemPreview() {
    MKWarItem(war = MKWar(NewWar.mock()).apply { this.name = "HR - Ev" }) {

    }
}

@Composable
@Preview
fun MKWarItemStatsPreview() {
    MKWarItem(war = MKWar(NewWar.mock()).apply { this.name = "HR - Ev" }, isForStats = true) {

    }
}