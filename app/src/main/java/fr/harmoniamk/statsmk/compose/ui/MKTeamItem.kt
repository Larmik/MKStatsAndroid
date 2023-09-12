package fr.harmoniamk.statsmk.compose.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.database.entities.TeamEntity
import fr.harmoniamk.statsmk.fragment.stats.opponentRanking.OpponentRankingItemViewModel
import fr.harmoniamk.statsmk.model.firebase.Team

@Composable
fun MKTeamItem(team: Team? = null, teamToManage: Team? = null, teamRanking: OpponentRankingItemViewModel? = null, isVertical: Boolean = false, onClick: (String) -> Unit, onEditClick: (String) -> Unit) {
    val finalTeam = team ?: teamToManage ?: teamRanking?.team
    val teamId = team?.mid ?: teamRanking?.team?.mid
    Card(
        Modifier
            .padding(5.dp)
            .clickable { teamId?.let { onClick(it) } }) {
        when (isVertical) {
            false ->
                Row(modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 10.dp)
                    .fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    finalTeam?.picture?.let { AsyncImage(model = it, contentDescription = null, modifier = Modifier.size(50.dp))}
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        finalTeam?.name?.let { MKText(text = it, font = R.font.montserrat_bold, fontSize = 18) }
                        finalTeam?.shortName?.let { MKText(text = it, fontSize = 16, font = R.font.montserrat_regular) }
                    }
                    teamToManage?.let {
                        Image(
                            painter = painterResource(id = R.drawable.edit),
                            contentDescription = null,
                            modifier = Modifier
                                .size(25.dp)
                                .clickable { onEditClick(it.mid) }
                        )
                    }
                    teamRanking?.let {
                        Row {
                            Column {
                                MKText(text = R.string.wars_jou_es, fontSize = 12)
                                MKText(text = R.string.winrate, fontSize = 12)
                                MKText(text = R.string.score_moyen, fontSize = 12)
                            }
                            Spacer(modifier = Modifier.width(5.dp))
                            Column {
                                MKText(text = it.warsPlayedLabel, font = R.font.montserrat_bold, fontSize = 12)
                                MKText(text = it.winrateLabel, font = R.font.montserrat_bold, fontSize = 12)
                                MKText(text = it.averageLabel, font = R.font.montserrat_bold, fontSize = 12)
                            }
                        }
                    }
                }
            else -> Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(10.dp)) {
                finalTeam?.picture?.let { AsyncImage(model = it, contentDescription = null, modifier = Modifier.size(50.dp))}
                Spacer(Modifier.height(10.dp))
                finalTeam?.name?.let { MKText(text = it, font = R.font.montserrat_bold, fontSize = 18) }
            }
        }
    }
}

@Composable
@Preview
fun MKTeamItemPreview() {
    MKTeamItem(onEditClick = {}, onClick = {}, team = Team(
        TeamEntity(
            mid = "mid",
            name = "Harmonia",
            shortName = "HR"
        )
    ).apply { this.picture = "https://firebasestorage.googleapis.com/v0/b/stats-mk.appspot.com/o/1643723546718?alt=media&token=901e95bd-5d15-4ef4-a541-bdbf28d3bfca" })
}