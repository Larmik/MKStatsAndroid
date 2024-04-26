package fr.harmoniamk.statsmk.compose.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.compose.ui.MKBaseScreen
import fr.harmoniamk.statsmk.compose.ui.MKBottomSheet
import fr.harmoniamk.statsmk.compose.ui.MKPlayerItem
import fr.harmoniamk.statsmk.compose.ui.MKScoreView
import fr.harmoniamk.statsmk.compose.ui.MKSegmentedButtons
import fr.harmoniamk.statsmk.compose.ui.MKTrackItem
import fr.harmoniamk.statsmk.compose.viewModel.TrackDetailsViewModel
import fr.harmoniamk.statsmk.compose.viewModel.TrackDetailsViewModel.Companion.viewModel
import fr.harmoniamk.statsmk.enums.Maps
import kotlinx.coroutines.launch

@Composable
@OptIn(ExperimentalMaterialApi::class)
fun TrackDetailsScreen(warId: String, warTrackId: String, onBack: () -> Unit) {
    val viewModel: TrackDetailsViewModel = viewModel(warId = warId, warTrackId = warTrackId)
    val war = viewModel.sharedWar.collectAsState()
    val currentTrack = viewModel.sharedCurrentTrack.collectAsState()
    val positions = viewModel.sharedPositions.collectAsState()
    val shocks = viewModel.sharedShocks.collectAsState()
    val buttonsVisible = viewModel.sharedButtonsVisible.collectAsState()
    val currentState = viewModel.sharedBottomSheetValue.collectAsState()
    val bottomSheetState =
        rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
    val coroutineScope = rememberCoroutineScope()
    val buttons = listOf(
        Pair(
            stringResource(R.string.editer_circuit), viewModel::onEditTrack),
        Pair(stringResource(R.string.editer_positions), viewModel::onEditPositions),
        Pair(stringResource(R.string.editer_shocks), viewModel::onEditShocks),
    )
    LaunchedEffect(Unit) {
        viewModel.sharedBottomSheetValue.collect {
            when (it) {
                null -> bottomSheetState.hide()
                else -> bottomSheetState.show()
            }
        }
    }
    BackHandler {
        when (bottomSheetState.isVisible) {
            true -> coroutineScope.launch { bottomSheetState.hide() }
            else -> onBack()
        }
    }
    val trackIndex = war.value?.warTracks?.map { it.track }?.indexOf(currentTrack.value?.track)
    MKBaseScreen(
        title = war.value?.name.orEmpty(),
        subTitle = war.value?.war?.createdDate,
        state = bottomSheetState,
        sheetContent = {
            MKBottomSheet(
                trackIndex = trackIndex,
                state = currentState.value,
                onDismiss = { trackIndex?.let { viewModel.dismissBottomSheet(it) } },
                onEditPosition = {},
                onEditTrack = {}
            )
        },
        content = {
            currentTrack.value?.index?.let { MKTrackItem(map = Maps.entries[it]) }
            when (buttonsVisible.value) {
                true -> MKSegmentedButtons(buttons = buttons)
                else -> Spacer(modifier = Modifier.height(10.dp))
            }
            LazyColumn {
                items(items = positions.value.orEmpty()) {
                    MKPlayerItem(
                        player = it.mkcPlayer,
                        position = it,
                        shockVisible = false,
                        shockCount = shocks.value?.singleOrNull { shock -> shock.playerId == it.mkcPlayer?.mkcId }?.count ?: 0
                    )
                }
            }
            currentTrack.value?.let { MKScoreView(track = it) }
        }
    )
}