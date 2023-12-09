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
import fr.harmoniamk.statsmk.compose.ui.MKCTeamItem
import fr.harmoniamk.statsmk.compose.ui.MKTextField
import fr.harmoniamk.statsmk.compose.viewModel.TeamListViewModel

@Composable
@OptIn(ExperimentalMaterialApi::class)
fun TeamListScreen(viewModel: TeamListViewModel = hiltViewModel(), onTeamClick: (String) -> Unit) {
    val teams = viewModel.sharedTeams.collectAsState()
    val searchState = remember { mutableStateOf(TextFieldValue("")) }
    MKBaseScreen(title = R.string.cr_er_une_war, subTitle = R.string.s_lectionnez_un_adversaire) {
        MKTextField(
            value = searchState.value,
            onValueChange = {
                searchState.value = it
                viewModel.search(it.text)
            },
            placeHolderRes = R.string.rechercher_un_advsersaire
        )
        LazyColumn {
            items(teams.value.orEmpty()) {
                MKCTeamItem(team = it, onClick = onTeamClick)
            }
        }
    }
}