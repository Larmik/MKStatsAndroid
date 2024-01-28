package fr.harmoniamk.statsmk.compose.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.model.firebase.NewWar
import fr.harmoniamk.statsmk.model.local.MKWar
import fr.harmoniamk.statsmk.model.mock.mock

@Composable
fun MKCurrentWarCell(war: MKWar, loadingState: String? = null, onClick: () -> Unit) {
    val teamName = war.name?.split("-")?.getOrNull(0)?.trim().toString()
    val opponentName = war.name?.split("-")?.getOrNull(1)?.trim().toString()
    val teamScore = war.displayedScore.split("-").getOrNull(0)?.trim().toString()
    val opponentScore = war.displayedScore.split("-").getOrNull(1)?.trim().toString()
    val diff = war.displayedDiff
    val remaining = 12 - (war.warTracks?.size ?: 0)
    val diffColor = when {
        war.displayedDiff.contains("-") -> R.color.lose
        war.displayedDiff.contains("+") -> R.color.win
        else -> R.color.white
    }
    Card(
        shape = RoundedCornerShape(5.dp),
        backgroundColor = colorResource(id = R.color.harmonia_dark),
        contentColor = colorResource(id = R.color.white),
        elevation = 0.dp,
        modifier = Modifier.padding(10.dp).clickable { onClick() }
    ) {
        when (loadingState) {
            null ->    Column(
                horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
                    .padding(10.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        MKText(
                            text = teamName,
                            font = R.font.montserrat_bold,
                            modifier = Modifier.padding(vertical = 5.dp),
                            fontSize = 16,
                            textColor = R.color.white
                        )
                        MKText(
                            text = teamScore,
                            font = R.font.orbitron_semibold,
                            fontSize = 22,
                            textColor = R.color.white
                        )
                    }
                    MKText(
                        text = diff,
                        font = R.font.orbitron_semibold,
                        fontSize = 18,
                        textColor = diffColor
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        MKText(
                            text = opponentName,
                            font = R.font.montserrat_bold,
                            modifier = Modifier.padding(vertical = 5.dp),
                            fontSize = 16,
                            textColor = R.color.white
                        )
                        MKText(
                            text = opponentScore,
                            font = R.font.orbitron_semibold,
                            fontSize = 22,
                            textColor = R.color.white
                        )
                    }
                }
                MKText(
                    text = "Maps restantes: $remaining",
                    font = R.font.montserrat_regular,
                    fontSize = 12,
                    textColor = R.color.white
                )
            }
            else ->  Column(
                horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
                    .padding(10.dp)
                    .fillMaxWidth()
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(vertical = 10.dp).size(40.dp), color = colorResource(
                        id = R.color.white
                    )
                )
                MKText(text = "Récupération de la war en cours ...", textColor = R.color.white)
            }
        }
    }
}

@Composable
@Preview
fun MKCurrentWarCellPreview() {
    MKCurrentWarCell(MKWar(NewWar.mock()).apply { this.name = "HR - Ev" }) {

    }
}