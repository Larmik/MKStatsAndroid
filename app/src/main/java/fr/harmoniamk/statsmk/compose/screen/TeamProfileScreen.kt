package fr.harmoniamk.statsmk.compose.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.compose.ui.MKBaseScreen
import fr.harmoniamk.statsmk.compose.ui.MKCPlayerItem
import fr.harmoniamk.statsmk.compose.ui.MKText
import fr.harmoniamk.statsmk.compose.viewModel.TeamProfileViewModel.Companion.viewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class, ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
fun TeamProfileScreen(id: String, onPlayerClick: (String) -> Unit) {
    val viewModel = viewModel(id)
    val picture = viewModel.sharedPictureLoaded.collectAsState()
    val team = viewModel.sharedTeam.collectAsState()
    val players by viewModel.sharedPlayers.collectAsState()

    viewModel.init()

    MKBaseScreen(title = team.value?.team_name.orEmpty()) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(10.dp), horizontalArrangement = Arrangement.SpaceAround) {
            if (picture.value != null) {
                when (picture.value?.isEmpty()) {
                    true -> Image(
                        painter = painterResource(R.drawable.mk_stats_logo_picture),
                        contentDescription = null,
                        modifier = Modifier.size(120.dp).clip(CircleShape)
                    )
                    else ->  AsyncImage(
                        model = picture.value,
                        contentDescription = null,
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                    )
                }

            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.height(120.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                team.value?.createdDate?.let {
                    MKText(text = String.format("Date de création : %s", it), fontSize = 12, font = R.font.montserrat_bold)
                    Spacer(Modifier.height(10.dp))
                }
                MKText(text = team.value?.team_description.orEmpty(), fontSize = 10)
            }
        }

        LazyColumn(Modifier.offset(y = (-5).dp)) {
            players.takeIf { it.isNotEmpty() }?.let {
                stickyHeader {
                    Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically, modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .background(color = colorResource(R.color.harmonia_dark))) {
                        MKText(font = R.font.montserrat_bold, fontSize = 18, text = "Roster", textColor = R.color.white)
                    }
                }
                items(items = players) {
                    MKCPlayerItem(player = it, onPlayerClick = onPlayerClick)
                }
            }

        }
    }
}