package fr.harmoniamk.statsmk.compose.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.compose.ui.MKBaseScreen
import fr.harmoniamk.statsmk.compose.ui.MKBottomSheet
import fr.harmoniamk.statsmk.compose.ui.MKButton
import fr.harmoniamk.statsmk.compose.ui.MKDialog
import fr.harmoniamk.statsmk.compose.ui.MKPenaltyView
import fr.harmoniamk.statsmk.compose.ui.MKPlayerList
import fr.harmoniamk.statsmk.compose.ui.MKScoreView
import fr.harmoniamk.statsmk.compose.ui.MKSegmentedButtons
import fr.harmoniamk.statsmk.compose.ui.MKShockView
import fr.harmoniamk.statsmk.compose.ui.MKText
import fr.harmoniamk.statsmk.compose.ui.MKTrackItem
import fr.harmoniamk.statsmk.compose.viewModel.CurrentWarViewModel
import fr.harmoniamk.statsmk.model.firebase.Penalty
import fr.harmoniamk.statsmk.model.firebase.Shock
import fr.harmoniamk.statsmk.repository.mock.AuthenticationRepositoryMock
import fr.harmoniamk.statsmk.repository.mock.DatabaseRepositoryMock
import fr.harmoniamk.statsmk.repository.mock.FirebaseRepositoryMock
import fr.harmoniamk.statsmk.repository.mock.PreferencesRepositoryMock
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@Composable
@OptIn(ExperimentalMaterialApi::class)
fun CurrentWarScreen(
    viewModel: CurrentWarViewModel = hiltViewModel(),
    onNextTrack: () -> Unit,
    onBack: () -> Unit,
    onTrackClick: (String) -> Unit
) {

    val war = viewModel.sharedCurrentWar.collectAsState()
    val players = viewModel.sharedWarPlayers.collectAsState()
    val tracks = viewModel.sharedTracks.collectAsState()
    val currentState = viewModel.sharedBottomSheetValue.collectAsState()
    val dialogState = viewModel.sharedDialogValue.collectAsState()
    val bottomSheetState =
        rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)


    val buttons = listOf(
        Pair(R.string.remplacement, viewModel::onSubPlayer),
        Pair(R.string.p_nalit, viewModel::onPenalty),
        Pair(R.string.annuler_le_match, viewModel::onCancelClick),
    )
    LaunchedEffect(Unit) {
        viewModel.sharedBottomSheetValue.collect {
            when (it) {
                null -> bottomSheetState.hide()
                else -> bottomSheetState.show()
            }
        }
    }
    LaunchedEffect(Unit) {
        viewModel.sharedBackToWars.collect {
            onBack()
        }
    }
    BackHandler { onBack() }
    dialogState.value?.let { MKDialog(state = it) }
    MKBaseScreen(title = war.value?.name ?: "", subTitle = war.value?.displayedState,
        state = bottomSheetState,
        sheetContent = {
            MKBottomSheet(
                trackIndex = null,
                state = currentState.value,
                onDismiss = viewModel::dismissBottomSheet,
                onEditPosition = {},
                onEditTrack = {}
            )
        }) {
        MKSegmentedButtons(buttons = buttons)
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            MKPenaltyView(modifier = Modifier.weight(1f), penalties = war.value?.war?.penalties)
            MKScoreView(modifier = Modifier.weight(1f), war = war.value)
            MKShockView(modifier = Modifier.weight(1f), tracks = war.value?.warTracks)
        }
        players.value?.let { MKPlayerList(players = it) }

        MKButton(text = R.string.prochaine_course, onClick = onNextTrack)
        Spacer(modifier = Modifier.height(10.dp))
        tracks.value?.let {
            MKText(text = R.string.courses_jou_es, font = R.font.montserrat_bold)
            LazyColumn(Modifier.padding(10.dp)) {
                items(items = it) {
                    MKTrackItem(
                        modifier = Modifier.padding(bottom = 5.dp),
                        track = it,
                        goToDetails = { _ -> onTrackClick(it.track?.mid.orEmpty()) })
                }
            }
        }
    }
}

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@Preview
@Composable
fun CurrentWarScreenPreview() {
    CurrentWarScreen(
        viewModel = CurrentWarViewModel(
            firebaseRepository = FirebaseRepositoryMock(
                penalties = listOf(Penalty("12345", 20)),
                shocks = listOf(Shock("12345", 1))
            ),
            authenticationRepository = AuthenticationRepositoryMock(),
            databaseRepository = DatabaseRepositoryMock(),
            preferencesRepository = PreferencesRepositoryMock()
        ),
        onNextTrack = {},
        onBack = {},
        onTrackClick = {}
    )
}