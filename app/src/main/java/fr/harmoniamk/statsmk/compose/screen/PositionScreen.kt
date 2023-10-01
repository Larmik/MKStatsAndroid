package fr.harmoniamk.statsmk.compose.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.compose.ui.MKBaseScreen
import fr.harmoniamk.statsmk.compose.ui.MKPositionSelector
import fr.harmoniamk.statsmk.compose.ui.MKScoreView
import fr.harmoniamk.statsmk.compose.ui.MKText
import fr.harmoniamk.statsmk.compose.ui.MKTrackItem
import fr.harmoniamk.statsmk.compose.viewModel.PositionViewModel.Companion.viewModel
import fr.harmoniamk.statsmk.extension.isTrue
import kotlinx.coroutines.flow.filterNotNull

@Composable
@OptIn(ExperimentalMaterialApi::class)
fun PositionScreen(
    trackIndex: Int,
    editing: Boolean = false,
    onBack: () -> Unit,
    onNext: (Int) -> Unit
) {
    val viewModel = viewModel(index = trackIndex, editing = editing)
    val war = viewModel.sharedWar.collectAsState()
    val map = viewModel.sharedCurrentMap.collectAsState()
    val selectedPositions = viewModel.sharedSelectedPositions.collectAsState()
    val playerName = viewModel.sharedPlayerLabel.collectAsState()
    val trackIndexRes = viewModel.sharedTrackNumber.collectAsState()

    BackHandler { viewModel.onBack() }

    LaunchedEffect(Unit) {
        viewModel.sharedQuit.filterNotNull().collect {
            if (editing) viewModel.clearPos()
            onBack()
        }
    }
    LaunchedEffect(Unit) {
        viewModel.sharedGoToResult.filterNotNull().collect { onNext(trackIndex) }
    }
    MKBaseScreen(
        title = war.value?.name.orEmpty(),
        subTitle = trackIndexRes.value?.let { stringResource(id = it) }) {
        map.value?.let { MKTrackItem(map = it) }
        when (editing) {
            true -> MKScoreView(
                track = war.value?.warTracks?.getOrNull(trackIndex),
                modifier = Modifier.padding(vertical = 10.dp)
            )
            else -> MKScoreView(war = war.value, modifier = Modifier.padding(vertical = 10.dp))
        }
        MKText(
            text = String.format(
                stringResource(id = R.string.select_pos_placeholder),
                playerName.value
            )
        )
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            Row {
                MKPositionSelector(
                    position = 1,
                    modifier = Modifier.weight(1f),
                    isVisible = !selectedPositions.value?.contains(1).isTrue
                ) {
                    viewModel.takeIf { !selectedPositions.value?.contains(1).isTrue }
                        ?.onPositionClick(1)
                }
                MKPositionSelector(
                    position = 2,
                    modifier = Modifier.weight(1f),
                    isVisible = !selectedPositions.value?.contains(2).isTrue
                ) {
                    viewModel.takeIf { !selectedPositions.value?.contains(2).isTrue }
                        ?.onPositionClick(2)
                }
                MKPositionSelector(
                    position = 3,
                    modifier = Modifier.weight(1f),
                    isVisible = !selectedPositions.value?.contains(3).isTrue
                ) {
                    viewModel.takeIf { !selectedPositions.value?.contains(3).isTrue }
                        ?.onPositionClick(3)
                }
            }
            Row {
                MKPositionSelector(
                    position = 4,
                    modifier = Modifier.weight(1f),
                    isVisible = !selectedPositions.value?.contains(4).isTrue
                ) {
                    viewModel.takeIf { !selectedPositions.value?.contains(4).isTrue }
                        ?.onPositionClick(4)
                }
                MKPositionSelector(
                    position = 5,
                    modifier = Modifier.weight(1f),
                    isVisible = !selectedPositions.value?.contains(5).isTrue
                ) {
                    viewModel.takeIf { !selectedPositions.value?.contains(5).isTrue }
                        ?.onPositionClick(5)
                }
                MKPositionSelector(
                    position = 6,
                    modifier = Modifier.weight(1f),
                    isVisible = !selectedPositions.value?.contains(6).isTrue
                ) {
                    viewModel.takeIf { !selectedPositions.value?.contains(6).isTrue }
                        ?.onPositionClick(6)
                }
            }
            Row {
                MKPositionSelector(
                    position = 7,
                    modifier = Modifier.weight(1f),
                    isVisible = !selectedPositions.value?.contains(7).isTrue
                ) {
                    viewModel.takeIf { !selectedPositions.value?.contains(7).isTrue }
                        ?.onPositionClick(7)
                }
                MKPositionSelector(
                    position = 8,
                    modifier = Modifier.weight(1f),
                    isVisible = !selectedPositions.value?.contains(8).isTrue
                ) {
                    viewModel.takeIf { !selectedPositions.value?.contains(8).isTrue }
                        ?.onPositionClick(8)
                }
                MKPositionSelector(
                    position = 9,
                    modifier = Modifier.weight(1f),
                    isVisible = !selectedPositions.value?.contains(9).isTrue
                ) {
                    viewModel.takeIf { !selectedPositions.value?.contains(9).isTrue }
                        ?.onPositionClick(9)
                }
            }
            Row {
                MKPositionSelector(
                    position = 10,
                    modifier = Modifier.weight(1f),
                    isVisible = !selectedPositions.value?.contains(10).isTrue
                ) {
                    viewModel.takeIf { !selectedPositions.value?.contains(10).isTrue }
                        ?.onPositionClick(10)
                }
                MKPositionSelector(
                    position = 11,
                    modifier = Modifier.weight(1f),
                    isVisible = !selectedPositions.value?.contains(11).isTrue
                ) {
                    viewModel.takeIf { !selectedPositions.value?.contains(11).isTrue }
                        ?.onPositionClick(11)
                }
                MKPositionSelector(
                    position = 12,
                    modifier = Modifier.weight(1f),
                    isVisible = !selectedPositions.value?.contains(12).isTrue
                ) {
                    viewModel.takeIf { !selectedPositions.value?.contains(12).isTrue }
                        ?.onPositionClick(12)
                }
            }
        }
    }
}

@Preview
@Composable
fun PositionScreenPreview() {
    PositionScreen(trackIndex = 23, editing = false, {}) {

    }
}