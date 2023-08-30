package fr.harmoniamk.statsmk.compose.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
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

    val bottomSheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
    val coroutineScope = rememberCoroutineScope()
    
    val buttons = listOf(
        Pair(R.string.editer_circuit, viewModel::onEditTrack),
        Pair(R.string.editer_positions, viewModel::onEditPositions),
        Pair(R.string.editer_shocks, viewModel::onEditShocks),
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
    
    MKBaseScreen(
        title = war.value?.name.orEmpty(),
        subTitle = war.value?.war?.createdDate,
        state = bottomSheetState,
        sheetContent = {
            MKBottomSheet(
                trackIndex = war.value?.warTracks?.map { it.track }?.indexOf(currentTrack.value?.track) ,
                state = currentState.value,
                onDismiss = viewModel::dismissBottomSheet)
            },
        content = {
            currentTrack.value?.index?.let { MKTrackItem(map = Maps.values()[it]) }
            buttonsVisible.value.takeIf { it }?.let { MKSegmentedButtons(buttons = buttons) }
            LazyColumn {
                items(items = positions.value.orEmpty()) {
                    MKPlayerItem(
                        position = it,
                        shockVisible = false,
                        shockCount = shocks.value?.singleOrNull { shock -> shock.playerId == it.player?.mid }?.count ?: 0
                    )
                }
            }
            currentTrack.value?.let { MKScoreView(track = it) }
        }
    )
}