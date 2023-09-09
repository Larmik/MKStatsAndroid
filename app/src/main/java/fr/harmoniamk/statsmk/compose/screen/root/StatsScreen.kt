package fr.harmoniamk.statsmk.compose.screen.root

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.compose.ui.MKBaseScreen
import fr.harmoniamk.statsmk.compose.ui.MKListItem
import fr.harmoniamk.statsmk.enums.ListItemType
import fr.harmoniamk.statsmk.enums.ListItems


@Composable
@OptIn(ExperimentalMaterialApi::class)
fun StatsScreen(onItemClick: (String?) -> Unit) {
    MKBaseScreen(title = R.string.stats) {
        ListItems.values().filter { it.type == ListItemType.stats }.forEach {
            MKListItem(item = it, separator = true, onNavigate = onItemClick) {}
        }
    }
}