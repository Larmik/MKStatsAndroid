package fr.harmoniamk.statsmk.compose.screen

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.text.input.TextFieldValue
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.compose.ui.MKBaseScreen
import fr.harmoniamk.statsmk.compose.ui.MKTextField
import fr.harmoniamk.statsmk.compose.ui.MKTrackItem
import fr.harmoniamk.statsmk.enums.Maps

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TrackListScreen() {
    val searchState = remember { mutableStateOf(TextFieldValue("")) }
    MKBaseScreen(title = R.string.tous_les_circuits) {
        MKTextField(
            value = searchState.value,
            onValueChange = { searchState.value = it },
            placeHolderRes = R.string.rechercher_un_nom_ou_une_abr_viation)
        LazyColumn() {
            items(Maps.values()) {
                MKTrackItem(map = it)
            }
        }
    }
}