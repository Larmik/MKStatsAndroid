package fr.harmoniamk.statsmk.compose.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.extension.positionColor
import fr.harmoniamk.statsmk.fragment.stats.playerRanking.PlayerRankingItemViewModel
import fr.harmoniamk.statsmk.model.firebase.NewWarPositions
import fr.harmoniamk.statsmk.model.firebase.User
import fr.harmoniamk.statsmk.model.local.MKWarPosition

@Composable
fun MKPlayerItem(
    player: User? = null,
    playerRanking: PlayerRankingItemViewModel? = null,
    position: MKWarPosition? = null,
    isSelected: Boolean = false,
    shockVisible: Boolean = false,
    editVisible: Boolean = false,
    shockCount: Int = 0,
    onAddShock: (String) -> Unit = { },
    onRemoveShock: (String) -> Unit = { },
    onRootClick: () -> Unit = { },
    onEditClick: (User) -> Unit) {
    val finalPlayer = player ?: position?.player ?: playerRanking?.user
    val backgroundColor = colorResource(id =
        when (isSelected) {
            true -> R.color.harmonia_dark
            else -> R.color.white_alphaed
        }
    )
    val textColor = when (isSelected) {
        true -> R.color.white
        else -> R.color.harmonia_dark
    }

    Card(
        Modifier
            .padding(5.dp)
            .clickable { onRootClick() }, backgroundColor = backgroundColor) {
        Row(modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            finalPlayer?.picture?.let { AsyncImage(model = it, contentDescription = null, modifier = Modifier.size(50.dp)) }
            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                finalPlayer?.name?.let { MKText(text = it, font = R.font.montserrat_bold, textColor = textColor) }
                shockCount.takeIf { it > 0 }?.let {
                    Image(painter = painterResource(id = R.drawable.shock), contentDescription = null, modifier = Modifier
                        .size(25.dp)
                        .padding(start = 10.dp))
                    MKText(text = String.format(
                        stringResource(id = R.string.shock_count_placeholder),
                        shockCount.toString()
                    ), font = R.font.orbitron_semibold)
                }

            }
            position?.let {
                MKText(text = it.position.position.toString(), font = R.font.mk_position, textColor = it.position.position.positionColor(), fontSize = 26, modifier = Modifier.padding(end = 15.dp))
                shockVisible.takeIf{ it }?.let {
                    Row(horizontalArrangement = Arrangement.Center) {
                        MKText(text = R.string.minus, font = R.font.orbitron_semibold, fontSize = 26, modifier = Modifier
                            .size(30.dp)
                            .clickable {
                                position.player?.mid?.let { mid ->
                                    onRemoveShock(mid)
                                }
                            })
                        Image(painter = painterResource(id = R.drawable.shock), contentDescription = null, modifier = Modifier
                            .size(30.dp)
                            .padding(horizontal = 5.dp))
                        MKText(text = R.string.plus, font = R.font.orbitron_semibold, fontSize = 26, modifier = Modifier
                            .size(30.dp)
                            .clickable {
                                position.player?.mid?.let { mid ->
                                    onAddShock(mid)
                                }
                            })
                    }
                }
            }
            playerRanking?.let {
                Row {
                    Column {
                        MKText(text = R.string.wars_jou_es, fontSize = 12)
                        MKText(text = R.string.winrate, fontSize = 12)
                        MKText(text = R.string.score_moyen, fontSize = 12)
                    }
                    Spacer(modifier = Modifier.width(5.dp))
                    Column {
                        MKText(text = it.warsPlayedLabel, font = R.font.montserrat_bold, fontSize = 12)
                        MKText(text = it.winrateLabel, font = R.font.montserrat_bold, fontSize = 12)
                        MKText(text = it.averageLabel, font = R.font.montserrat_bold, fontSize = 12)
                    }
                }
            }
            player?.takeIf { editVisible }?.let {
                Image(painter = painterResource(id = R.drawable.edit), contentDescription = null, modifier = Modifier
                    .size(25.dp)
                    .clickable { onEditClick(it) })
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
    )) {

    }
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
    ) {}
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
        ),
    ), shockCount = 2
    ) {}
}