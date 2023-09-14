package fr.harmoniamk.statsmk.compose.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.compose.ui.MKBaseScreen
import fr.harmoniamk.statsmk.compose.ui.MKTextField
import fr.harmoniamk.statsmk.compose.ui.MKTrackItem
import fr.harmoniamk.statsmk.compose.ui.MKWarTrackItem
import fr.harmoniamk.statsmk.compose.viewModel.WarTrackListViewModel
import fr.harmoniamk.statsmk.enums.Maps

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun WarTrackListScreen(viewModel: WarTrackListViewModel = hiltViewModel(), trackIndex: Int, userId: String? = null, teamId: String? = null) {

    val stats = viewModel.sharedMapStats.collectAsState()
    val searchState = remember { mutableStateOf(TextFieldValue("")) }
    viewModel.init(trackIndex, teamId, userId)

    MKBaseScreen(title = R.string.d_tails) {
        Maps.values().getOrNull(trackIndex)?.let {
            MKTrackItem(map = it)
        }
        Row(
            Modifier
                .padding(10.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            MKTextField(
                modifier = Modifier.fillMaxWidth(0.8f),
                value = searchState.value,
                onValueChange = {
                    searchState.value = it
                    viewModel.onSearch(it.text)
                },
                placeHolderRes = R.string.rechercher_un_advsersaire
            )
            Image(modifier = Modifier
                .size(30.dp)
                .clickable { },
                painter = painterResource(id = R.drawable.listoption),
                contentDescription = null
            )
        }
        LazyColumn {
            items(items = stats.value.orEmpty()) {
                MKWarTrackItem(details = it, isIndiv = userId != null)
            }
        }
    }

}