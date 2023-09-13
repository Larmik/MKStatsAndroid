package fr.harmoniamk.statsmk.compose.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.extension.positionColor
import fr.harmoniamk.statsmk.model.local.MapDetails

@Composable
fun MKWarTrackItem(details: MapDetails, isIndiv: Boolean) {
    val bgColor = when {
        isIndiv -> R.color.white_alphaed
        details.warTrack.displayedDiff.contains("+") -> R.color.win_alphaed
        details.warTrack.displayedDiff.contains("-") -> R.color.lose_alphaed
        else -> R.color.white_alphaed
    }
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
            .padding(bottom = 5.dp)
            .background(color = colorResource(id = bgColor), shape = RoundedCornerShape(5.dp)), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(5.dp)) {
            MKText(text = details.war.name.orEmpty(), font = R.font.montserrat_bold)
            MKText(text = details.war.war?.createdDate.orEmpty(), fontSize = 12)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(5.dp)) {
            when (isIndiv) {
                true -> MKText(text = details.position.toString(), fontSize = 24, font = R.font.mk_position, textColor = details.position.positionColor())
                else -> MKScoreView(track = details.warTrack, isSmaller = true)
            }
        }
    }
}