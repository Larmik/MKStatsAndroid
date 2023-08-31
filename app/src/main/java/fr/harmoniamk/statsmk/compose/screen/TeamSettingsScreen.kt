package fr.harmoniamk.statsmk.compose.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.compose.ui.MKBaseScreen
import fr.harmoniamk.statsmk.compose.ui.MKPlayerItem
import fr.harmoniamk.statsmk.compose.ui.MKSegmentedButtons
import fr.harmoniamk.statsmk.compose.ui.MKText
import fr.harmoniamk.statsmk.compose.ui.MKTextField
import fr.harmoniamk.statsmk.fragment.settings.managePlayers.ManagePlayersViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class, ExperimentalMaterialApi::class)
@Composable
fun TeamSettingsScreen(viewModel: ManagePlayersViewModel = hiltViewModel()) {
    val picture = viewModel.sharedPictureLoaded.collectAsState()
    val teamName = viewModel.sharedTeamName.collectAsState()
    val searchState = remember { mutableStateOf(TextFieldValue("")) }
    val players = viewModel.sharedPlayers.collectAsState()

    val buttons = listOf(
        Pair(R.string.modifier_l_quipe, {}),
        Pair(R.string.modifier_le_logo, {}),
        Pair(R.string.ajouter_un_joueur, {}),
    )
    MKBaseScreen(title = R.string.mon_quipe) {
        MKSegmentedButtons(buttons = buttons)
        AsyncImage(model = picture.value, contentDescription = null)
        MKText(text = teamName.value.orEmpty())
        Spacer(modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(color = colorResource(id = R.color.white)))
        MKTextField(value = searchState.value, onValueChange = { searchState.value = it }, placeHolderRes = R.string.rechercher_un_joueur)
        LazyColumn(Modifier.padding(10.dp)) {
            items(items = players.value) {
                MKPlayerItem(playerToManage = it)
            }
        }

    }

}