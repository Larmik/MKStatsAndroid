package fr.harmoniamk.statsmk.compose.ui

import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.extension.isTrue
import fr.harmoniamk.statsmk.extension.positionColor
import fr.harmoniamk.statsmk.fragment.playerSelect.UserSelector
import fr.harmoniamk.statsmk.model.firebase.NewWarPositions
import fr.harmoniamk.statsmk.model.firebase.User
import fr.harmoniamk.statsmk.model.local.MKWarPosition

@Composable
fun MKPlayerItem(player: User? = null, position: MKWarPosition? = null, isSelected: Boolean = false, shockVisible: Boolean = false, onAddShock: () -> Unit = { }, onRemoveShock: () -> Unit = { }, onRootClick: () -> Unit = { }) {
    val finalPlayer = player ?: position?.player
    val backgroundColor = colorResource(id =
        when (isSelected) {
            true -> R.color.harmonia_dark
            else -> R.color.white
        }
    )
    val textColor = when (isSelected) {
        true -> R.color.white
        else -> R.color.harmonia_dark
    }

    Card(Modifier.padding(5.dp).clickable { onRootClick() }, backgroundColor = backgroundColor) {
        Row(modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            finalPlayer?.picture?.let { AsyncImage(model = it, contentDescription = null, modifier = Modifier.size(50.dp)) }
            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.SpaceAround, verticalAlignment = Alignment.CenterVertically) {
                finalPlayer?.name?.let { MKText(text = it, font = R.font.montserrat_bold, textColor = textColor) }
            }
            position?.let {
                MKText(text = it.position.position.toString(), font = R.font.mk_position, textColor = it.position.position.positionColor(), fontSize = 26, modifier = Modifier.padding(end = 15.dp))
                shockVisible.takeIf{ it }?.let {
                    Row(horizontalArrangement = Arrangement.Center) {
                        MKText(text = R.string.minus, font = R.font.orbitron_semibold, fontSize = 26, modifier = Modifier
                            .size(30.dp)
                            .clickable { onRemoveShock() })
                        Image(painter = painterResource(id = R.drawable.shock), contentDescription = null, modifier = Modifier
                            .size(30.dp)
                            .padding(horizontal = 5.dp))
                        MKText(text = R.string.plus, font = R.font.orbitron_semibold, fontSize = 26, modifier = Modifier
                            .size(30.dp)
                            .clickable { onAddShock() })
                    }
                }
            }
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
@Composable
@Preview
fun MKPlayerItemPositionPreview() {
    MKPlayerItem(position = MKWarPosition(
        position = NewWarPositions("mid", "pl_id", 4),
        player = User(
            mid = "mid",
            name = "Lari",
            picture = "https://firebasestorage.googleapis.com/v0/b/stats-mk.appspot.com/o/1643723546718?alt=media&token=901e95bd-5d15-4ef4-a541-bdbf28d3bfca"
        )), shockVisible = true
    )
}
@Composable
@Preview
fun MKPlayerItemPositionPreviewWithSkock() {
    MKPlayerItem(position = MKWarPosition(
        position = NewWarPositions("mid", "pl_id", 4),
        player = User(
            mid = "mid",
            name = "Lari",
            picture = "https://firebasestorage.googleapis.com/v0/b/stats-mk.appspot.com/o/1643723546718?alt=media&token=901e95bd-5d15-4ef4-a541-bdbf28d3bfca"
        ))
    )
}