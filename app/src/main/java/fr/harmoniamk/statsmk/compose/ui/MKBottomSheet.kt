package fr.harmoniamk.statsmk.compose.ui

import androidx.compose.runtime.Composable
import fr.harmoniamk.statsmk.compose.screen.EditUserScreen
import fr.harmoniamk.statsmk.compose.screen.FilterSortScreen
import fr.harmoniamk.statsmk.compose.screen.PenaltyScreen
import fr.harmoniamk.statsmk.compose.screen.PlayersSettingsScreen
import fr.harmoniamk.statsmk.compose.screen.PositionScreen
import fr.harmoniamk.statsmk.compose.screen.ResetPasswordScreen
import fr.harmoniamk.statsmk.compose.screen.SubPlayerScreen
import fr.harmoniamk.statsmk.compose.screen.TrackListScreen
import fr.harmoniamk.statsmk.compose.screen.WarTrackResultScreen
import fr.harmoniamk.statsmk.compose.viewModel.Filter
import fr.harmoniamk.statsmk.compose.viewModel.Sort
import fr.harmoniamk.statsmk.enums.FilterType
import fr.harmoniamk.statsmk.enums.SortType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

sealed class MKBottomSheetState {
    class EditTrack : MKBottomSheetState()
    class EditPositions : MKBottomSheetState()
    class EditShocks : MKBottomSheetState()
    class SubPlayer : MKBottomSheetState()
    class Penalty : MKBottomSheetState()
    class EditUser : MKBottomSheetState()
    class ResetPassword : MKBottomSheetState()
    class FilterSort(val sort: Sort, val filter: Filter): MKBottomSheetState()
}

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@Composable
fun MKBottomSheet(
    trackIndex: Int?,
    state: MKBottomSheetState?,
    onEditTrack: (Int) -> Unit = { },
    onDismiss: () -> Unit,
    onEditPosition: (Int) -> Unit = { },
    onSorted: (SortType) -> Unit = { },
    onFiltered: (List<FilterType>) -> Unit = { },
) {
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
                    onBack = onDismiss,
                    backToCurrent = onDismiss
                )
            }
        }
        is MKBottomSheetState.SubPlayer -> {
            SubPlayerScreen(onDismiss = onDismiss)
        }
        is MKBottomSheetState.Penalty -> {
            PenaltyScreen(onDismiss = onDismiss)
        }
        is MKBottomSheetState.EditUser -> {
            EditUserScreen(onDismiss = onDismiss)
        }
        is MKBottomSheetState.ResetPassword -> {
            ResetPasswordScreen(onDismiss = onDismiss)
        }
        is MKBottomSheetState.FilterSort -> {
            FilterSortScreen(sort = state.sort, filter = state.filter, onDismiss = onDismiss, onSorted = onSorted, onFiltered = onFiltered)
        }
        else -> {}
    }
}