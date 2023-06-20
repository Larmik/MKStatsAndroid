package fr.harmoniamk.statsmk.compose.screen

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.text.input.TextFieldValue
import androidx.hilt.navigation.compose.hiltViewModel
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.compose.ui.MKBaseScreen
import fr.harmoniamk.statsmk.compose.ui.MKTextField
import fr.harmoniamk.statsmk.compose.ui.MKTrackItem
import fr.harmoniamk.statsmk.compose.viewModel.TrackListViewModel

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TrackListScreen(viewModel: TrackListViewModel = hiltViewModel(), onTrackClick: (Int) -> Unit) {
    val tracks = viewModel.sharedSearchedItems.collectAsState()
    val searchState = remember { mutableStateOf(TextFieldValue("")) }
    MKBaseScreen(title = R.string.tous_les_circuits) {
        MKTextField(
            value = searchState.value,
            onValueChange = {
                searchState.value = it
                viewModel.search(it.text)
            },
            placeHolderRes = R.string.rechercher_un_nom_ou_une_abr_viation)
        LazyColumn {
            items(tracks.value) {
                MKTrackItem(map = it, onClick = { index ->
                    viewModel.addTrack(index)
                    onTrackClick(index)
                })
            }
        }
    }
}