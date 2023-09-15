package fr.harmoniamk.statsmk.compose.ui.stats

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.compose.ui.MKText
import fr.harmoniamk.statsmk.model.local.Stats

@Composable
fun MKTeamStatsView(stats: Stats, userId: String? = null, onMostPlayedClick: (String?, String?) -> Unit, onMostDefeatedClick: (String?, String?) -> Unit, onLessDefeatedClick: (String?, String?) -> Unit) {
    Column(Modifier.padding(bottom = 20.dp)) {
        MKText(text = R.string.adversaires, fontSize = 16, font = R.font.montserrat_bold, modifier = Modifier.padding(bottom = 10.dp))
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
            .padding(bottom = 10.dp)
            .fillMaxWidth()
            .clickable { onMostPlayedClick(stats.mostPlayedTeam?.team?.mid, userId) }) {
            MKText(text = R.string.equipe_la_plus_jou_e, fontSize = 12)
            MKText(text = stats.mostPlayedTeam?.teamName.toString(), font = R.font.montserrat_bold)
            MKText(text = String.format(stringResource(id = R.string.matchs_played), stats.mostPlayedTeam?.totalPlayed.toString()), fontSize = 12)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f).clickable { onMostDefeatedClick(stats.mostDefeatedTeam?.team?.mid, userId) }, horizontalAlignment = Alignment.CenterHorizontally) {
                MKText(text = R.string.la_plus_gagn_e, fontSize = 12)
                MKText(text = stats.mostDefeatedTeam?.teamName.toString(), font = R.font.montserrat_bold)
                MKText(text = String.format(stringResource(id = R.string.victory_placeholder), stats.mostDefeatedTeam?.totalPlayed.toString()), fontSize = 12)
            }
            Column(Modifier.weight(1f).clickable { onLessDefeatedClick(stats.lessDefeatedTeam?.team?.mid, userId) }, horizontalAlignment = Alignment.CenterHorizontally) {
                MKText(text = R.string.la_plus_perdue, fontSize = 12)
                MKText(text = stats.lessDefeatedTeam?.teamName.toString(), font = R.font.montserrat_bold)
                MKText(text = String.format(stringResource(id = R.string.defeat_placeholder), stats.lessDefeatedTeam?.totalPlayed.toString()), fontSize = 12)
            }
        }
    }
}