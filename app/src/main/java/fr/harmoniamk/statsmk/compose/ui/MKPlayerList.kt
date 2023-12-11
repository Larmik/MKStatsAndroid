package fr.harmoniamk.statsmk.compose.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.extension.isTrue
import fr.harmoniamk.statsmk.extension.safeSubList
import fr.harmoniamk.statsmk.model.local.CurrentPlayerModel
import fr.harmoniamk.statsmk.model.firebase.User

@Composable
fun MKPlayerList(players: List<CurrentPlayerModel>) {
    val splitIndex = when (players.size % 2) {
        0 -> players.size/2
        else -> players.size/2 + 1
    }
    Card(elevation = 0.dp, backgroundColor = colorResource(R.color.transparent_white), modifier = Modifier.padding(10.dp)) {
        Row(Modifier.padding(10.dp)) {
            LazyColumn(Modifier.weight(1f)) {
                items(players.safeSubList(0, splitIndex)) {
                    val colorFilter = ColorFilter.tint(
                        color = colorResource(id = when {
                            it.isNew.isTrue -> R.color.luigi
                            it.isOld.isTrue -> R.color.mario
                            else -> R.color.transparent
                        })
                    )
                    val rotation = when {
                        it.isOld.isTrue -> 180f
                        else -> 0f
                    }
                    Row(Modifier.padding(vertical = 1.5.dp), verticalAlignment = Alignment.CenterVertically) {
                        Row(
                            modifier = Modifier.width(120.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.arrow),
                                contentDescription = null,
                                colorFilter = colorFilter,
                                modifier = Modifier
                                    .size(11.dp)
                                    .rotate(rotation)
                            )
                            MKText(modifier = Modifier.padding(horizontal = 5.dp),text = it.player?.name ?: "", maxLines = 1)

                        }
                        MKText(text = it.score.toString(), font = R.font.montserrat_bold)
                        it.shockCount.takeIf { it > 0 }?.let {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Image(painter = painterResource(id = R.drawable.shock), contentDescription = null, modifier = Modifier.size(15.dp))
                                if (it > 1) MKText(text = String.format(stringResource(id = R.string.shock_count_placeholder), it.toString()), fontSize = 12)
                            }
                        }
                    }
                }
            }
            LazyColumn(Modifier.weight(1f)) {
                items(players.safeSubList(splitIndex, players.size)) {
                    val colorFilter = ColorFilter.tint(
                        color = colorResource(id = when {
                            it.isNew.isTrue -> R.color.luigi
                            it.isOld.isTrue -> R.color.mario
                            else -> R.color.transparent
                        })
                    )
                    val rotation = when {
                        it.isOld.isTrue -> 180f
                        else -> 0f
                    }
                    Row(Modifier.padding(vertical = 1.5.dp), verticalAlignment = Alignment.CenterVertically) {
                        Row(
                            modifier = Modifier.width(120.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.arrow),
                                contentDescription = null,
                                colorFilter = colorFilter,
                                modifier = Modifier
                                    .size(11.dp)
                                    .rotate(rotation)
                            )
                            MKText(modifier = Modifier.padding(horizontal = 5.dp),text = it.player?.name ?: "", maxLines = 1)

                        }
                        MKText(text = it.score.toString(), font = R.font.montserrat_bold)
                        it.shockCount.takeIf { it > 0 }?.let {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Image(painter = painterResource(id = R.drawable.shock), contentDescription = null, modifier = Modifier.size(15.dp))
                                if (it > 1) MKText(text = String.format(stringResource(id = R.string.shock_count_placeholder), it.toString()), fontSize = 12)
                            }
                        }
                    }
                }
            }
        }
    }
}
