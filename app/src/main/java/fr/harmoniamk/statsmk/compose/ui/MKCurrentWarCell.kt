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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.compose.viewModel.ColorsViewModel
import fr.harmoniamk.statsmk.model.firebase.CurrentWar


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
        war.displayedDiff.contains("-") -> colorResource(R.color.lose)
        war.displayedDiff.contains("+") -> colorResource(R.color.win)
        else -> colorsViewModel.secondaryTextColor
    }
    Card(
        shape = RoundedCornerShape(5.dp),
        backgroundColor = colorsViewModel.secondaryColor,
        contentColor = colorsViewModel.secondaryTextColor,
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
                    newTextColor = colorsViewModel.secondaryTextColor
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
                            newTextColor = colorsViewModel.secondaryTextColor
                        )
                        MKText(
                            text = teamScore,
                            font = R.font.orbitron_semibold,
                            fontSize = 22,
                            newTextColor = colorsViewModel.secondaryTextColor
                        )
                    }
                    MKText(
                        text = diff,
                        font = R.font.orbitron_semibold,
                        fontSize = 18,
                        newTextColor = diffColor
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        MKText(
                            text = opponentName,
                            font = R.font.montserrat_bold,
                            modifier = Modifier.padding(vertical = 5.dp),
                            fontSize = 16,
                            newTextColor = colorsViewModel.secondaryTextColor
                        )
                        MKText(
                            text = opponentScore,
                            font = R.font.orbitron_semibold,
                            fontSize = 22,
                            newTextColor = colorsViewModel.secondaryTextColor
                        )
                    }
                }
                MKText(
                    text = "${stringResource(R.string.maps_restantes)} $remaining",
                    font = R.font.montserrat_regular,
                    fontSize = 12,
                    newTextColor = colorsViewModel.secondaryTextColor
                )
            }
    }
}
