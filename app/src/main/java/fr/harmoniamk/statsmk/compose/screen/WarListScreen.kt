package fr.harmoniamk.statsmk.compose.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.compose.ui.MKBaseScreen
import fr.harmoniamk.statsmk.compose.ui.MKBottomSheet
import fr.harmoniamk.statsmk.compose.ui.MKTextField
import fr.harmoniamk.statsmk.compose.ui.MKWarItem
import fr.harmoniamk.statsmk.compose.viewModel.WarListViewModel
import fr.harmoniamk.statsmk.enums.WarSortType

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun WarListScreen(viewModel: WarListViewModel = hiltViewModel(), userId: String? = null, teamId: String? = null, isWeek: Boolean? = null, onWarClick: (String) -> Unit) {

    val wars = viewModel.sharedWars.collectAsState()
    val searchState = remember { mutableStateOf(TextFieldValue("")) }
    val currentState = viewModel.sharedBottomSheetValue.collectAsState()
    val bottomSheetState =
        rememberModalBottomSheetState(
            initialValue = ModalBottomSheetValue.Hidden,
            confirmValueChange = { it == ModalBottomSheetValue.Expanded || it == ModalBottomSheetValue.HalfExpanded })

    viewModel.init(userId, teamId, isWeek, WarSortType.DATE, listOf())
    LaunchedEffect(Unit) {
        viewModel.sharedBottomSheetValue.collect {
            when (it) {
                null -> bottomSheetState.hide()
                else -> bottomSheetState.show()
            }
        }
    }
    MKBaseScreen(title = R.string.toutes_les_wars_en_quipe,
        state = bottomSheetState,
        sheetContent = {
            MKBottomSheet(
                trackIndex = null,
                state = currentState.value,
                onDismiss = viewModel::dismissBottomSheet,
                onEditPosition = {},
                onEditTrack = {},
                onSorted = { viewModel.onSorted(it) },
                onFiltered = { viewModel.onFiltered(it) }
            )
        }) {
        Row(
            Modifier
                .padding(10.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            MKTextField(
                modifier = Modifier.fillMaxWidth(0.8f),
                value = searchState.value,
                onValueChange = {
                    searchState.value = it
                    viewModel.onSearch(it.text)
                },
                placeHolderRes = R.string.rechercher_un_advsersaire
            )
            Image(modifier = Modifier
                .size(30.dp)
                .clickable { viewModel.onClickOptions() },
                painter = painterResource(id = R.drawable.listoption),
                contentDescription = null
            )
        }
        LazyColumn {
            items(items = wars.value) { war ->
                MKWarItem(war = war, onClick = {
                    war.war?.mid?.let {
                        onWarClick(it)
                    }
                })
            }
        }
    }

}