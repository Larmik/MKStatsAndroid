package fr.harmoniamk.statsmk.compose.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.extension.isTrue
import fr.harmoniamk.statsmk.extension.safeSubList
import fr.harmoniamk.statsmk.compose.CurrentPlayerModel
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
                        Image(
                            painter = painterResource(id = R.drawable.arrow),
                            contentDescription = null,
                            colorFilter = colorFilter,
                            modifier = Modifier.size(11.dp).rotate(rotation)
                        )
                        MKText(text = it.player?.name ?: "", modifier = Modifier.defaultMinSize(minWidth = 120.dp))
                        MKText(text = it.score.toString(), font = R.font.montserrat_bold)
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
                        Image(
                            painter = painterResource(id = R.drawable.arrow),
                            contentDescription = null,
                            colorFilter = colorFilter,
                            modifier = Modifier.size(11.dp).rotate(rotation)
                        )
                        MKText(text = it.player?.name ?: "", modifier = Modifier.defaultMinSize(minWidth = 120.dp))
                        MKText(text = it.score.toString(), font = R.font.montserrat_bold)
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun MKPlayerListPreview() {
    MKPlayerList(players = listOf(
        CurrentPlayerModel(User(mid = "mid", name ="name"), 17, isNew = true),
        CurrentPlayerModel(User(mid = "mid", name ="name2"), 17),
        CurrentPlayerModel(User(mid = "mid", name ="name4"), 17),
        CurrentPlayerModel(User(mid = "mid", name ="name8"), 17),
        CurrentPlayerModel(User(mid = "mid", name ="name3"), 17),
        CurrentPlayerModel(User(mid = "mid", name ="tototititot"), 17),
        CurrentPlayerModel(User(mid = "mid", name ="name5"), 17, isOld = true),
        CurrentPlayerModel(User(mid = "mid", name ="name6"), 17)
    ))
}