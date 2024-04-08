package fr.harmoniamk.statsmk.compose.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.compose.ui.MKBaseScreen
import fr.harmoniamk.statsmk.compose.ui.MKTextField
import fr.harmoniamk.statsmk.compose.ui.MKTrackItem
import fr.harmoniamk.statsmk.compose.viewModel.TrackListViewModel.Companion.viewModel
import kotlinx.coroutines.flow.filterNotNull

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TrackListScreen(
    editing: Boolean = false,
    trackIndex: Int = -1,
    onTrackClick: (Int) -> Unit,
    onDismiss: () -> Unit = {}
) {
    val viewModel = viewModel(editing)
    val tracks = viewModel.sharedSearchedItems.collectAsState()
    val searchState = remember { mutableStateOf(TextFieldValue("")) }
    val war = viewModel.sharedCurrentWar.collectAsState()
    LaunchedEffect(Unit) {
        viewModel.sharedQuit.filterNotNull().takeIf { editing }?.collect {
            onDismiss()
        }
    }
    MKBaseScreen(title = R.string.tous_les_circuits) {
        MKTextField(
            value = searchState.value,
            onValueChange = {
                searchState.value = it
                viewModel.search(it.text)
            },
            placeHolderRes = R.string.rechercher_un_nom_ou_une_abr_viation
        )
        LazyColumn {
            items(tracks.value) {
                MKTrackItem(
                    modifier = Modifier.padding(bottom = 5.dp),
                    map = it,
                    onClick = { index ->
                        when (editing) {
                            true -> viewModel.editTrack(
                                war = war.value,
                                indexInList = trackIndex,
                                newTrackIndex = index
                            )
                            else -> viewModel.addTrack(index)
                        }
                        onTrackClick(index)
                    }
                )
            }
        }
    }
}