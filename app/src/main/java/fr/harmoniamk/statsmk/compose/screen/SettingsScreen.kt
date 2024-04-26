package fr.harmoniamk.statsmk.compose.screen

import android.widget.Toast
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.compose.ui.MKBaseScreen
import fr.harmoniamk.statsmk.compose.ui.MKBottomSheet
import fr.harmoniamk.statsmk.compose.ui.MKDialog
import fr.harmoniamk.statsmk.compose.ui.MKListItem
import fr.harmoniamk.statsmk.compose.ui.MKText
import fr.harmoniamk.statsmk.compose.viewModel.SettingsViewModel
import fr.harmoniamk.statsmk.enums.MenuItems

@Composable
@OptIn(ExperimentalMaterialApi::class)
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel(), onSettingsItemClick: (String) -> Unit) {

    val lastUpdate = viewModel.sharedLastUpdate.collectAsState()
    val loadingState = viewModel.sharedDialogValue.collectAsState()
    val context = LocalContext.current

    val currentState = viewModel.sharedBottomSheetValue.collectAsState()
    val bottomSheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        confirmValueChange = { it == ModalBottomSheetValue.Expanded || it == ModalBottomSheetValue.HalfExpanded }
    )

    loadingState.value?.let { MKDialog(state = it) }

    LaunchedEffect(Unit) {
        viewModel.sharedBottomSheetValue.collect {
            when (it) {
                null -> bottomSheetState.hide()
                else -> bottomSheetState.show()
            }
        }
    }
    LaunchedEffect(Unit) {
        viewModel.sharedToast.collect {
            Toast.makeText(context, context.getString(it), Toast.LENGTH_SHORT).show()
        }
    }

    MKBaseScreen(title = R.string.settings,
        state = bottomSheetState,
        sheetContent = {
            MKBottomSheet(
                trackIndex = null,
                state = currentState.value,
                onDismiss = viewModel::dismissBottomSheet,
                onDisplayModeValidated = viewModel::setStatsDisplayMode,
                onColorsSelected = viewModel::setColorsTheme
            )
        }) {
        listOfNotNull(
            MenuItems.Theme(),
            MenuItems.StatsDisplayMode(),
            MenuItems.Refresh(),
            MenuItems.PurgeUsers().takeIf { viewModel.isGod },
            MenuItems.FetchTags().takeIf { viewModel.isGod },
            MenuItems.Help(),
            MenuItems.Credit(),
            MenuItems.Coffee(),
        ).forEach {
            MKListItem(item = it, separator = true, onNavigate = onSettingsItemClick, onClick = {
                when (it) {
                    is MenuItems.Refresh -> viewModel.onUpdate()
                    is MenuItems.PurgeUsers -> viewModel.purgeUsers()
                    is MenuItems.FetchTags -> viewModel.fetchTags()
                    is MenuItems.StatsDisplayMode -> viewModel.showStatsDisplay()
                    is MenuItems.Theme -> viewModel.showTheme()
                    else -> { }
                }
            })
        }
        Spacer(Modifier.weight(1f))
        MKText(text ="${stringResource(R.string.derni_re_mise_jour)} ${lastUpdate.value}", modifier = Modifier.padding(bottom = 10.dp))

    }
}