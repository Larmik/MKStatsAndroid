package fr.harmoniamk.statsmk.compose.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.compose.viewModel.ColorsViewModel
import fr.harmoniamk.statsmk.extension.fromHex
import fr.harmoniamk.statsmk.model.network.MKPlayer

@Composable
fun MKCPlayerItem(player: MKPlayer, onPlayerClick: (String) -> Unit) {
    val colorsViewModel: ColorsViewModel = hiltViewModel()
    Card(
        elevation = 0.dp,
        backgroundColor = colorsViewModel.secondaryColorAlphaed, modifier = Modifier.padding(bottom = 5.dp).clickable { onPlayerClick(player.mkcId) }) {
        Row(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = player.flag,
                contentDescription = null,
                modifier = Modifier.width(40.dp).height(30.dp).clip(CircleShape),
                contentScale = ContentScale.FillBounds
            )
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MKText(
                    modifier = Modifier.widthIn(0.dp, 120.dp),
                    text = player.name,
                    font = R.font.montserrat_bold,
                    newTextColor = colorsViewModel.mainTextColor,
                    maxLines = 1
                )
            }
            when (player.isLeader.startsWith("1")) {
                true -> Row(
                    modifier = Modifier
                        .width(65.dp)
                        .height(20.dp)
                        .background(color = Color.fromHex("#029dbd"), shape = RoundedCornerShape(10.dp))
                        .border(BorderStroke(1.dp, Color.fromHex("#027d99")), shape = RoundedCornerShape(10.dp)),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MKText(
                        text = "Leader",
                        fontSize = 12,
                        newTextColor = colorsViewModel.secondaryTextColor,
                        font = R.font.montserrat_bold
                    )
                }
                else -> Spacer(Modifier.width(65.dp).height(20.dp))
            }
        }
    }
}
