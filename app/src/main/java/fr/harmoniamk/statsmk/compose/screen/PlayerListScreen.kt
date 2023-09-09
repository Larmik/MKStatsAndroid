package fr.harmoniamk.statsmk.compose.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.compose.ui.MKBaseScreen
import fr.harmoniamk.statsmk.compose.ui.MKButton
import fr.harmoniamk.statsmk.compose.ui.MKPlayerItem
import fr.harmoniamk.statsmk.compose.ui.MKText
import fr.harmoniamk.statsmk.compose.viewModel.PlayerListViewModel
import fr.harmoniamk.statsmk.compose.viewModel.PlayerListViewModel.Companion.viewModel
import fr.harmoniamk.statsmk.extension.isTrue
import kotlinx.coroutines.flow.filterNotNull

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PlayerListScreen(teamId: String?, onWarStarted: () -> Unit) {
    val viewModel: PlayerListViewModel = viewModel(id = teamId)
    val players = viewModel.sharedPlayers.collectAsState()
    val warName = viewModel.sharedWarName.collectAsState()
    LaunchedEffect(Unit) {
        viewModel.sharedStarted.filterNotNull().collect {
            onWarStarted()
        }
    }
    MKBaseScreen(title = R.string.cr_er_une_war, subTitle = warName.value) {
        MKText(
            text = R.string.s_lectionnez_les_six_joueurs_de_votre_quipe,
            modifier = Modifier.padding(vertical = 10.dp)
        )
        LazyColumn(Modifier.weight(1f)) {
            items(players.value.orEmpty()) {
                MKPlayerItem(player = it.user, isSelected = it.isSelected.isTrue, onRootClick = {
                    viewModel.selectUser(it.copy(isSelected = !it.isSelected.isTrue))
                }) {

                }
            }
        }
        MKButton(
            text = R.string.valider,
            enabled = players.value?.filter { it.isSelected.isTrue }?.size == 6,
            onClick = viewModel::createWar
        )
    }

}