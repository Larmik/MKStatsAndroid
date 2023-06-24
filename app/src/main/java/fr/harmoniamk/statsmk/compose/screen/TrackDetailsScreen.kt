package fr.harmoniamk.statsmk.compose.screen

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.compose.ui.MKBaseScreen
import fr.harmoniamk.statsmk.compose.ui.MKPlayerItem
import fr.harmoniamk.statsmk.compose.ui.MKScoreView
import fr.harmoniamk.statsmk.compose.ui.MKSegmentedButtons
import fr.harmoniamk.statsmk.compose.ui.MKText
import fr.harmoniamk.statsmk.compose.ui.MKTrackItem
import fr.harmoniamk.statsmk.compose.viewModel.TrackDetailsViewModel
import fr.harmoniamk.statsmk.compose.viewModel.TrackDetailsViewModel.Companion.viewModel
import fr.harmoniamk.statsmk.enums.Maps
import kotlinx.coroutines.flow.filterNotNull

@Composable
@OptIn(ExperimentalMaterialApi::class)
fun TrackDetailsScreen(warId: String, warTrackId: String) {
    val viewModel: TrackDetailsViewModel = viewModel(warId = warId, warTrackId = warTrackId)
    val war = viewModel.sharedWar.collectAsState()
    val currentTrack = viewModel.sharedCurrentTrack.collectAsState()
    val positions = viewModel.sharedPositions.collectAsState()
    val buttonsVisible = viewModel.sharedButtonsVisible.collectAsState()
    val bottomSheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
    
    val buttons = listOf(
        Pair(R.string.editer_circuit, viewModel::onEditTrack),
        Pair(R.string.editer_positions, viewModel::onEditPositions),
        Pair(R.string.editer_shocks, viewModel::onEditShocks),
    )

    LaunchedEffect(Unit) {
        viewModel.sharedBottomSheetValue.filterNotNull().collect {
            bottomSheetState.show()
        }
    }
    
    MKBaseScreen(title = war.value?.name.orEmpty(), subTitle = war.value?.war?.createdDate, state = bottomSheetState, sheetContent = {
        MKText(text = viewModel.sharedBottomSheetValue.collectAsState(null).value ?: "Error")
    }) {
        currentTrack.value?.index?.let { MKTrackItem(map = Maps.values()[it]) }
        buttonsVisible.value.takeIf { it }?.let { 
            MKSegmentedButtons(buttons = buttons)
        }
        LazyColumn {
            items(items = positions.value.orEmpty()) {
                MKPlayerItem(position = it, shockVisible = false)
            }
        }
        currentTrack.value?.let { MKScoreView(track = it) }
    }
}