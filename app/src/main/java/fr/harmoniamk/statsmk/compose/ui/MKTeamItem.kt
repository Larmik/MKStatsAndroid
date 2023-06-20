package fr.harmoniamk.statsmk.compose.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.database.entities.TeamEntity
import fr.harmoniamk.statsmk.model.firebase.Team

@Composable
fun MKTeamItem(team: Team, onClick: (String) -> Unit) {
    Card(Modifier.padding(5.dp).clickable { onClick(team.mid) }) {
        Row(modifier = Modifier
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            team.picture?.let { AsyncImage(model = it, contentDescription = null, modifier = Modifier.size(50.dp))}
            team.name?.let { MKText(text = it, font = R.font.montserrat_bold, fontSize = 18) }
            team.shortName?.let { MKText(text = it, fontSize = 16, font = R.font.montserrat_regular) }
        }
    }
}

@Composable
@Preview
fun MKTeamItemPreview() {
    MKTeamItem(team = Team(
        TeamEntity(
            mid = "mid",
            name = "Harmonia",
            shortName = "HR"
        )
    ).apply { this.picture = "https://firebasestorage.googleapis.com/v0/b/stats-mk.appspot.com/o/1643723546718?alt=media&token=901e95bd-5d15-4ef4-a541-bdbf28d3bfca" }) {

    }
}