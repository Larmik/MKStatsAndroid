package fr.harmoniamk.statsmk.compose.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.extension.isTrue
import fr.harmoniamk.statsmk.model.firebase.NewWar
import fr.harmoniamk.statsmk.model.firebase.NewWarTrack
import fr.harmoniamk.statsmk.model.local.MKWar
import fr.harmoniamk.statsmk.model.local.MKWarTrack
import fr.harmoniamk.statsmk.model.mock.mock

@Composable
fun MKScoreView(track: MKWarTrack? = null, war: MKWar? = null) {
    val score = when {
        war != null -> war.displayedScore
        track != null -> track.displayedResult
        else -> ""
    }
    val diff = when {
        war != null -> war.displayedDiff
        track != null -> track.displayedDiff
        else -> ""
    }
    val diffColor = when {
        war?.displayedDiff?.contains("-").isTrue -> R.color.lose
        war?.displayedDiff?.contains("+").isTrue -> R.color.green
        track?.displayedDiff?.contains("-").isTrue -> R.color.lose
        track?.displayedDiff?.contains("+").isTrue -> R.color.green
        else -> R.color.harmonia_dark
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        MKText(text = score, modifier = Modifier.padding(10.dp), fontSize = 20, font = R.font.orbitron_semibold)
        MKText(text = diff, modifier = Modifier.padding(bottom = 10.dp), fontSize = 16, font = R.font.orbitron_regular, textColor = diffColor)
    }

}

@Preview
@Composable
fun MKScorePreviewWar(){
    MKScoreView(war = MKWar(NewWar.mock()))
}

@Preview
@Composable
fun MKScorePreviewTrack(){
    MKScoreView(track = MKWarTrack(NewWarTrack.mock()))
}