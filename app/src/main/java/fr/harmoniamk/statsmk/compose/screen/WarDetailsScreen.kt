package fr.harmoniamk.statsmk.compose.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.compose.ui.MKBaseScreen
import fr.harmoniamk.statsmk.compose.ui.MKPlayerList
import fr.harmoniamk.statsmk.compose.ui.MKScoreView
import fr.harmoniamk.statsmk.compose.ui.MKText
import fr.harmoniamk.statsmk.compose.ui.MKTrackItem
import fr.harmoniamk.statsmk.compose.viewModel.WarDetailsViewModel.Companion.viewModel

@Composable
fun WarDetailsScreen(id: String?) {
    val viewModel = viewModel(id = id)

    val war = viewModel.sharedWar.collectAsState()
    val players = viewModel.sharedWarPlayers.collectAsState()
    val tracks = viewModel.sharedTracks.collectAsState()

    MKBaseScreen(title = war.value?.name.orEmpty(), subTitle = war.value?.war?.createdDate) {
        players.value?.let {
            MKPlayerList(players = it)
        }
        MKScoreView(war = war.value)
        Row() {
            Column(Modifier.weight(1f).padding(horizontal = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                MKText(text = R.string.meilleur_circuit, font = R.font.montserrat_bold)
                MKTrackItem(isVertical = true, track = tracks.value?.maxByOrNull { track -> track.teamScore }) {

                }
            }
            Column(Modifier.weight(1f).padding(horizontal = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                MKText(text = R.string.pire_circuit, font = R.font.montserrat_bold)
                MKTrackItem(isVertical = true, track = tracks.value?.minByOrNull { track -> track.teamScore }) {

                }
            }
        }
        tracks.value?.let {
            MKText(text = R.string.tous_les_circuits, font = R.font.montserrat_bold)
            LazyColumn(Modifier.padding(10.dp)) {
                items(items = it) {
                    MKTrackItem(track = it) {

                    }
                }
            }
        }

    }
}