package fr.harmoniamk.statsmk.compose.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.compose.ui.MKBaseScreen
import fr.harmoniamk.statsmk.compose.ui.MKBottomSheet
import fr.harmoniamk.statsmk.compose.ui.MKSegmentedButtons
import fr.harmoniamk.statsmk.compose.ui.MKTeamItem
import fr.harmoniamk.statsmk.compose.ui.MKTextField
import fr.harmoniamk.statsmk.compose.viewModel.OpponentSettingsViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class, ExperimentalMaterialApi::class)
@Composable
fun OpponentSettingsScreen(viewModel: OpponentSettingsViewModel = hiltViewModel()) {
    val searchState = remember { mutableStateOf(TextFieldValue("")) }
    val teams = viewModel.sharedTeams.collectAsState()
    val addTeamVisible = viewModel.sharedAddTeamVisibility.collectAsState()
    val bottomSheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
    val currentState = viewModel.sharedBottomSheetValue.collectAsState(null)
    LaunchedEffect(Unit) {
        viewModel.sharedBottomSheetValue.collect {
            when (it) {
                null -> bottomSheetState.hide()
                else -> bottomSheetState.show()
            }
        }
    }
    MKBaseScreen(title = R.string.adversaires, state = bottomSheetState, sheetContent = {
        MKBottomSheet(
            trackIndex = null,
            state = currentState.value,
            onDismiss = viewModel::dismissBottomSheet,
            onEditPosition = {},
            onEditTrack = {}
        )
    }) {
        addTeamVisible.value.takeIf { it }?.let {
            MKSegmentedButtons(
                buttons = listOf(
                    Pair(R.string.ajouter_une_quipe, viewModel::onAddTeam)
                )
            )
        }
        MKTextField(
            value = searchState.value,
            onValueChange = {
                searchState.value = it
                viewModel.onSearch(it.text)
            },
            placeHolderRes = R.string.rechercher_un_advsersaire
        )
        LazyColumn(Modifier.padding(10.dp)) {
            items(items = teams.value) {
                MKTeamItem(
                    teamToManage = it,
                    editVisible = addTeamVisible.value,
                    onClick = {},
                    onEditClick = viewModel::onEditTeam
                )
            }
        }
    }
}