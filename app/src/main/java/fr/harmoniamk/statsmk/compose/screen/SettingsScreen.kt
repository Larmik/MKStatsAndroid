package fr.harmoniamk.statsmk.compose.screen

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.compose.ui.MKBaseScreen
import fr.harmoniamk.statsmk.compose.ui.MKDialog
import fr.harmoniamk.statsmk.compose.ui.MKListItem
import fr.harmoniamk.statsmk.compose.ui.MKText
import fr.harmoniamk.statsmk.compose.viewModel.SettingsViewModel
import fr.harmoniamk.statsmk.enums.MenuItems

@Composable
@OptIn(ExperimentalMaterialApi::class)
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {

    val lastUpdate = viewModel.sharedLastUpdate.collectAsState()
    val loadingState = viewModel.sharedDialogValue.collectAsState()

    loadingState.value?.let { MKDialog(state = it) }

    MKBaseScreen(title = R.string.settings) {
        listOfNotNull(
            MenuItems.Refresh()
        ).forEach {
            MKListItem(item = it, separator = true, onNavigate = {}, onClick = {
                viewModel.onUpdate()
            })
        }
        Spacer(Modifier.height(20.dp))
        MKText(text =" Dernière mise à jour : ${lastUpdate.value}" )

    }
}