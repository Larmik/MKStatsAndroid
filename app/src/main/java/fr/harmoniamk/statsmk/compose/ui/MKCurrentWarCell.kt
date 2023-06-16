package fr.harmoniamk.statsmk.compose.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
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
fun MKCurrentWarCell(war: MKWar) {

    val teamName = war.name?.split("-")?.getOrNull(0)?.trim().toString()
    val opponentName = war.name?.split("-")?.getOrNull(1)?.trim().toString()
    val teamScore = war.displayedScore.split("-").getOrNull(0)?.trim().toString()
    val opponentScore = war.displayedScore.split("-").getOrNull(1)?.trim().toString()
    val diff = war.displayedDiff
    val remaining = 12 - (war.warTracks?.size ?: 0)
    val diffColor = when {
        war.displayedDiff.contains("-") -> R.color.lose
        war.displayedDiff.contains("+") -> R.color.green
        else -> R.color.white
    }

    Card(
        shape = RoundedCornerShape(5.dp),
        backgroundColor = colorResource(id = R.color.harmonia_dark),
        contentColor = colorResource(
            id = R.color.white
        )
    ) {
        Column(
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
                        fontSize = 20,
                        textColor = R.color.white
                    )
                    MKText(
                        text = teamScore,
                        font = R.font.orbitron_semibold,
                        modifier = Modifier.padding(vertical = 5.dp),
                        fontSize = 32,
                        textColor = R.color.white
                    )
                }
                MKText(
                    text = "-",
                    modifier = Modifier.padding(horizontal = 30.dp),
                    fontSize = 32,
                    textColor = R.color.white
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    MKText(
                        text = opponentName,
                        font = R.font.montserrat_bold,
                        modifier = Modifier.padding(vertical = 5.dp),
                        fontSize = 20,
                        textColor = R.color.white
                    )
                    MKText(
                        text = opponentScore,
                        font = R.font.orbitron_semibold,
                        modifier = Modifier.padding(vertical = 5.dp),
                        fontSize = 32,
                        textColor = R.color.white
                    )
                }
            }
            MKText(
                text = diff,
                font = R.font.orbitron_semibold,
                modifier = Modifier.padding(vertical = 5.dp),
                fontSize = 24,
                textColor = diffColor
            )
            MKText(
                text = "Maps restantes: $remaining",
                font = R.font.montserrat_regular,
                modifier = Modifier.padding(vertical = 5.dp),
                fontSize = 14,
                textColor = R.color.white
            )
        }
    }
}

@Composable
@Preview
fun MKCurrentWarCellPreview() {
    MKCurrentWarCell(MKWar(NewWar.mock()).apply { this.name = "HR - Ev" })
}