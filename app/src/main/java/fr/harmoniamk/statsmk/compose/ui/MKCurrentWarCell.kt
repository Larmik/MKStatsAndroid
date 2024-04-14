package fr.harmoniamk.statsmk.compose.ui

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.compose.viewModel.ColorsViewModel
import fr.harmoniamk.statsmk.extension.fromHex
import fr.harmoniamk.statsmk.model.firebase.NewWar
import fr.harmoniamk.statsmk.model.local.MKWar
import fr.harmoniamk.statsmk.model.mock.mock

data class CurrentWar(val teamName: String, val war: MKWar)

@Composable
fun MKCurrentWarCell(current: CurrentWar, onClick: (String) -> Unit) {
    val colorsViewModel: ColorsViewModel = hiltViewModel()
    val war = current.war
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
        backgroundColor = colorsViewModel.secondaryColor,
        contentColor = colorResource(id = R.color.white),
        elevation = 0.dp,
        modifier = Modifier.padding(10.dp).clickable { onClick(war.war?.teamHost.orEmpty()) }
    ) {
       Column(
                horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
                    .padding(10.dp)
                    .fillMaxWidth()
            ) {
                MKText(
                    text = current.teamName,
                    font = R.font.montserrat_regular,
                    fontSize = 12,
                    textColor = R.color.white
                )
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
    }
}
