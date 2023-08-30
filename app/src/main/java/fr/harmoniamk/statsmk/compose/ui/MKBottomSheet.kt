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
fun MKBottomSheet(track: MKWarTrack? = null, trackIndex: Int?, state: MKBottomSheetState?, onEditTrack: (Int) -> Unit = { }, onDismiss: (Int) -> Unit, onEditPosition: (Int) -> Unit = { }) {
    when (state) {
        is MKBottomSheetState.EditTrack -> {
            trackIndex?.let {
                TrackListScreen(onTrackClick = onEditTrack, editing = true, trackIndex = it, onDismiss = onDismiss)
            }

        }
        is MKBottomSheetState.EditPositions -> {
            trackIndex?.let {
                PositionScreen(track = track, trackIndex = it, onBack = onDismiss, editing = true, onNext = onEditPosition)
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