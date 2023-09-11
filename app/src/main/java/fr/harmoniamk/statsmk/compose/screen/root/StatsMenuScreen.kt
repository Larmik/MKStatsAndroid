package fr.harmoniamk.statsmk.compose.screen.root

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.compose.ui.MKBaseScreen
import fr.harmoniamk.statsmk.compose.ui.MKListItem
import fr.harmoniamk.statsmk.compose.viewModel.StatsMenuViewModel
import fr.harmoniamk.statsmk.enums.MenuItems


@Composable
@OptIn(ExperimentalMaterialApi::class)
fun StatsMenuScreen(viewModel: StatsMenuViewModel = hiltViewModel(), onItemClick: (String) -> Unit) {
    MKBaseScreen(title = R.string.stats) {
        listOf(
            MenuItems.IndivStats(viewModel.sharedId.collectAsState().value.orEmpty()),
            MenuItems.TeamStats(),
            MenuItems.PlayerStats(),
            MenuItems.OpponentStats(),
            MenuItems.MapStats(),
        ).forEach {
            MKListItem(item = it, separator = true, onNavigate = onItemClick) {}
        }
    }
}