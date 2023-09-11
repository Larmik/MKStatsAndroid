package fr.harmoniamk.statsmk.compose.screen.root

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.compose.ui.MKBaseScreen
import fr.harmoniamk.statsmk.compose.ui.MKListItem
import fr.harmoniamk.statsmk.enums.MenuItems


@Composable
@OptIn(ExperimentalMaterialApi::class)
fun SettingsScreen(onItemClick: (String) -> Unit) {
    MKBaseScreen(title = R.string.settings) {
        listOf(
            MenuItems.ManagePlayers(),
            MenuItems.ManageTeams(),
            MenuItems.Players(),
            MenuItems.Profile(),
        ).forEach {
            MKListItem(item = it, separator = true, onNavigate = onItemClick) {}
        }
    }
}