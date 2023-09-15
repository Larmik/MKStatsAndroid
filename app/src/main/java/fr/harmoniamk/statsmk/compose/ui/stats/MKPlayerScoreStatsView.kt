package fr.harmoniamk.statsmk.compose.ui.stats

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.compose.ui.MKText
import fr.harmoniamk.statsmk.model.local.Stats

@Composable
fun MKPlayerScoreStatsView(
    stats: Stats,
    onHighestClick: (String?) -> Unit,
    onLowestClick: (String?) -> Unit
) {
    Column(Modifier.padding(bottom = 20.dp)) {
        MKText(
            text = "Scores",
            fontSize = 16,
            font = R.font.montserrat_bold,
            modifier = Modifier.padding(bottom = 10.dp)
        )
        Row {
            Column(
                Modifier
                    .weight(1f)
                    .clickable { onLowestClick(stats.lowestScore?.war?.war?.mid) },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MKText(text = R.string.pire_score, fontSize = 12)
                MKText(
                    text = stats.lowestPlayerScore?.first?.toString()
                        ?: stats.lowestScore?.score.toString(),
                    fontSize = 20,
                    font = R.font.orbitron_semibold
                )
                MKText(text = stats.lowestScore?.opponentLabel.toString())
                MKText(text = stats.lowestScore?.war?.war?.createdDate.toString(), fontSize = 12)
            }
            Column(
                Modifier
                    .weight(1f)
                    .clickable { onHighestClick(stats.highestScore?.war?.war?.mid) },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MKText(text = R.string.meilleur_score, fontSize = 12)
                MKText(
                    text = stats.highestPlayerScore?.first?.toString()
                        ?: stats.highestScore?.score.toString(),
                    fontSize = 20,
                    font = R.font.orbitron_semibold
                )
                MKText(text = stats.highestScore?.opponentLabel.toString())
                MKText(text = stats.highestScore?.war?.war?.createdDate.toString(), fontSize = 12)
            }
        }
    }
}