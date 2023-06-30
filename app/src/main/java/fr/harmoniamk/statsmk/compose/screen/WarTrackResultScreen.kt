package fr.harmoniamk.statsmk.compose.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.compose.ui.MKBaseScreen
import fr.harmoniamk.statsmk.compose.ui.MKButton
import fr.harmoniamk.statsmk.compose.ui.MKPlayerItem
import fr.harmoniamk.statsmk.compose.ui.MKScoreView
import fr.harmoniamk.statsmk.compose.ui.MKTrackItem
import fr.harmoniamk.statsmk.compose.viewModel.WarTrackResultViewModel
import kotlinx.coroutines.flow.filterNotNull

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun WarTrackResultScreen(viewModel: WarTrackResultViewModel = hiltViewModel(), onBack: () -> Unit, backToCurrent: () -> Unit, goToResume: (String) -> Unit) {
    val war = viewModel.sharedWar.collectAsState()
    val trackIndexRes = viewModel.sharedTrackNumber.collectAsState()
    val map = viewModel.sharedCurrentMap.collectAsState()
    val positions = viewModel.sharedWarPos.collectAsState()
    val track = viewModel.sharedTrack.collectAsState()
    BackHandler {
        onBack()
    }
    LaunchedEffect(Unit) {
        viewModel.sharedBackToCurrent.collect {
            backToCurrent()
        }
    }
    LaunchedEffect(Unit) {
        viewModel.sharedGoToWarResume.filterNotNull().collect {
            goToResume(it)
        }
    }

    MKBaseScreen(title = war.value?.name.orEmpty(), subTitle = trackIndexRes.value) {
        map.value?.let { MKTrackItem(map = it) }
        LazyColumn {
            items(positions.value.orEmpty()) {
                MKPlayerItem(position = it, shockVisible = true)
            }
        }
        track.value?.let { MKScoreView(track = it) }
        MKButton(text = R.string.valider, onClick = viewModel::onValid)
    }

}