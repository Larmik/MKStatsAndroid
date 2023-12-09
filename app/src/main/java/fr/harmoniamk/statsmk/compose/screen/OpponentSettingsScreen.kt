package fr.harmoniamk.statsmk.compose.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.compose.ui.MKBaseScreen
import fr.harmoniamk.statsmk.compose.ui.MKCTeamItem
import fr.harmoniamk.statsmk.compose.ui.MKTextField
import fr.harmoniamk.statsmk.compose.viewModel.OpponentSettingsViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class, ExperimentalMaterialApi::class)
@Composable
fun OpponentSettingsScreen(viewModel: OpponentSettingsViewModel = hiltViewModel(), onTeamClick: (String) -> Unit) {
    val searchState = remember { mutableStateOf(TextFieldValue("")) }
    val teams = viewModel.sharedTeams.collectAsState()

    MKBaseScreen(title = R.string.adversaires) {
        MKTextField(
            value = searchState.value,
            onValueChange = {
                searchState.value = it
                viewModel.onSearch(it.text)
            },
            placeHolderRes = R.string.rechercher_un_advsersaire
        )
        LazyColumn(Modifier.padding(vertical = 10.dp)) {
            items(items = teams.value) {
                MKCTeamItem(
                    team = it,
                    onClick = onTeamClick,
                )
            }
        }
    }
}