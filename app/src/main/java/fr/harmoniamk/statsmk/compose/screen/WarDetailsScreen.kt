package fr.harmoniamk.statsmk.compose.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.compose.ui.MKBaseScreen
import fr.harmoniamk.statsmk.compose.ui.MKPenaltyView
import fr.harmoniamk.statsmk.compose.ui.MKPlayerList
import fr.harmoniamk.statsmk.compose.ui.MKScoreView
import fr.harmoniamk.statsmk.compose.ui.MKShockView
import fr.harmoniamk.statsmk.compose.ui.MKText
import fr.harmoniamk.statsmk.compose.ui.MKTrackItem
import fr.harmoniamk.statsmk.compose.viewModel.WarDetailsViewModel.Companion.viewModel

@Composable
@OptIn(ExperimentalMaterialApi::class)
fun WarDetailsScreen(id: String?, onTrackClick: (String) -> Unit) {
    val viewModel = viewModel(id = id)
    val war = viewModel.sharedWar.collectAsState()
    val players = viewModel.sharedWarPlayers.collectAsState()
    val tracks = viewModel.sharedTracks.collectAsState()
    val bestTrack = tracks.value?.maxByOrNull { track -> track.teamScore }
    val worstTrack = tracks.value?.minByOrNull { track -> track.teamScore }

    MKBaseScreen(title = war.value?.name.orEmpty(), subTitle = war.value?.war?.createdDate) {
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            MKPenaltyView(modifier = Modifier.weight(0.8f), penalties = war.value?.war?.penalties)
            MKScoreView(modifier = Modifier.weight(1.2f), war = war.value)
            MKShockView(modifier = Modifier.weight(0.8f), tracks = war.value?.warTracks)
        }
        players.value?.let { MKPlayerList(players = it) }
        Row {
            Column(
                Modifier
                    .weight(1f)
                    .padding(horizontal = 10.dp), horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MKText(text = R.string.meilleur_circuit, font = R.font.montserrat_bold)
                MKTrackItem(
                    isVertical = true,
                    track = bestTrack,
                    goToDetails = { onTrackClick(bestTrack?.track?.mid.orEmpty()) })
            }
            Column(
                Modifier
                    .weight(1f)
                    .padding(horizontal = 10.dp), horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MKText(text = R.string.pire_circuit, font = R.font.montserrat_bold)
                MKTrackItem(
                    isVertical = true,
                    track = worstTrack,
                    goToDetails = { onTrackClick(worstTrack?.track?.mid.orEmpty()) })
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        tracks.value?.let {
            MKText(text = R.string.tous_les_circuits, font = R.font.montserrat_bold)
            LazyColumn(Modifier.padding(10.dp)) {
                items(items = it) {
                    MKTrackItem(
                        modifier = Modifier.padding(bottom = 5.dp),
                        track = it,
                        goToDetails = { _ -> onTrackClick(it.track?.mid.orEmpty()) })
                }
            }
        }
    }
}