package fr.harmoniamk.statsmk.compose.ui

import androidx.compose.runtime.Composable
import fr.harmoniamk.statsmk.compose.screen.PositionScreen
import fr.harmoniamk.statsmk.compose.screen.TrackListScreen

sealed class MKBottomSheetState() {
    class EditTrack() : MKBottomSheetState()
    class EditPositions() : MKBottomSheetState()
    class EditShocks() : MKBottomSheetState()
    class SubPlayer() : MKBottomSheetState()
    class Penalty() : MKBottomSheetState()
}

@Composable
fun MKBottomSheet(trackIndex: Int?, state: MKBottomSheetState?, onEditTrack: (Int) -> Unit = { }, onDismiss: (Int) -> Unit, onEditPosition: (Int) -> Unit = { }) {
    when (state) {
        is MKBottomSheetState.EditTrack -> {
            trackIndex?.let {
                TrackListScreen(onTrackClick = onEditTrack, editing = true, trackIndex = it, onDismiss = onDismiss)
            }

        }
        is MKBottomSheetState.EditPositions -> {
            trackIndex?.let {
                PositionScreen(trackIndex = it, onBack = onDismiss, editing = true, onNext = onEditPosition)
            }

        }
        is MKBottomSheetState.EditShocks -> {

        }
        else -> {}
    }


}