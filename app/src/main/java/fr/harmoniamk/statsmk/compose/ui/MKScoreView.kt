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
fun MKScoreView(modifier: Modifier = Modifier, track: MKWarTrack? = null, war: MKWar? = null, isSmaller: Boolean = false, colored: Boolean = false) {
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
        isSmaller && !colored -> R.color.harmonia_dark
        war?.displayedDiff?.contains("-").isTrue -> R.color.lose
        war?.displayedDiff?.contains("+").isTrue -> R.color.green
        track?.displayedDiff?.contains("-").isTrue -> R.color.lose
        track?.displayedDiff?.contains("+").isTrue -> R.color.green
        else -> R.color.harmonia_dark
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        MKText(text = score, modifier = Modifier.padding(horizontal = if (isSmaller) 0.dp else 10.dp), fontSize = if (isSmaller) 14 else 22, font = R.font.orbitron_semibold)
        MKText(text = diff, modifier = Modifier.padding(horizontal = if (isSmaller) 0.dp else 10.dp), fontSize = if (isSmaller) 11 else 18, font = R.font.orbitron_regular, textColor = diffColor)
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
@Preview
@Composable
fun MKScorePreviewTrackSmall(){
    MKScoreView(track = MKWarTrack(NewWarTrack.mock()), isSmaller = true)
}