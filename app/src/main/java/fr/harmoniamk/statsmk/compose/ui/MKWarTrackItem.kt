package fr.harmoniamk.statsmk.compose.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.compose.viewModel.ColorsViewModel
import fr.harmoniamk.statsmk.extension.positionColor
import fr.harmoniamk.statsmk.model.local.MapDetails

@Composable
fun MKWarTrackItem(details: MapDetails, isIndiv: Boolean) {
    val colorsViewModel: ColorsViewModel = hiltViewModel()
    val borderColor = when {
        isIndiv -> R.color.transparent
        details.warTrack.displayedDiff.contains("+") -> R.color.win
        details.warTrack.displayedDiff.contains("-") -> R.color.lose
        else -> R.color.transparent
    }
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
            .padding(bottom = 5.dp)
            .border(2.dp, color = colorResource(id = borderColor), shape = RoundedCornerShape(5.dp))
            .background(
                color = colorsViewModel.secondaryColorAlphaed,
                shape = RoundedCornerShape(5.dp)
            ), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(10.dp)) {
            MKText(text = details.war.name.orEmpty(), font = R.font.montserrat_bold)
            MKText(text = details.war.war?.createdDate.orEmpty(), fontSize = 12)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            details.warTrack.track?.shocks?.takeIf { it.isNotEmpty() }?.let {
                Image(
                    painter = painterResource(id = R.drawable.shock),
                    contentDescription = null,
                    modifier = Modifier.size(15.dp)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(10.dp)) {
                when (isIndiv) {
                    true -> MKText(text = details.position.toString(), fontSize = 24, font = R.font.mk_position, textColor = details.position.positionColor())
                    else -> MKScoreView(track = details.warTrack, isSmaller = true)
                }
            }
        }
    }
}