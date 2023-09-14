package fr.harmoniamk.statsmk.compose.screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.compose.ui.MKBaseScreen
import fr.harmoniamk.statsmk.compose.ui.MKBottomSheet
import fr.harmoniamk.statsmk.compose.ui.MKPlayerItem
import fr.harmoniamk.statsmk.compose.ui.MKSegmentedButtons
import fr.harmoniamk.statsmk.compose.ui.MKTextField
import fr.harmoniamk.statsmk.compose.viewModel.TeamSettingsViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class, ExperimentalMaterialApi::class)
@Composable
fun TeamSettingsScreen(viewModel: TeamSettingsViewModel = hiltViewModel()) {
    val picture = viewModel.sharedPictureLoaded.collectAsState()
    val teamName = viewModel.sharedTeamName.collectAsState()
    val searchState = remember { mutableStateOf(TextFieldValue("")) }
    val players by viewModel.sharedPlayers.collectAsState()
    val currentState = viewModel.sharedBottomSheetValue.collectAsState(null)
    val bottomSheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
        onResult = viewModel::onPictureEdited
    )
    val buttons = listOf(
        Pair(R.string.modifier_l_quipe, viewModel::onEditTeam),
        Pair(R.string.modifier_le_logo) {
            launcher.launch(
                PickVisualMediaRequest(
                    mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly
                )
            )
        },
        Pair(R.string.ajouter_un_joueur, viewModel::onAddPlayer),
    )

    LaunchedEffect(Unit) {
        viewModel.sharedBottomSheetValue.collect {
            when (it) {
                null -> bottomSheetState.hide()
                else -> bottomSheetState.show()
            }
        }
    }
    MKBaseScreen(title = R.string.mon_quipe,
        subTitle = teamName.value.orEmpty(),
        state = bottomSheetState,
        sheetContent = {
            MKBottomSheet(
                trackIndex = null,
                state = currentState.value,
                onDismiss = viewModel::dismissBottomSheet,
                onEditPosition = {},
                onEditTrack = {}
            )
        }
    ) {
        MKSegmentedButtons(buttons = buttons)
        AsyncImage(
            model = picture.value,
            contentDescription = null,
            modifier = Modifier.size(100.dp)
        )
        MKTextField(
            value = searchState.value,
            onValueChange = {
                searchState.value = it
                viewModel.onSearch(it.text)
            },
            placeHolderRes = R.string.rechercher_un_joueur
        )
        LazyColumn(Modifier.padding(10.dp)) {
            items(items = players) {
                MKPlayerItem(
                    player = it.player,
                    editVisible = it.canEdit,
                    onEditClick = viewModel::onEditPlayer
                )
            }
        }
    }
}