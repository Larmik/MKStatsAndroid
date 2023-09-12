package fr.harmoniamk.statsmk.compose.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.res.stringResource
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.compose.ui.MKBaseScreen
import fr.harmoniamk.statsmk.compose.ui.MKButton
import fr.harmoniamk.statsmk.compose.ui.MKPlayerItem
import fr.harmoniamk.statsmk.compose.ui.MKScoreView
import fr.harmoniamk.statsmk.compose.ui.MKTrackItem
import fr.harmoniamk.statsmk.compose.viewModel.WarTrackResultViewModel
import fr.harmoniamk.statsmk.compose.viewModel.WarTrackResultViewModel.Companion.viewModel

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun WarTrackResultScreen(
    trackIndex: Int = -1,
    editing: Boolean = false,
    onBack: () -> Unit,
    backToCurrent: () -> Unit
) {
    val viewModel: WarTrackResultViewModel = viewModel(trackResultIndex = trackIndex, editing = editing)
    val war = viewModel.sharedWar.collectAsState()
    val map = viewModel.sharedCurrentMap.collectAsState()
    val positions = viewModel.sharedWarPos.collectAsState()
    val shocks = viewModel.sharedShocks.collectAsState()
    val track = viewModel.sharedTrack.collectAsState()
    val trackIndexRes = viewModel.sharedTrackNumber.collectAsState()

    BackHandler {
        viewModel.onBack()
        onBack()
    }
    LaunchedEffect(Unit) {
        viewModel.sharedBackToCurrent.collect {
            backToCurrent()
        }
    }
    MKBaseScreen(title = war.value?.name.orEmpty(), subTitle = trackIndexRes.value?.let { stringResource(id = it) }) {
        map.value?.let { MKTrackItem(map = it) }
        LazyColumn {
            items(positions.value.orEmpty()) {
                MKPlayerItem(
                    position = it,
                    shockVisible = true,
                    shockCount = shocks.value?.singleOrNull { shock -> shock.playerId == it.player?.mid }?.count ?: 0,
                    onAddShock = viewModel::onAddShock,
                    onRemoveShock = viewModel::onRemoveShock
                ) {}
            }
        }
        track.value?.let { MKScoreView(track = it) }
        MKButton(text = R.string.valider, onClick = viewModel::onValid)
    }
}