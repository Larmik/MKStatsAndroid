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
fun RegistryScreen(viewModel: StatsMenuViewModel = hiltViewModel(), onItemClick: (String) -> Unit) {
    MKBaseScreen(title = R.string.registry) {
        listOfNotNull(
            MenuItems.ManagePlayers().takeIf { viewModel.sharedTeam.collectAsState().value != null },
            MenuItems.ManageTeams(),
            MenuItems.Players(),
            MenuItems.Profile(),
            MenuItems.Settings()
        ).forEach {
            MKListItem(item = it, separator = true, onNavigate = onItemClick) {}
        }
    }
}