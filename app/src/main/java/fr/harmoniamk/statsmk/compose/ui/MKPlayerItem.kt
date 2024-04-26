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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.compose.viewModel.ColorsViewModel
import fr.harmoniamk.statsmk.extension.positionColor
import fr.harmoniamk.statsmk.fragment.stats.playerRanking.PlayerRankingItemViewModel
import fr.harmoniamk.statsmk.model.local.MKWarPosition
import fr.harmoniamk.statsmk.model.network.MKPlayer

@Composable
fun MKPlayerItem(
    player: MKPlayer? = null,
    playerRanking: PlayerRankingItemViewModel? = null,
    position: MKWarPosition? = null,
    isSelected: Boolean = false,
    shockVisible: Boolean = false,
    shockCount: Int = 0,
    onAddShock: (String) -> Unit = { },
    onRemoveShock: (String) -> Unit = { },
    onRootClick: () -> Unit = { }) {
    val colorsViewModel: ColorsViewModel = hiltViewModel()

    val backgroundColor = when (isSelected) {
        true -> colorsViewModel.secondaryColor
        else -> colorsViewModel.secondaryColorAlphaed
    }

    val textColor = when (isSelected) {
        true -> colorsViewModel.secondaryTextColor
        else -> colorsViewModel.mainTextColor
    }
    Card(
        elevation = 0.dp,
        modifier = Modifier
            .padding(5.dp)
            .clickable { onRootClick() }, backgroundColor = backgroundColor) {
        Row(modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            when  {

                playerRanking?.user?.flag != null ->     AsyncImage(model = playerRanking.user.flag, contentDescription = null, modifier = Modifier.size(40.dp).clip(
                        CircleShape))
                player?.picture != null -> AsyncImage(model = player.picture, contentDescription = null, modifier = Modifier.size(40.dp).clip(
                    CircleShape))
                else -> Image(painter = painterResource(R.drawable.mk_stats_logo_picture), contentDescription = null, modifier = Modifier.size(40.dp).clip(
                    CircleShape))
            }

            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                playerRanking?.user?.name?.let { MKText(modifier = Modifier.widthIn(0.dp, 120.dp), text = it, font = R.font.montserrat_bold, newTextColor = textColor, maxLines = 1) }
                player?.name?.let { MKText(modifier = Modifier.widthIn(0.dp, 120.dp), text = it, font = R.font.montserrat_bold, newTextColor = textColor, maxLines = 1) }
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
                MKText(text = it.position.position.toString(), font = R.font.mk_position, newTextColor = it.position.position.positionColor(), fontSize = 26, modifier = Modifier.padding(end = 15.dp))
                shockVisible.takeIf{ it }?.let {
                    Row(horizontalArrangement = Arrangement.Center) {
                        MKText(text = R.string.minus, font = R.font.orbitron_semibold, fontSize = 26, modifier = Modifier
                            .size(30.dp)
                            .clickable {
                                position.mkcPlayer?.mkcId?.let { mid ->
                                    onRemoveShock(mid)
                                }
                            })
                        Image(painter = painterResource(id = R.drawable.shock), contentDescription = null, modifier = Modifier
                            .size(30.dp)
                            .padding(horizontal = 5.dp))
                        MKText(text = R.string.plus, font = R.font.orbitron_semibold, fontSize = 26, modifier = Modifier
                            .size(30.dp)
                            .clickable {
                                position.mkcPlayer?.mkcId?.let { mid ->
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
         }
    }
}
