package fr.harmoniamk.statsmk.compose.ui

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
import fr.harmoniamk.statsmk.model.firebase.User

@Composable
fun MKPlayerItem(player: User) {
    Card() {
        Row(modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround, verticalAlignment = Alignment.CenterVertically) {
            player.picture?.let { AsyncImage(model = it, contentDescription = null, modifier = Modifier.size(50.dp)) }
            player.name?.let { MKText(text = it, font = R.font.montserrat_bold) }
        }
    }

}

@Composable
@Preview
fun MKPlayerItemPreview() {
    MKPlayerItem(player = User(
        mid = "mid",
        name = "Lari",
        picture = "https://firebasestorage.googleapis.com/v0/b/stats-mk.appspot.com/o/1643723546718?alt=media&token=901e95bd-5d15-4ef4-a541-bdbf28d3bfca"
    ))
}