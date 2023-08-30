package fr.harmoniamk.statsmk.compose.ui

import androidx.compose.runtime.Composable
import fr.harmoniamk.statsmk.compose.screen.PositionScreen
import fr.harmoniamk.statsmk.compose.screen.TrackListScreen
import fr.harmoniamk.statsmk.compose.screen.WarTrackResultScreen
import fr.harmoniamk.statsmk.model.local.MKWarTrack

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
                TrackListScreen(
                    trackIndex = it,
                    editing = true,
                    onDismiss = onDismiss,
                    onTrackClick = onEditTrack
                )
            }
        }
        is MKBottomSheetState.EditPositions -> {
            trackIndex?.let {
                PositionScreen(
                    trackIndex = it,
                    editing = true,
                    onBack = onDismiss,
                    onNext = onEditPosition
                )
            }

        }
        is MKBottomSheetState.EditShocks -> {
            trackIndex?.let {
                WarTrackResultScreen(
                    trackIndex = it,
                    editing = true,
                    onBack = { onDismiss(trackIndex) },
                    backToCurrent = { onDismiss(trackIndex) },
                    goToResume = { onDismiss(trackIndex) }
                )
            }
        }
        else -> {}
    }


}