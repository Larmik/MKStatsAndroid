package fr.harmoniamk.statsmk.compose.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.extension.safeSubList
import fr.harmoniamk.statsmk.fragment.currentWar.CurrentPlayerModel
import fr.harmoniamk.statsmk.model.firebase.User

@Composable
fun MKPlayerList(players: List<CurrentPlayerModel>) {
    Card(elevation = 0.dp, backgroundColor = colorResource(R.color.transparent_white), modifier = Modifier.padding(10.dp)) {
        Row(Modifier.padding(10.dp)) {
            LazyColumn(Modifier.weight(1f)) {
                items(players.safeSubList(0,3)) {
                    Row(Modifier.padding(vertical = 1.5.dp)) {
                        MKText(text = it.player?.name ?: "", modifier = Modifier.defaultMinSize(minWidth = 120.dp))
                        MKText(text = it.score.toString(), font = R.font.montserrat_bold)
                    }
                }
            }
            LazyColumn(Modifier.weight(1f)) {
                items(players.safeSubList(3, players.size)) {
                    Row(Modifier.padding(vertical = 1.5.dp)) {
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
        CurrentPlayerModel(User(mid = "mid", name ="name"), 17),
        CurrentPlayerModel(User(mid = "mid", name ="name2"), 17),
        CurrentPlayerModel(User(mid = "mid", name ="name3"), 17),
        CurrentPlayerModel(User(mid = "mid", name ="tototititot"), 17),
        CurrentPlayerModel(User(mid = "mid", name ="name5"), 17),
        CurrentPlayerModel(User(mid = "mid", name ="name6"), 17)
    ))
}